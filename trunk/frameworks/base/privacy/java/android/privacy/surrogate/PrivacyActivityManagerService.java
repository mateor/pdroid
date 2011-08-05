package android.privacy.surrogate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Provides privacy methods for ActivityManagerService
 */
public final class PrivacyActivityManagerService {
    
    private final static String TAG = "PrivacyActivityManagerService";
    
    private static PrivacySettingsManager pSetMan;
    
    /**
     * Intercepts broadcasts and replaces the broadcast contents according to 
     * privacy permissions
     * @param packageName may not be null
     * @param uid must be >= 0
     * @param intent intent.getAction() may not return null
     */
    public static void enforcePrivacyPermission(String packageName, int uid, Intent intent, Context context) {
        if (pSetMan == null) pSetMan = (PrivacySettingsManager) context.getSystemService("privacy");
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        String action = intent.getAction();
        String output;
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            output = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            try {
                if (pSet != null && pSet.getOutgoingCallsSetting() != PrivacySettings.REAL) {
                    output = "";
                    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, output);
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            output = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            try {
                if (pSet != null && pSet.getIncomingCallsSetting() != PrivacySettings.REAL) {
                    output = "";
                    intent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, output);
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        } else if (action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            output = "[real]";
            try {
                if (pSet != null && pSet.getSmsSetting() != PrivacySettings.REAL) {
                    output = "[empty]";
                    intent.putExtras(new Bundle());
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        } else if (action.equals(Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION)) {
            output = "[real]";
            try {
                if (pSet != null && pSet.getMmsSetting() != PrivacySettings.REAL) {
                    output = "[empty]";
                    intent.putExtras(new Bundle());
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to enforce intent broadcast permission", e);
            }
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        }
    }
}
