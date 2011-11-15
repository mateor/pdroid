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
 * Provides privacy handling for {@link android.content.ContentResolver}
 * @author Anonymous 
 */
public final class PrivacyContentResolver {
    
    private static final String TAG = "PrivacyContentResolver";
    
    private static PrivacySettingsManager pSetMan;
    
    /**
     * Returns a dummy database cursor if access is restricted by privacy settings
     * @param uri
     * @param context
     * @param realCursor
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
            if (auth != null) {
                if (auth.equals(android.provider.Contacts.AUTHORITY) ||
                        auth.equals(ContactsContract.AUTHORITY)) {
                    
                    if (pSet != null && pSet.getContactsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_CONTACTS, null);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_CONTACTS, null);
                    }
                    
                } else if (auth.equals(Calendar.AUTHORITY)) {
                    
                    if (pSet != null && pSet.getCalendarSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_CALENDAR, null);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_CALENDAR, null);
                    }
                    
                } else if (auth.equals(Mms.CONTENT_URI.getAuthority())) {
                    
                    if (pSet != null && pSet.getMmsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_MMS, null);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_MMS, null);
                    }
                    
                } else if (auth.equals(Sms.CONTENT_URI.getAuthority())) {
                    
                    if (pSet != null && pSet.getSmsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_SMS, null);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_SMS, null);
                    }
                // all messages, sms and mms
                } else if (auth.equals(MmsSms.CONTENT_URI.getAuthority()) || 
                        auth.equals("mms-sms-v2") /* htc specific, accessed by system messages application */) { 
                    
                    // deny access if access to either sms, mms or both is restricted by privacy settings
                    if (pSet != null && (pSet.getMmsSetting() == PrivacySettings.EMPTY || 
                            pSet.getSmsSetting() == PrivacySettings.EMPTY)) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_MMS_SMS, null);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_MMS_SMS, null);
                    }

                } else if (auth.equals(CallLog.AUTHORITY)) {
                    
                    if (pSet != null && pSet.getCallLogSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_CALL_LOG, null);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_CALL_LOG, null);
                    }

                } else if (auth.equals(Browser.BOOKMARKS_URI.getAuthority())) {
                    
                    if (pSet != null && pSet.getBookmarksSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_BOOKMARKS, null);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_BOOKMARKS, null);
                    }
                    
                }
            }
//            Log.d(TAG, "query - " + packageName + " (" + uid + ") auth: " + auth + " output: " + output_label);
            return output;
        }
        return realCursor;
    }
}
