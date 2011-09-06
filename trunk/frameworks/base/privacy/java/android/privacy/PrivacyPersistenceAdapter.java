
package android.privacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Binder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class PrivacyPersistenceAdapter {

    private static final String TAG = "PrivacyPersistenceAdapter";

    private static final String DATABASE_NAME = "/data/system/privacy.db";
    
    /**
     * Used to save settings for access from core libraries
     */
    private static final String SETTINGS_DIRECTORY = "/data/system/privacy";

    private static final String DATABASE_TABLE = "settings";

    private static final String DATABASE_CREATE = 
            "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ( " + 
            " _id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            " packageName TEXT, " + 
            " uid INTEGER, " + 
            " deviceIdSetting INTEGER, " + 
            " deviceId TEXT, " + 
            " line1NumberSetting INTEGER, " + 
            " line1Number TEXT, " + 
            " locationGpsSetting INTEGER, " + 
            " locationGpsLat TEXT, " + 
            " locationGpsLon TEXT, " + 
            " locationNetworkSetting INTEGER, " + 
            " locationNetworkLat TEXT, " + 
            " locationNetworkLon TEXT, " + 
            " networkInfoSetting INTEGER, " + 
            " simInfoSetting INTEGER, " + 
            " simSerialNumberSetting INTEGER, " + 
            " simSerialNumber TEXT, " + 
            " subscriberIdSetting INTEGER, " + 
            " subscriberId TEXT, " + 
            " accountsSetting INTEGER, " + 
            " accountsAuthTokensSetting INTEGER, " + 
            " outgoingCallsSetting INTEGER, " + 
            " incomingCallsSetting INTEGER, " + 
            " contactsSetting INTEGER, " + 
            " calendarSetting INTEGER, " + 
            " mmsSetting INTEGER, " + 
            " smsSetting INTEGER, " + 
            " callLogSetting INTEGER, " + 
            " bookmarksSetting INTEGER, " + 
            " systemLogsSetting INTEGER, " + 
            " externalStorageSetting INTEGER, " + 
            " cameraSetting INTEGER, " + 
            " recordAudioSetting INTEGER" + 
            ");";
    
    private static final String[] DATABASE_FIELDS = new String[] { "_id", "packageName", "uid", 
        "deviceIdSetting", "deviceId", "line1NumberSetting", "line1Number", "locationGpsSetting", 
        "locationGpsLat", "locationGpsLon", "locationNetworkSetting", "locationNetworkLat", 
        "locationNetworkLon", "networkInfoSetting", "simInfoSetting", "simSerialNumberSetting", 
        "simSerialNumber", "subscriberIdSetting", "subscriberId", "accountsSetting", "accountsAuthTokensSetting", 
        "outgoingCallsSetting", "incomingCallsSetting", "contactsSetting", "calendarSetting", 
        "mmsSetting", "smsSetting", "callLogSetting", "bookmarksSetting", 
        "systemLogsSetting", "externalStorageSetting", "cameraSetting", "recordAudioSetting" };

    private SQLiteDatabase db;

    public PrivacyPersistenceAdapter(Context context) {
        // check write permission for /data/system/
        boolean canWrite = new File("/data/system/").canWrite();
        Log.d(TAG, "Constructing " + TAG + " for package: " +  context.getPackageName() + 
                " UID: " + Binder.getCallingUid() + "; Write permission for /data/system/: " + canWrite);
        // create the database and settings directory if we have write permission and they do not exist
        if (canWrite) {
            if (!new File(DATABASE_NAME).exists()) createDatabase();
            if (!new File(SETTINGS_DIRECTORY).exists()) createSettingsDir();
        }
    }

    public synchronized PrivacySettings getSettings(String packageName, int uid) {
        // TODO: remove synchronized and only close if other threads do not access db 
        Log.d(TAG, "getSettings: settings request for package: " + packageName + " UID: " + uid);
        PrivacySettings s = null;
        
        if (packageName == null) {
            Log.e(TAG, "getSettings: insufficient application identifier - package name is required");
            return s;
        }
        
        SQLiteDatabase db;
        try {
            db = getReadableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "getSettings: database could not be opened");
            return null;
        }
        
        Cursor c = null;

        try {
            // try to get settings based on package name only first; some system applications
            // (e.g., HTC Settings) run with a different UID than reported by package manager
            c = db.query(DATABASE_TABLE, DATABASE_FIELDS, "packageName=?", new String[] { packageName }, null, null, null);

            if (c != null) {
                if (c.getCount() > 1) {
                    // if we get multiple entries, try using UID as well; not guaranteed to find existing
                    // settings for system applications (see above comment) but this is rather rare
                    Log.d(TAG, "getSettings: multiple settings entries found for package name: " + packageName
                            + "; trying with UID: " + uid);
                    
                    c = db.query(DATABASE_TABLE, DATABASE_FIELDS, 
                            "packageName=? AND uid=?", new String[] { packageName, uid + "" }, null, null, null);
                }
                if (c.getCount() == 1 && c.moveToFirst()) {
                    s = new PrivacySettings(c.getInt(0), c.getString(1), c.getInt(2), (byte)c.getShort(3), c.getString(4), 
                            (byte)c.getShort(5), c.getString(6), (byte)c.getShort(7), c.getString(8), c.getString(9), (byte)c.getShort(10), 
                            c.getString(11), c.getString(12), (byte)c.getShort(13), (byte)c.getShort(14), (byte)c.getShort(15), 
                            c.getString(16), (byte)c.getShort(17), c.getString(18), (byte)c.getShort(19), (byte)c.getShort(20), 
                            (byte)c.getShort(21), (byte)c.getShort(22), (byte)c.getShort(23), (byte)c.getShort(24), (byte)c.getShort(25), 
                            (byte)c.getShort(26), (byte)c.getShort(27), (byte)c.getShort(28), (byte)c.getShort(29), (byte)c.getShort(30), 
                            (byte)c.getShort(31), (byte)c.getShort(32));
                    Log.d(TAG, "getSettings: found settings entry for package: " + packageName + " UID: " + uid);
                } else if (c.getCount() > 1) {
                    // multiple settings entries have same package name AND UID, this should NEVER happen
                    Log.e(TAG, "getSettings: duplicate entries in the privacy.db");
                    // if it does happen, null will be returned, since we cannot be sure what setting to use
                }
            } else {
                Log.d(TAG, "getSettings: no settings found for package: " + packageName + " UID: " + uid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }
        
        Log.d(TAG, "getSettings: returning settings: " + s);
        return s;
    }
    
    /**
     * Saves the settings object fields into DB and into plain text files where applicable. 
     * The DB changes will not be made persistent if saving settings to plain text files
     * fails.
     * @param s settings object
     * @return true if settings were saved successfully, false otherwise
     */
    public synchronized boolean saveSettings(PrivacySettings s) {
        boolean result = true;
        String packageName = s.getPackageName();
        Integer uid = s.getUid();
        Log.d(TAG, "saveSettings: settings save request : " + s);
        
        if (packageName == null || packageName.isEmpty() || uid == null) {
            Log.e(TAG, "Either package name, UID or both is missing.");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("packageName", packageName);
        values.put("uid", uid);
        
        values.put("deviceIdSetting", s.getDeviceIdSetting());
        values.put("deviceId", s.getDeviceId());
        
        values.put("line1NumberSetting", s.getLine1NumberSetting());
        values.put("line1Number", s.getLine1Number());
        
        values.put("locationGpsSetting", s.getLocationGpsSetting());
        values.put("locationGpsLat", s.getLocationGpsLat());
        values.put("locationGpsLon", s.getLocationGpsLon());
        
        values.put("locationNetworkSetting", s.getLocationNetworkSetting());
        values.put("locationNetworkLat", s.getLocationNetworkLat());
        values.put("locationNetworkLon", s.getLocationNetworkLon());
        
        values.put("networkInfoSetting", s.getNetworkInfoSetting());        
        values.put("simInfoSetting", s.getSimInfoSetting());
        
        values.put("simSerialNumberSetting", s.getSimSerialNumberSetting());        
        values.put("simSerialNumber", s.getSimSerialNumber());
        values.put("subscriberIdSetting", s.getSubscriberIdSetting());        
        values.put("subscriberId", s.getSubscriberId());
        
        values.put("accountsSetting", s.getAccountsSetting());
        values.put("accountsAuthTokensSetting", s.getAccountsAuthTokensSetting());
        values.put("outgoingCallsSetting", s.getOutgoingCallsSetting());
        values.put("incomingCallsSetting", s.getIncomingCallsSetting());
        
        values.put("contactsSetting", s.getContactsSetting());
        values.put("calendarSetting", s.getCalendarSetting());
        values.put("mmsSetting", s.getMmsSetting());
        values.put("smsSetting", s.getSmsSetting());
        values.put("callLogSetting", s.getCallLogSetting());
        values.put("bookmarksSetting", s.getBookmarksSetting());
        values.put("systemLogsSetting", s.getSystemLogsSetting());
        values.put("externalStorageSetting", s.getExternalStorageSetting());
        values.put("cameraSetting", s.getCameraSetting());
        values.put("recordAudioSetting", s.getRecordAudioSetting());
        
        
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // make sure this ends up in a consistent state (DB and plain text files)
        Cursor c = null;
        try {
            // save settings to the DB
            Log.d(TAG, "saveSettings: checking if entry exists already");
            if (s.get_id() != null) { // entry exists -> update

                Log.d(TAG, "saveSettings: updating existing entry");
                db.update(DATABASE_TABLE, values, "_id=?", new String[] { s.get_id().toString() });

            } else { // new entry -> insert if no duplicates exist

                Log.d(TAG, "saveSettings: new entry; verifying if duplicates exist");
                c = db.query(DATABASE_TABLE, new String[] { "_id" }, "packageName=? AND uid=?", 
                        new String[] { s.getPackageName(), s.getUid() + "" }, null, null, null);
                
                if (c != null) {
                    if (c.getCount() == 1) { // exactly one entry
                        // exists -> update
                        Log.d(TAG, "saveSettings: updating existing entry");
                        db.update(DATABASE_TABLE, values, "packageName=? AND uid=?", 
                                new String[] { s.getPackageName(), s.getUid() + "" });
                    } else if (c.getCount() == 0) { // no entries -> insert
                        Log.d(TAG, "saveSettings: inserting new entry");
                        db.insert(DATABASE_TABLE, null, values);
                    } else { // something went totally wrong and there are multiple entries for same identifier
                        result = false;
                        Log.e(TAG, "saveSettings: duplicate entries in the privacy.db");
                    }
                } else {
                    result = false;
                    // jump to catch block to avoid marking transaction as successful
                    throw new Exception("saveSettings: database access failed");
                }
            }
            
            // save settings to plain text file (for access from core libraries)
            File settingsDir = new File("/data/system/privacy/" + packageName + "/" + uid + "/");
            File settingsPackageDir = new File("/data/system/privacy/" + packageName + "/");
            File systemLogsSettingFile = new File("/data/system/privacy/" + packageName + "/" + 
                    uid + "/systemLogsSetting");
            File externalStorageSettingFile = new File("/data/system/privacy/" + packageName + "/" + 
                    uid + "/externalStorageSetting");
            File cameraSettingFile = new File("/data/system/privacy/" + packageName + "/" + 
                    uid + "/cameraSetting");
            File recordAudioSettingFile = new File("/data/system/privacy/" + packageName + "/" + 
                    uid + "/recordAudioSetting");
            try {
                // create all parent directories on the file path
                settingsDir.mkdirs();
                // make the directory readable (requires it to be executable as well)
                settingsDir.setReadable(true, false);
                settingsDir.setExecutable(true, false);
                // make the parent directory readable (requires it to be executable as well)
                settingsPackageDir.setReadable(true, false);
                settingsPackageDir.setExecutable(true, false);
                // create the setting files and make them readable
                systemLogsSettingFile.createNewFile();
                systemLogsSettingFile.setReadable(true, false);
                externalStorageSettingFile.createNewFile();
//                externalStorageSettingFile.setReadable(true, false);
//                cameraSettingFile.createNewFile();
//                cameraSettingFile.setReadable(true, false);
//                recordAudioSettingFile.createNewFile();
//                recordAudioSettingFile.setReadable(true, false);
                // write settings to files
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(systemLogsSettingFile));
                writer.append(s.getSystemLogsSetting() + "");
                writer.flush();
                writer.close();
//                writer = new OutputStreamWriter(new FileOutputStream(externalStorageSettingFile));
//                writer.append(s.getExternalStorageSetting() + "");
//                writer.flush();
//                writer.close();
//                writer = new OutputStreamWriter(new FileOutputStream(cameraSettingFile));
//                writer.append(s.getCameraSetting() + "");
//                writer.flush();
//                writer.close();
//                writer = new OutputStreamWriter(new FileOutputStream(recordAudioSettingFile));
//                writer.append(s.getRecordAudioSetting() + "");
//                writer.flush();
//                writer.close();
            } catch (IOException e) {
                // TODO: roll back changes made to plain text files
                result = false;
                // jump to catch block to avoid marking transaction as successful
                throw new Exception("saveSettings: could not write settings to file"); 
            }
            // mark DB transaction successful (commit the changes)
            db.setTransactionSuccessful();
        } catch (Exception e) {
            result = false;
            Log.e(TAG, "saveSettings: could not save settings", e);
        } finally {
            db.endTransaction();
            if (c != null) c.close();
            if (db != null) db.close();
        }

        return result;
    }
    
    private synchronized void createDatabase() {
        Log.d(TAG, "createDatabase: Creating privacy.db in /data/system");
        SQLiteDatabase db = 
            SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE | 
                    SQLiteDatabase.CREATE_IF_NECESSARY);
        Log.d(TAG, "createDatabase: Executing database create statement on privacy.db");
        db.execSQL(DATABASE_CREATE);
        Log.d(TAG, "createDatabase: Closing connection to privacy.db");
        db.close();
    }
    
    private synchronized void createSettingsDir() {
        // create settings directory (for settings accessed from core libraries)
        File settingsDir = new File("/data/system/privacy/");
        settingsDir.mkdirs();
        settingsDir.setReadable(true, false); // make it readable for everybody
        // for some reason reading the files only works if it is executable
        settingsDir.setExecutable(true, false);    
    }
    
    private synchronized SQLiteDatabase getReadableDatabase() {
        if (db != null && db.isOpen() && db.isReadOnly()) return db;

        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
        this.db = db;

        return this.db;
    }

    private synchronized SQLiteDatabase getWritableDatabase() {
        // create the database if it does not exist
        if (!new File(DATABASE_NAME).exists()) createDatabase();
        
        if (db != null && db.isOpen() && !db.isReadOnly()) return db;
        
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        this.db = db;

        return this.db;
    }
}