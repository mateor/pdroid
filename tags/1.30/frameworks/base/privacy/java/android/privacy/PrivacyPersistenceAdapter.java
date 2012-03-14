package android.privacy;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.FileUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for persisting privacy settings to built-in memory
 * @author Anonymous
 */
public class PrivacyPersistenceAdapter {

    private static final String TAG = "PrivacyPersistenceAdapter";
    
    private static final int RETRY_QUERY_COUNT = 5;

    private static final String DATABASE_FILE = "/data/system/privacy.db";
    
    private static final int DATABASE_VERSION = 3;
    
    public static final int DUMMY_UID = -1;
    
    /**
     * Number of threads currently reading the database
     */
    public static Integer readingThreads = 0;
    
    /**
     * Used to save settings for access from core libraries
     */
    public static final String SETTINGS_DIRECTORY = "/data/system/privacy";

    private static final String TABLE_SETTINGS = "settings";
    
    private static final String TABLE_MAP = "map";
    
    private static final String TABLE_ALLOWED_CONTACTS = "allowed_contacts";
    
    private static final String TABLE_VERSION = "version";
    
    private static final String CREATE_TABLE_SETTINGS = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " ( " + 
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
        " recordAudioSetting INTEGER, " + 
        " notificationSetting INTEGER, " + 
        " intentBootCompletedSetting INTEGER" + 
        ");";
    
    private static final String CREATE_TABLE_MAP = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_MAP + " ( name TEXT PRIMARY KEY, value TEXT );";
    
    private static final String CREATE_TABLE_ALLOWED_CONTACTS = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_ALLOWED_CONTACTS + " ( settings_id, contact_id, PRIMARY KEY(settings_id, contact_id) );";
    
    private static final String INSERT_VERSION = 
        "INSERT OR REPLACE INTO " + TABLE_MAP + " (name, value) " + "VALUES (\"db_version\", " + DATABASE_VERSION + ");";
    
    private static final String INSERT_ENABLED = 
        "INSERT OR REPLACE INTO " + TABLE_MAP + " (name, value) " + "VALUES (\"enabled\", \"1\");";
    
    private static final String INSERT_NOTIFICATIONS_ENABLED = 
        "INSERT OR REPLACE INTO " + TABLE_MAP + " (name, value) " + "VALUES (\"notifications_enabled\", \"1\");";
    
    private static final String[] DATABASE_FIELDS = new String[] { "_id", "packageName", "uid", 
        "deviceIdSetting", "deviceId", "line1NumberSetting", "line1Number", "locationGpsSetting", 
        "locationGpsLat", "locationGpsLon", "locationNetworkSetting", "locationNetworkLat", 
        "locationNetworkLon", "networkInfoSetting", "simInfoSetting", "simSerialNumberSetting", 
        "simSerialNumber", "subscriberIdSetting", "subscriberId", "accountsSetting", "accountsAuthTokensSetting", 
        "outgoingCallsSetting", "incomingCallsSetting", "contactsSetting", "calendarSetting", 
        "mmsSetting", "smsSetting", "callLogSetting", "bookmarksSetting", "systemLogsSetting", 
        "externalStorageSetting", "cameraSetting", "recordAudioSetting", "notificationSetting", 
        "intentBootCompletedSetting" };
    
    public static final String SETTING_ENABLED = "enabled";
    public static final String SETTING_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String SETTING_DB_VERSION = "db_version";
    public static final String VALUE_TRUE = "1";
    public static final String VALUE_FALSE = "0";

    private SQLiteDatabase db;
    
    private Context context;

    public PrivacyPersistenceAdapter(Context context) {
        this.context = context;
        // check write permission for /data/system/
        boolean canWrite = new File("/data/system/").canWrite();
//        Log.d(TAG, "Constructing " + TAG + " for package: " +  context.getPackageName() + 
//                " UID: " + Binder.getCallingUid() + "; Write permission for /data/system/: " + canWrite);
        // create the database and settings directory if we have write permission and they do not exist
        if (canWrite) {
            if (!new File(DATABASE_FILE).exists()) createDatabase();
            if (!new File(SETTINGS_DIRECTORY).exists()) createSettingsDir();
            // upgrade if needed
            int currentVersion = getDbVersion();
//            Log.d(TAG, "PrivacyPersistenceAdapter - current DB version: " + currentVersion);
            if (currentVersion < DATABASE_VERSION) upgradeDatabase(currentVersion);
        }
    }

