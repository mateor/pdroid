package android.privacy.surrogate;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.provider.Browser;
import android.provider.Calendar;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings.Bookmarks;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;

import java.util.Map;

/**
 * Provides privacy handling for the {@link android.content.ContentResolver} class
 */
public final class PrivacyContentResolver {
    
    private final static String TAG = "PrivacyActivityManagerService";
    
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
            if (pSet != null && auth != null) {
                if (auth.equals(android.provider.Contacts.AUTHORITY) ||
                        auth.equals(ContactsContract.AUTHORITY)) {
                    
                    if (pSet.getContactsSetting() == PrivacySettings.EMPTY) return new PrivacyCursor();
                    else return realCursor;
                    
                } else if (auth.equals(Calendar.AUTHORITY)) {
                    
                    if (pSet.getCalendarSetting() == PrivacySettings.EMPTY) return new PrivacyCursor();
                    else return realCursor;
                    
                } else if (auth.equals(Mms.CONTENT_URI.toString())) {
                    
                    if (pSet.getMmsSetting() == PrivacySettings.EMPTY) return new PrivacyCursor();
                    else return realCursor;
                    
                } else if (auth.equals(Sms.CONTENT_URI.toString())) {
                    
                    if (pSet.getSmsSetting() == PrivacySettings.EMPTY) return new PrivacyCursor();
                    else return realCursor;
                    
                } else if (auth.equals(MmsSms.CONTENT_URI.toString())) { // all messages, sms and mms
                    
                    // deny access if access to either sms, mms or both is restricted by privacy settings
                    if (pSet.getMmsSetting() == PrivacySettings.EMPTY || 
                            pSet.getSmsSetting() == PrivacySettings.EMPTY) return new PrivacyCursor();
                    else return realCursor;
                    
                } else if (auth.equals(CallLog.AUTHORITY)) {
                    
                    if (pSet.getCallLogSetting() == PrivacySettings.EMPTY) return new PrivacyCursor();
                    else return realCursor;
                    
                } else if (auth.equals(Browser.BOOKMARKS_URI.toString())) {
                    
                    if (pSet.getBookmarksSetting() == PrivacySettings.EMPTY) return new PrivacyCursor();
                    else return realCursor;
                    
                }
            }
        }
        return realCursor;
    }
}
