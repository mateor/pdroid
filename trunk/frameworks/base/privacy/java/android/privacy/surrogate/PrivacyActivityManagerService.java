package android.privacy.surrogate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Does not extend but provides according privacy methods
 */
public final class PrivacyActivityManagerService /** extends ActivityManagerService **/ {
    
    private final static String TAG = "PrivacyActivityManagerService";
    
    private static PrivacySettingsManager pSetMan;
    
    /**
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
                e.printStackTrace();
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
                e.printStackTrace();
            }
            Log.d(TAG, "broadcasting intent " + action + " - " + packageName + " (" + uid + ") output: " + output);
        }
    }
}