    private synchronized void upgradeDatabase(int currentVersion) {
        Log.d(TAG, "upgradeDatabase - upgrading DB from version " + currentVersion + " to " + DATABASE_VERSION);
        
        // backup current database file
        File dbFile = new File(DATABASE_FILE);
        File dbBackupFile = new File(DATABASE_FILE + ".bak");
        // remove old backup
        try {
            dbBackupFile.delete();
        } catch (SecurityException e) {
            Log.w(TAG, "upgradeDatabase - could not remove old backup");
        }
        // backup current DB file
        FileUtils.copyFile(dbFile, dbBackupFile);
        // make sure a backup was created
        if (System.currentTimeMillis() - dbBackupFile.lastModified() > 2000) {
            Log.e(TAG, "upgradeDatabase - could not create a database backup, aborting...");
            return;
        }
        
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        
        switch (currentVersion) {
            case 1:
            case 2:
                try {
                    if (db != null && db.isOpen()) {
//                        db.execSQL("ALTER TABLE " + TABLE_SETTINGS + " ADD COLUMN intentBootCompletedSetting INTEGER;");
                        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSION + ";");
                        db.execSQL(CREATE_TABLE_ALLOWED_CONTACTS); 
                        db.execSQL(CREATE_TABLE_MAP);
                        db.execSQL(INSERT_VERSION);
                        db.execSQL(INSERT_ENABLED);
                        db.execSQL(INSERT_NOTIFICATIONS_ENABLED);
                        
                        // remove uid dirs from the settings directory
                        File settingsDir = new File(SETTINGS_DIRECTORY);
                        for (File packageDir : settingsDir.listFiles()) {
                            for (File uidDir : packageDir.listFiles()) {
                                if (uidDir.isDirectory()) {
                                    File[] settingsFiles = uidDir.listFiles();
                                    // copy the first found (most likely the only one) one level up
                                    if (settingsFiles[0] != null) {
                                        File newPath = new File(packageDir + "/" + settingsFiles[0].getName());
                                        newPath.delete();
                                        settingsFiles[0].renameTo(newPath);
                                        deleteRecursive(uidDir);
                                    }
                                }
                            }
                        }
                        
                        purgeSettings();
                        
                        db.setTransactionSuccessful();
                    }
                } catch (Exception e) {
                    if (db != null && db.isOpen()) db.close();
                    Log.w(TAG, "upgradeDatabase - could not upgrade DB; will restore backup", e);
                    FileUtils.copyFile(dbBackupFile, dbFile);
                    dbBackupFile.delete();
                }
                break;
                
            case 3:
                // most current version, do nothing
                Log.w(TAG, "upgradeDatabase - trying to upgrade most current DB version");
                break;
        }
        
