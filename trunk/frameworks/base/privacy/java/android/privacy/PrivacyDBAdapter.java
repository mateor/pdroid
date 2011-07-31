
package android.privacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Binder;
import android.util.Log;

import java.io.File;

public class PrivacyDBAdapter {

    private static final String TAG = "PrivacyDBAdapter";

    private static final String DATABASE_NAME = "/data/system/privacy.db";

    private static final String DATABASE_TABLE = "settings";

    private static final int DATABASE_VERSION = 1;

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
            " outgoingCallsSetting INTEGER" + 
            ");";
    
    private static final String[] DATABASE_FIELDS = new String[] { "_id", "packageName", "uid", 
        "deviceIdSetting", "deviceId", "line1NumberSetting", "line1Number", "locationGpsSetting", 
        "locationGpsLat", "locationGpsLon", "locationNetworkSetting", "locationNetworkLat", 
        "locationNetworkLon", "networkInfoSetting", "simInfoSetting", "simSerialNumberSetting", 
        "simSerialNumber", "subscriberIdSetting", "subscriberId", "accountsSetting", "accountsAuthTokensSetting", 
        "outgoingCallsSetting"};

    private SQLiteDatabase db;

    public PrivacyDBAdapter(Context context) {
        // check write permission for /data/system/
        boolean canWrite = new File("/data/system/").canWrite();
        Log.d(TAG, "Constructing " + TAG + " for package: " +  context.getPackageName() + 
                " UID: " + Binder.getCallingUid() + "; Write permission for /data/system/: " + canWrite);
        // create the database if we have write permission and the DB does not exist
        if (canWrite && !new File(DATABASE_NAME).exists()) createDatabase();
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        Log.d(TAG, "getSettings: settings request for package: " + packageName + " UID: " + uid);
        PrivacySettings s = null;
        
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
                    // if we get multiple entries, try using UID as well 
                    // not guaranteed to find existing settings but it should only affect some system applications
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
                            (byte)c.getShort(21));
                    Log.d(TAG, "getSettings: found settings entry for package: " + packageName + " UID: " + uid);
                } else if (c.getCount() > 1) {
                    // multiple settings entries have same package name AND UID, this should NEVER happen
                    Log.e(TAG, "FATAL ERROR: duplicate entries in the privacy.db");
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

    public boolean saveSettings(PrivacySettings s) {
        boolean result = false;
        String packageName = s.getPackageName();
        Integer uid = s.getUid();
        Log.d(TAG, "saveSettings: settings save request : " + s);
        
        if (packageName == null || packageName.isEmpty() || uid == null) {
            Log.e(TAG, "Either package name, UID or both is missing.");
            return result;
        }

        SQLiteDatabase db = getWritableDatabase();

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

        Log.d(TAG, "saveSettings: checking if entry exists already.");
        Cursor c = null;
        try {
            if (s.get_id() != null) { // entry exists -> update

                Log.d(TAG, "saveSettings: updating existing entry");
                db.update(DATABASE_TABLE, values, "_id=?", new String[] { s.get_id().toString() });
                result = true;

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
                        result = true;
                    } else if (c.getCount() == 0) { // no entries -> insert
                        Log.d(TAG, "saveSettings: inserting new entry");
                        db.insert(DATABASE_TABLE, null, values);
                        result = true;
                    } else { // something went totally wrong and there are multiple entries for same identifier
                        Log.e(TAG, "FATAL ERROR: duplicate entries in the privacy.db");
                    }
                } else {
                    Log.e(TAG, "FATAL ERROR: database access failed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }

        return result;
    }
    
    private void createDatabase() {
        Log.d(TAG, "Creating privacy.db in /data/system; Flags: OPEN_READWRITE CREATE_IF_NECESSARY");
        SQLiteDatabase db = 
            SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        Log.d(TAG, "PrivacyDBAdapter: Executing database create statement on privacy.db");
        db.execSQL(DATABASE_CREATE);
        Log.d(TAG, "PrivacyDBAdapter: Closing connection to privacy.db");
        db.close();
    }
    
    private synchronized SQLiteDatabase getReadableDatabase() {
        
        if (db != null && db.isOpen() && db.isReadOnly()) {
            return db;
        }

        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
        this.db = db;

        return this.db;
    }

    private synchronized SQLiteDatabase getWritableDatabase() {
        // create the database if it does not exist
        if (!new File(DATABASE_NAME).exists()) createDatabase();
        
        if (db != null && db.isOpen() && !db.isReadOnly()) {
            return db;
        }
        
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        this.db = db;

        return this.db;
    }
}
