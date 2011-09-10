package android.privacy.surrogate;

import android.content.Context;
import android.content.Intent;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Provides privacy methods for ActivityManagerService
 * @author Anonymous
 */
public final class PrivacyActivityManagerService {
    
    private final static String TAG = "PrivacyActivityManagerService";
    
    private static PrivacySettingsManager pSetMan;
    
    private static Intent tmpIn;
    private static long tmpInHash = 0;
    private static int tmpInReceivers = 0;
    
    private static Intent tmpOut;
    private static long tmpOutHash = 0;
    private static int tmpOutReceivers = 0;
    
    private static Intent tmpSms;
    private static long tmpSmsHash = 0;
    private static int tmpSmsReceivers = 0;
    
    private static Intent tmpMms;
    private static long tmpMmsHash = 0;
    private static int tmpMmsReceivers = 0;
    
    /**
     * Intercepts broadcasts and replaces the broadcast contents according to 
     * privacy permissions
     * @param packageName may not be null
     * @param uid must be >= 0
     * @param intent intent.getAction() may not return null
     */
    public static void enforcePrivacyPermission(String packageName, int uid, Intent intent, Context context, int receivers) {
        if (pSetMan == null) pSetMan = (PrivacySettingsManager) context.getSystemService("privacy");
        PrivacySettings pSet;
        String action = intent.getAction();
        String output;
        // outgoing call
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            pSet = pSetMan.getSettings(packageName, uid);
            output = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            
            // store the original version to supply real values to trusted applications
            // since Android sends the same intent to multiple receivers
            if (tmpOutHash != hashCode(intent)) {
                tmpOut = (Intent)intent.clone();
                tmpOutHash = hashCode(intent);
                tmpOutReceivers = receivers;
            }
            
            try {
                if (pSet != null && pSet.getOutgoingCallsSetting() != PrivacySettings.REAL) {
                    output = "";
                    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, output);
                } else if (tmpOutHash == hashCode(intent)) {
                    // if this intent was stored before, get the real value since it could have been modified
                    output = tmpOut.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, output);
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            
            if (tmpOutReceivers > 1) {
                tmpOutReceivers--;
            } else { // free memory after all receivers have been served
                tmpOut = null;
                tmpOutHash = 0;
            }
            
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        // incoming call
        } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) && 
                intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            pSet = pSetMan.getSettings(packageName, uid);
            output = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            
            if (tmpInHash != hashCode(intent)) {
                tmpIn = (Intent)intent.clone();
                tmpInHash = hashCode(intent);
                tmpInReceivers = receivers;
            }
            
            try {
                if (pSet != null && pSet.getIncomingCallsSetting() != PrivacySettings.REAL) {
                    output = "";
                    intent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, output);
                } else if (tmpInHash == hashCode(intent)) {
                    output = tmpIn.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    intent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, output);
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            
            if (tmpInReceivers > 1) {
                tmpInReceivers--;
            } else { // free memory after all receivers have been served
                tmpIn = null;
                tmpInHash = 0;
            }
            
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        // incoming SMS
        } else if (action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            pSet = pSetMan.getSettings(packageName, uid);
            output = "[real]";
            Log.d(TAG, "package: " + packageName + " uid: " + uid);
            
            Object[] o = ((Object[])intent.getSerializableExtra("pdus"));
            byte[] b = o != null ? (byte[])o[0] : null;
            
            if (tmpSmsHash != hashCode(intent)) {
                tmpSms = (Intent)intent.clone();
                tmpSmsHash = hashCode(intent);
                tmpSmsReceivers = receivers;
                Log.d(TAG, "new intent; saving copy: receivers: " + receivers + " hash: " + tmpSmsHash + " " + 
                        "pdu number: " + (o != null ? o.length : "null") + " " + 
                        "1st pdu length: " + (b != null ? b.length : "null"));
            } else {
                Log.d(TAG, "known intent; hash: " + hashCode(intent) + " remaining receivers: " + tmpSmsReceivers);
            }
            
            try {
                if (pSet != null && pSet.getSmsSetting() != PrivacySettings.REAL) {
                    output = "[empty]";
                    
                    Object[] emptypdusObj = new Object[1];
                    emptypdusObj[0] = (Object) new byte[] {0,32,1,-127,-16,0,0,17,-112,1,48,34,34,-128,1,32};
                    intent.putExtra("pdus", emptypdusObj);
                    
                    Log.d(TAG, "permission denied, replaced pdu; pdu number: " + 
                            (o != null ? o.length : "null") + " " +
                        "1st pdu length:" + (b != null ? b.length : "null"));
                } else if (tmpSmsHash == hashCode(intent)) {
                    intent.putExtra("pdus", tmpSms.getSerializableExtra("pdus"));
                    
                    o = ((Object[])intent.getSerializableExtra("pdus"));
                    b = o != null ? (byte[])o[0] : null;
                    Log.d(TAG, "permission granted, inserting saved pdus; pdu number: " + 
                            (o != null ? o.length : "null") + " " +
                            "1st pdu length:" + (b != null ? b.length : "null"));
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            
            if (tmpSmsReceivers > 1) {
                tmpSmsReceivers--;
            } else { // free memory after all receivers have been served
                Log.d(TAG, "removing intent with hash: " + tmpSmsHash);
                tmpSms = null;
                tmpSmsHash = 0;
            }            
            
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        // incoming MMS
        } else if (action.equals(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION) ||
                action.equals(Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION)) {
            pSet = pSetMan.getSettings(packageName, uid);
            output = "[real]";
            
            Object[] o = ((Object[])intent.getSerializableExtra("pdus"));
            byte[] b = o != null ? (byte[])o[0] : null;
            
            if (tmpMmsHash != hashCode(intent)) {
                tmpMms = (Intent)intent.clone();
                tmpMmsHash = hashCode(intent);
                tmpMmsReceivers = receivers;
                Log.d(TAG, "new intent; saving copy: receivers: " + receivers + " hash: " + tmpMmsHash + " " + 
                        "pdu number: " + (o != null ? o.length : "null") + " " + 
                        "1st pdu length: " + (b != null ? b.length : "null"));
            } else {
                Log.d(TAG, "known intent; hash: " + hashCode(intent) + " remaining receivers: " + tmpMmsReceivers);
            }
            
            try {
                if (pSet != null && pSet.getMmsSetting() != PrivacySettings.REAL) {
                    output = "[empty]";
                    
                    Object[] emptypdusObj = new Object[1];
                    emptypdusObj[0] = (Object) new byte[] {0,32,1,-127,-16,0,0,17,-112,1,48,34,34,-128,1,32};
                    intent.putExtra("pdus", emptypdusObj);
                } else if (tmpMmsHash == hashCode(intent)) {
                    intent.putExtra("pdus", tmpMms.getSerializableExtra("pdus"));
                    
                    o = ((Object[])intent.getSerializableExtra("pdus"));
                    b = o != null ? (byte[])o[0] : null;
                    Log.d(TAG, "permission granted, inserting saved pdus; pdu number: " + 
                            (o != null ? o.length : "null") + " " +
                            "1st pdu length:" + (b != null ? b.length : "null"));
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            
            if (tmpMmsReceivers > 1) {
                tmpMmsReceivers--;
            } else { // free memory after all receivers have been served
                Log.d(TAG, "removing intent with hash: " + tmpMmsHash);
                tmpMms = null;
                tmpMmsHash = 0;
            }
            
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        }
    }
    
    private static long hashCode(Intent intent) {
        long privacyHash = intent.getLongExtra("privacy_hash", 0);
        if (privacyHash == 0) {
            privacyHash = intent.filterHashCode() + System.currentTimeMillis();
            intent.putExtra("privacy_hash", privacyHash);
        }
        return privacyHash;
    }
}