        if (db != null && db.isOpen()) {
            db.endTransaction();
            db.close();
        }
    }
    
    private int getDbVersion() {
        String version = getValue(SETTING_DB_VERSION);
        if (version == null) return 1;
        
        int versionNum;
        try {
            versionNum = Integer.parseInt(version);
        } catch (Exception e) {
            Log.e(TAG, "getDbVersion - failed to parse database version; returning 1");
            return 1;
        }
        
        return versionNum;
    }
    
    public String getValue(String name) {
        readingThreads++;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        String output = null;
        
        try {
            c = query(db, TABLE_MAP, new String[] { "value" }, "name=?", 
                    new String[] { name }, null, null, null, null);
            if (c != null && c.getCount() > 0 && c.moveToFirst()) {
                output = c.getString(c.getColumnIndex("value"));
                c.close();
            } else {
                Log.w(TAG, "getValue - could not get value for name: " + name);
            }
        } catch (Exception e) {
            Log.w(TAG, "getValue - could not get value for name: " + name, e);
        }
        
        synchronized (readingThreads) {
            readingThreads--;
            // only close DB if no other threads are reading
            if (readingThreads == 0 && db != null && db.isOpen()) {
                db.close();
            }
        }
        return output;
    }
    
    public synchronized boolean setValue(String name, String value) {
        Log.e(TAG, "setValue - name " + name + " value " + value);
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("value", value);
        SQLiteDatabase db = getWritableDatabase();
        boolean success = db.replace(TABLE_MAP, null, values) != -1;
        if (readingThreads == 0 && db != null && db.isOpen()) db.close();
        return success;
    }
    
    public PrivacySettings getSettings(String packageName, boolean forceCloseDB) {
        PrivacySettings s = null;
        
        if (packageName == null) {
            Log.e(TAG, "getSettings - insufficient application identifier - package name is required");
            return s;
        }
        
        // indicate that the DB is being read to prevent closing by other threads
        readingThreads++;
//        Log.d(TAG, "getSettings - settings request for package: " + packageName + " readingThreads: " + readingThreads);
        
        SQLiteDatabase db;
        try {
            db = getReadableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "getSettings - database could not be opened", e);
            readingThreads--;
            return s;
        }
            
        Cursor c = null;

        try {
            c = query(db, TABLE_SETTINGS, DATABASE_FIELDS, "packageName=?", new String[] { packageName }, null, null, null, null);

            if (c != null && c.moveToFirst()) {
                s = new PrivacySettings(c.getInt(0), c.getString(1), c.getInt(2), (byte)c.getShort(3), c.getString(4), 
                        (byte)c.getShort(5), c.getString(6), (byte)c.getShort(7), c.getString(8), c.getString(9), (byte)c.getShort(10), 
                        c.getString(11), c.getString(12), (byte)c.getShort(13), (byte)c.getShort(14), (byte)c.getShort(15), 
                        c.getString(16), (byte)c.getShort(17), c.getString(18), (byte)c.getShort(19), (byte)c.getShort(20), 
                        (byte)c.getShort(21), (byte)c.getShort(22), (byte)c.getShort(23), (byte)c.getShort(24), (byte)c.getShort(25), 
                        (byte)c.getShort(26), (byte)c.getShort(27), (byte)c.getShort(28), (byte)c.getShort(29), (byte)c.getShort(30), 
                        (byte)c.getShort(31), (byte)c.getShort(32), (byte)c.getShort(33), (byte)c.getShort(34), null);
                
                // get allowed contacts IDs if necessary
//                Log.d(TAG, "getSettings - looking for allowed contacts for " + s.get_id());
//                c = query(db, TABLE_ALLOWED_CONTACTS, null, 
//                        "settings_id=?", new String[] { Integer.toString(s.get_id()) }, null, null, null, null);
                c = db.rawQuery("SELECT * FROM allowed_contacts WHERE settings_id=" + Integer.toString(s.get_id()) + ";", null);
                
                if (c != null && c.getCount() > 0) {
//                    Log.d(TAG, "getSettings - found allowed contacts");
                    int[] allowedContacts = new int[c.getCount()];
                    while (c.moveToNext()) allowedContacts[c.getPosition()] = c.getInt(1);
                    s.setAllowedContacts(allowedContacts);
                }
//                    Log.d(TAG, "getSettings - found settings entry for package: " + packageName + " UID: " + uid);
            } 
//            else {
//                Log.e(TAG, "getSettings - no settings found for package: " + packageName);
//            }
        } catch (Exception e) {
            Log.e(TAG, "getSettings - failed to get settings for package: " + packageName, e);
            e.printStackTrace();
            if (c != null) c.close();
        } finally {
            if (c != null) c.close();
//            if (forceCloseDB && db != null && db.isOpen()) {
//                db.close();
//            } else {
            synchronized (readingThreads) {
                readingThreads--;
                // only close DB if no other threads are reading
                if (readingThreads == 0 && db != null && db.isOpen()) {
                    db.close();
                }
            }
//            }
        }
        
