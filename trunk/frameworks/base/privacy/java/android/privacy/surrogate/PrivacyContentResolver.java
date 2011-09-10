package android.privacy.surrogate;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.provider.Browser;
import android.provider.Calendar;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.util.Log;

/**
 * Provides privacy handling for the {@link android.content.ContentResolver} class
 */
public final class PrivacyContentResolver {
    
    private final static String TAG = "PrivacyContentResolver";
    
    private static PrivacySettingsManager pSetMan;
    
    /**
     * Returns a dummy database cursor if access is restricted by privacy settings
     * @param uri
     * @param context
     */
    public static Cursor enforcePrivacyPermission(Uri uri, Context context, Cursor realCursor) {
        if (uri != null) {
            if (pSetMan == null) pSetMan = (PrivacySettingsManager) context.getSystemService("privacy");
            String packageName = context.getPackageName();
            int uid = Binder.getCallingUid();
            PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
            String auth = uri.getAuthority();
            String output_label = "[real]";
            Cursor output = realCursor;
            if (pSet != null && auth != null) {
                if (auth.equals(android.provider.Contacts.AUTHORITY) ||
                        auth.equals(ContactsContract.AUTHORITY)) {
                    
                    if (pSet.getContactsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                    }
                    
                } else if (auth.equals(Calendar.AUTHORITY)) {
                    
                    if (pSet.getCalendarSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                    }
                    
                } else if (auth.equals(Mms.CONTENT_URI.getAuthority())) {
                    
                    if (pSet.getMmsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                    }
                    
                } else if (auth.equals(Sms.CONTENT_URI.getAuthority())) {
                    
                    if (pSet.getSmsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";                      
                        output = new PrivacyCursor();
                    }
                // all messages, sms and mms
                } else if (auth.equals(MmsSms.CONTENT_URI.getAuthority()) || 
                        auth.equals("mms-sms-v2") /* htc specific, accessed by system messages application */) { 
                    
                    // deny access if access to either sms, mms or both is restricted by privacy settings
                    if (pSet.getMmsSetting() == PrivacySettings.EMPTY || 
                            pSet.getSmsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                    }

                } else if (auth.equals(CallLog.AUTHORITY)) {
                    
                    if (pSet.getCallLogSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                    } 

                } else if (auth.equals(Browser.BOOKMARKS_URI.getAuthority())) {
                    
                    if (pSet.getBookmarksSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                    }
                    
                }
            }
            Log.d(TAG, "query - " + packageName + " (" + uid + ") auth: " + auth + " output: " + output_label);
            return output;
        }
        return realCursor;
    }
}
