package android.privacy.surrogate;

import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
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
    public static Cursor enforcePrivacyPermission(IContentProvider provider, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, Context context, Cursor realCursor) throws RemoteException {
//    public static Cursor enforcePrivacyPermission(Uri uri, Context context, Cursor realCursor) {
        if (uri != null) {
            if (pSetMan == null) pSetMan = (PrivacySettingsManager) context.getSystemService("privacy");
            String packageName = context.getPackageName();
            int uid = Binder.getCallingUid();
            PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
            String auth = uri.getAuthority();
            String output_label = "[real]";
            Cursor output = realCursor;
            if (auth != null) {
                if (auth.equals(android.provider.Contacts.AUTHORITY) || auth.equals(ContactsContract.AUTHORITY)) {

                    if (pSet != null) {
                        if (pSet.getContactsSetting() == PrivacySettings.EMPTY) {
                            output_label = "[empty]";
                            output = new PrivacyCursor();
                            pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_CONTACTS, null, pSet);
                        } else if (pSet.getContactsSetting() == PrivacySettings.CUSTOM && 
                                uri.toString().contains(ContactsContract.Contacts.CONTENT_URI.toString())) {
//                            Log.d(TAG, "enforcePrivacyPermission - URI: " + uri.toString() + " " + uri.getAuthority() + " " + uri.getEncodedAuthority() + " " + uri.getEncodedFragment() + " " + uri.getEncodedPath() + " " + uri.getEncodedQuery() + " " + uri.getEncodedSchemeSpecificPart() + " " + uri.getEncodedUserInfo() + " " + uri.getFragment() + " " + uri.getPath());
//                            Log.d(TAG, "enforcePrivacyPermission - projection: " + arrayToString(projection) + " selection: " + selection + " selectionArgs: " + arrayToString(selectionArgs));
//                            Log.d(TAG, "enforcePrivacyPermission - cursor entries: " + output.getCount());
                            
                            boolean idFound = false;
                            if (projection != null) {
                                for (String p : projection) {
                                    if (p.equals(ContactsContract.Contacts._ID)) {
                                        idFound = true;
                                        break;
                                    }
                                }
                                
                                if (!idFound) { // add ID to projection
                                    String[] newProjection = new String[projection.length + 1];
                                    System.arraycopy(projection, 0, newProjection, 0, projection.length);
                                    newProjection[projection.length] = ContactsContract.Contacts._ID;
                                    projection = newProjection;
                                }
                            }
                            
//                            Log.d(TAG, "enforcePrivacyPermission - new projection: " + arrayToString(projection) + " selection: " + selection + " selectionArgs: " + arrayToString(selectionArgs));
                            
                            // re-query
                            output = provider.query(uri, projection, selection, selectionArgs, sortOrder);
//                            Log.d(TAG, "enforcePrivacyPermission - new cursor entries: " + output.getCount());
                            output = new PrivacyCursor(output, pSet.getAllowedContacts());
                            pSetMan.notification(packageName, uid, PrivacySettings.CUSTOM, PrivacySettings.DATA_CONTACTS, null, pSet);
                        } else { // REAL
                            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_CONTACTS, null, pSet);
                        }
                    }
                    
                } else if (auth.equals(Calendar.AUTHORITY)) {
                    
                    if (pSet != null && pSet.getCalendarSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_CALENDAR, null, pSet);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_CALENDAR, null, pSet);
                    }
                    
                } else if (auth.equals(Mms.CONTENT_URI.getAuthority())) {
                    
                    if (pSet != null && pSet.getMmsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_MMS, null, pSet);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_MMS, null, pSet);
                    }
                    
                } else if (auth.equals(Sms.CONTENT_URI.getAuthority())) {
                    
                    if (pSet != null && pSet.getSmsSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_SMS, null, pSet);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_SMS, null, pSet);
                    }
                // all messages, sms and mms
                } else if (auth.equals(MmsSms.CONTENT_URI.getAuthority()) || 
                        auth.equals("mms-sms-v2") /* htc specific, accessed by system messages application */) { 
                    
                    // deny access if access to either sms, mms or both is restricted by privacy settings
                    if (pSet != null && (pSet.getMmsSetting() == PrivacySettings.EMPTY || 
                            pSet.getSmsSetting() == PrivacySettings.EMPTY)) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_MMS_SMS, null, pSet);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_MMS_SMS, null, pSet);
                    }

                } else if (auth.equals(CallLog.AUTHORITY)) {
                    
                    if (pSet != null && pSet.getCallLogSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_CALL_LOG, null, pSet);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_CALL_LOG, null, pSet);
                    }

                } else if (auth.equals(Browser.BOOKMARKS_URI.getAuthority())) {
                    
                    if (pSet != null && pSet.getBookmarksSetting() == PrivacySettings.EMPTY) {
                        output_label = "[empty]";
                        output = new PrivacyCursor();
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_BOOKMARKS, null, pSet);
                    } else {
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_BOOKMARKS, null, pSet);
                    }
                    
                }
            }
//            Log.d(TAG, "query - " + packageName + " (" + uid + ") auth: " + auth + " output: " + output_label);
            return output;
        }
        return realCursor;
    }
    
    private static String arrayToString(String[] array) {
        StringBuffer sb = new StringBuffer();
        if (array != null) for (String bla : array) sb.append("[" + bla + "]");
        else return "";
        return sb.toString();
    }
}