//        Log.d(TAG, "getSettings - returning settings: " + s);
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
//        Integer uid = s.getUid();
//        Log.d(TAG, "saveSettings - settings save request : " + s);
        
        if (packageName == null || packageName.isEmpty()/* || uid == null*/) {
            Log.e(TAG, "saveSettings - either package name, UID or both is missing");
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("packageName", packageName);
//        values.put("uid", uid);
        values.put("uid", DUMMY_UID);
        
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
        values.put("notificationSetting", s.getNotificationSetting());
        values.put("intentBootCompletedSetting", s.getIntentBootCompletedSetting());
//        values.put("externalStorageSetting", s.getExternalStorageSetting());
//        values.put("cameraSetting", s.getCameraSetting());
//        values.put("recordAudioSetting", s.getRecordAudioSetting());
        
        readingThreads++;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // make sure this ends up in a consistent state (DB and plain text files)
        Cursor c = null;
        try {
            // save settings to the DB
//            Log.d(TAG, "saveSettings - checking if entry exists already");
            Integer id = s.get_id();
            if (id != null) { // existing entry -> update
//                Log.d(TAG, "saveSettings - updating existing entry");
                if (db.update(TABLE_SETTINGS, values, "_id=?", new String[] { id.toString() }) < 1) {
                    throw new Exception("saveSettings - failed to update database entry");
                }
                
                db.delete(TABLE_ALLOWED_CONTACTS, "settings_id=?", new String[] { id.toString() });
                int[] allowedContacts = s.getAllowedContacts();
                if (allowedContacts != null) {
                    ContentValues contactsValues = new ContentValues();
                    for (int i = 0; i < allowedContacts.length; i++) {
                        contactsValues.put("settings_id", id);
                        contactsValues.put("contact_id", allowedContacts[i]);
                        if (db.insert(TABLE_ALLOWED_CONTACTS, null, contactsValues) == -1)
                            throw new Exception("saveSettings - failed to update database entry (contacts)");
                    }
                }

            } else { // new entry -> insert if no duplicates exist
//                Log.d(TAG, "saveSettings - new entry; verifying if duplicates exist");
                c = db.query(TABLE_SETTINGS, new String[] { "_id" }, "packageName=?", 
                        new String[] { s.getPackageName() }, null, null, null);
                
                if (c != null) {
                    if (c.getCount() == 1) { // exactly one entry
                        // exists -> update
//                        Log.d(TAG, "saveSettings - updating existing entry");
                        if (db.update(TABLE_SETTINGS, values, "packageName=?", 
                                new String[] { s.getPackageName() }) < 1) {
                            throw new Exception("saveSettings - failed to update database entry");
                        }
                        
                        if (c.moveToFirst()) {
                            Integer idAlt = c.getInt(0); // id of the found duplicate entry
                            db.delete(TABLE_ALLOWED_CONTACTS, "settings_id=?", new String[] { idAlt.toString() });
                            int[] allowedContacts = s.getAllowedContacts();
                            if (allowedContacts != null) {
                                ContentValues contactsValues = new ContentValues();
                                for (int i = 0; i < allowedContacts.length; i++) {
                                    contactsValues.put("settings_id", idAlt);
                                    contactsValues.put("contact_id", allowedContacts[i]);
                                    if (db.insert(TABLE_ALLOWED_CONTACTS, null, contactsValues) == -1)
                                        throw new Exception("saveSettings - failed to update database entry (contacts)");
                                }
                            }    
                        }
                    } else if (c.getCount() == 0) { // no entries -> insert
//                        Log.d(TAG, "saveSettings - inserting new entry");
                        long rowId = db.insert(TABLE_SETTINGS, null, values);
                        if (rowId == -1) {
                            throw new Exception("saveSettings - failed to insert new record into DB");
                        }
                        
                        db.delete(TABLE_ALLOWED_CONTACTS, "settings_id=?", new String[] { Long.toString(rowId) });
                        int[] allowedContacts = s.getAllowedContacts();
                        if (allowedContacts != null) {
                            ContentValues contactsValues = new ContentValues();
                            for (int i = 0; i < allowedContacts.length; i++) {
                                contactsValues.put("settings_id", rowId);
                                contactsValues.put("contact_id", allowedContacts[i]);
                                if (db.insert(TABLE_ALLOWED_CONTACTS, null, contactsValues) == -1)
                                    throw new Exception("saveSettings - failed to update database entry (contacts)");
                            }
                        }                        
                    } else { // something went totally wrong and there are multiple entries for same identifier
                        result = false;
                        throw new Exception("saveSettings - duplicate entries in the privacy.db");
                    }
                } else {
                    result = false;
                    // jump to catch block to avoid marking transaction as successful
                    throw new Exception("saveSettings - cursor is null, database access failed");
                }
            }
            
            // save settings to plain text file (for access from core libraries)
//            Log.d(TAG, "saveSettings - saving to plain text file");
//            File settingsUidDir = new File("/data/system/privacy/" + packageName + "/" + uid + "/");
            File settingsPackageDir = new File("/data/system/privacy/" + packageName + "/");
            File systemLogsSettingFile = new File("/data/system/privacy/" + packageName + "/" + "/systemLogsSetting");
            try {
                // create all parent directories on the file path
//                settingsUidDir.mkdirs();
                // make the directory readable (requires it to be executable as well)
//                settingsUidDir.setReadable(true, false);
//                settingsUidDir.setExecutable(true, false);
                // make the parent directory readable (requires it to be executable as well)
                settingsPackageDir.mkdirs();
                settingsPackageDir.setReadable(true, false);
                settingsPackageDir.setExecutable(true, false);
                // create the setting files and make them readable
                systemLogsSettingFile.createNewFile();
                systemLogsSettingFile.setReadable(true, false);
                // write settings to files
//                Log.d(TAG, "saveSettings - writing to file");
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(systemLogsSettingFile));
                writer.append(s.getSystemLogsSetting() + "");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                result = false;
                // jump to catch block to avoid marking transaction as successful
                throw new Exception("saveSettings - could not write settings to file", e);
            }
            // mark DB transaction successful (commit the changes)
            db.setTransactionSuccessful();
//            Log.d(TAG, "saveSettings - completing transaction");
        } catch (Exception e) {
            result = false;
//            Log.d(TAG, "saveSettings - could not save settings", e);
        } finally {
            db.endTransaction();
            if (c != null) c.close();
            synchronized (readingThreads) {
                readingThreads--;
                // only close DB if no other threads are reading
                if (readingThreads == 0 && db != null && db.isOpen()) {
                    db.close();
                }
            }
        }

        return result;
    }
    
    /**
     * Deletes a settings entry from the DB
     * @return true if settings were deleted successfully, false otherwise
     */
    public synchronized boolean deleteSettings(String packageName) {
        boolean result = true;
        
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction(); // make sure this ends up in a consistent state (DB and plain text files)
        try {
//            Log.d(TAG, "deleteSettings - deleting database entry for " + packageName + " (" + uid + ")");
            // try deleting contacts allowed entries; do not fail if deletion not possible
            Cursor c = db.query(TABLE_SETTINGS, new String[] { "_id" }, "packageName=?", 
                    new String[] { packageName }, null, null, null);
            if (c != null && c.getCount() > 0 && c.moveToFirst()) {
                int id = c.getInt(0);
                db.delete(TABLE_ALLOWED_CONTACTS, "settings_id=?", new String[] { Integer.toString(id) });
                c.close();
            }
            
            if (db.delete(TABLE_SETTINGS, "packageName=?", new String[] { packageName }) == 0) {
                Log.e(TAG, "deleteSettings - database entry for " + packageName + " not found");
                return false;
            }
            
            // delete settings from plain text file (for access from core libraries)
//            File settingsUidDir = new File("/data/system/privacy/" + packageName + "/" + uid + "/");
            File settingsPackageDir = new File("/data/system/privacy/" + packageName + "/");
            File systemLogsSettingFile = new File("/data/system/privacy/" + packageName + "/systemLogsSetting");
            // delete the setting files
            systemLogsSettingFile.delete();
            // delete the parent directories
//            settingsUidDir.delete();
            if (settingsPackageDir.list() == null || settingsPackageDir.list().length == 0) settingsPackageDir.delete();
            // mark DB transaction successful (commit the changes)
            db.setTransactionSuccessful();
        } catch (Exception e) {
            result = false;
            Log.e(TAG, "deleteSettings - could not delete settings", e);
        } finally {
            db.endTransaction();
            if (db != null && db.isOpen()) db.close();
        }
        
        return result;
    }
    
    private Cursor query(SQLiteDatabase db, String table, String[] columns, String selection, 
            String[] selectionArgs, String groupBy, String having, String orderBy, String limit) throws Exception {
        Cursor c = null;
        // make sure getting settings does not fail because of IllegalStateException (db already closed)
        boolean success = false;
        for (int i = 0; success == false && i < RETRY_QUERY_COUNT; i++) {
            try {
                if (c != null) c.close();
                c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
                success = true;
            } catch (IllegalStateException e) {
                success = false;
                if (db != null && db.isOpen()) db.close();
                db = getReadableDatabase();
            }
        }
        if (success == false) throw new Exception("query - failed to execute query on the DB");
        return c;
    }
    
    public boolean purgeSettings() {
        boolean result = true;
//        Log.d(TAG, "purgeSettings - begin purging settings");
        // get installed apps
        List<String> apps = new ArrayList<String>();
        PackageManager pMan = context.getPackageManager();
        List<ApplicationInfo> installedApps = pMan.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : installedApps) { 
            apps.add(appInfo.packageName);
        }
        
//        Log.d(TAG, "purgeSettings - purging directories");
        // delete obsolete settings directories
        File settingsDir = new File(SETTINGS_DIRECTORY);
        for (File packageDir : settingsDir.listFiles()) {
            String packageName = packageDir.getName();
//            Log.d(TAG, "purgeSettings - checking package directory " + packageName);
            
            if (!apps.contains(packageName)) { // remove package dir if no such app installed
//                Log.d(TAG, "purgeSettings - deleting " + packageName);
                deleteRecursive(packageDir);
            } 
        }
        
//        Log.d(TAG, "purgeSettings - purging database");
        // delete obsolete entries from DB and update outdated entries
        readingThreads++;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = query(db, TABLE_SETTINGS, new String[] {"packageName"}, null, null, null, null, null, null);
//            Log.d(TAG, "purgeSettings - found " + c.getCount() + " entries in the DB");
            List<String> appsInDb = new ArrayList<String>();
            while (c.moveToNext()) {
                String packageName = c.getString(0);
                if (!apps.contains(packageName)) {
                    deleteSettings(packageName);
                } else {
                    if (appsInDb.contains(packageName)) { // if duplicate entry, remove all duplicates and keep only one
                        PrivacySettings pSetTmp = getSettings(packageName, false);
                        deleteSettings(packageName);
                        saveSettings(pSetTmp);
                    } else {
                        appsInDb.add(packageName);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "purgeSettings - purging DB failed", e);
            result = false;
        } finally {
            if (c != null) c.close();
            synchronized (readingThreads) {
                readingThreads--;
                // only close DB if no other threads are reading
                if (readingThreads == 0 && db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
        return result;
    }
    
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) deleteRecursive(child);
        }
        fileOrDirectory.delete();
    }
    
    private synchronized void createDatabase() {
        Log.i(TAG, "createDatabase - creating privacy database file");
        try {
            SQLiteDatabase db = 
                SQLiteDatabase.openDatabase(DATABASE_FILE, null, SQLiteDatabase.OPEN_READWRITE | 
                        SQLiteDatabase.CREATE_IF_NECESSARY);
            Log.i(TAG, "createDatabase - creating privacy database");
            db.execSQL(CREATE_TABLE_SETTINGS);
            db.execSQL(CREATE_TABLE_ALLOWED_CONTACTS);
            db.execSQL(CREATE_TABLE_MAP);
            db.execSQL(INSERT_VERSION);
            db.execSQL(INSERT_ENABLED);
            db.execSQL(INSERT_NOTIFICATIONS_ENABLED);
    //        Log.d(TAG, "createDatabase - closing connection to privacy.db");
            if (db != null && db.isOpen()) db.close();
        } catch (SQLException e) {
            Log.e(TAG, "createDatabase - failed to create privacy database", e);
        }
    }
    
    private synchronized void createSettingsDir() {
        // create settings directory (for settings accessed from core libraries)
        File settingsDir = new File("/data/system/privacy/");
        settingsDir.mkdirs();
        // make it readable for everybody
        settingsDir.setReadable(true, false);
        settingsDir.setExecutable(true, false);
    }
    
    private synchronized SQLiteDatabase getReadableDatabase() {
        if (db != null && db.isOpen()) return db;
        
        db = SQLiteDatabase.openDatabase(DATABASE_FILE, null, SQLiteDatabase.OPEN_READONLY);
        
        return db;
    }

    private synchronized SQLiteDatabase getWritableDatabase() {
        // create the database if it does not exist
        if (!new File(DATABASE_FILE).exists()) createDatabase();
        
        if (db != null && db.isOpen() && !db.isReadOnly()) return db;
        
        db = SQLiteDatabase.openDatabase(DATABASE_FILE, null, SQLiteDatabase.OPEN_READWRITE);

        return db;
    }
}
