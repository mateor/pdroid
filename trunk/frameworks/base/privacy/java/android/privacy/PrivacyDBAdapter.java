
package android.privacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;

public class PrivacyDBAdapter {

    private static final String TAG = "PrivacyDBAdapter";

    private static final String DATABASE_NAME = "/data/system/privacy.db";

    private static final String DATABASE_TABLE = "settings";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ( "
            + " _id INTEGER PRIMARY KEY AUTOINCREMENT, " + " packageName TEXT, " + " uid INTEGER, "
            + " deviceId TEXT, " + " line1Number TEXT);";

    private SQLiteDatabase db;

    public PrivacyDBAdapter(Context context) {
        // check write permission for /data/system/
        boolean canWrite = new File("/data/system/").canWrite();
        Log.d(TAG, "Write permission for /data/system/: " + canWrite);
        // create the database if we have write permission and the DB does not exist
        if (canWrite && !new File(DATABASE_NAME).exists()) {
            Log.d(TAG, "Creating privacy.db in /data/system; FLags: OPEN_READWRITE CREATE_IF_NECESSARY");
            SQLiteDatabase db = getWritableDatabase();
            Log.d(TAG, "PrivacyDBAdapter: Executing database create statement on privacy.db");
            db.execSQL(DATABASE_CREATE);
            Log.d(TAG, "PrivacyDBAdapter: Closing connection to privacy.db");
            db.close();
        }        
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        Log.d(TAG, "getSettings: settings request for package: " + packageName + " UID: " + uid);
        PrivacySettings s = null;
        Log.d(TAG, "getSettings: opening database in readable mode");
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;

        try {
            // try to get settings based on package name only first; some system applications
            // (e.g., HTC Settings) run with a different UID than reported by package manager
            c = db.query(DATABASE_TABLE, new String[] { "_id", "packageName", "uid",
                    "deviceId", "line1Number" }, "packageName=?", new String[] { packageName }, null, null, null);

            if (c != null) {
                if (c.getCount() > 1) {
                    // if we get multiple entries, try using UID as well 
                    // not guaranteed to find existing settings but it should only affect some system applications
                    Log.d(TAG, "getSettings: multiple settings entries found for package name: " + packageName
                            + "; trying with UID: " + uid);

                    c = db.query(DATABASE_TABLE,
                            new String[] { "_id", "packageName", "uid", "deviceId", "line1Number" },
                            "packageName=? AND uid=?", new String[] { packageName, uid + "" }, null, null, null);
                }

                if (c.getCount() == 1 && c.moveToFirst()) {
                    s = new PrivacySettings(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3), c.getString(4));
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

        return s;
    }

    public boolean saveSettings(PrivacySettings s) throws InsufficientAppIdentifierException {
        boolean result = false;
        // throw an exception if either package name or UID or both is not supplied
        // both are required for positive identification of an application
        String packageName = s.getPackageName();
        Integer uid = s.getUid();
        Log.d(TAG, "saveSettings: settings save request for package: " + packageName + " UID: " + uid);
        if (packageName == null || packageName.isEmpty() || uid == null) {
            throw new InsufficientAppIdentifierException("Either package name, UID or both is missing.");
        }

        Log.d(TAG, "saveSettings: opening database in writable mode");
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("packageName", packageName);
        values.put("uid", uid);
        
        values.put("deviceId", s.getDeviceId());
        values.put("line1Number", s.getLine1Number());

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

    private synchronized SQLiteDatabase getReadableDatabase() {
        if (db != null && db.isOpen() && db.isReadOnly()) {
            return db;
        }

        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
        this.db = db;

        return this.db;
    }

    private synchronized SQLiteDatabase getWritableDatabase() {
        if (db != null && db.isOpen() && !db.isReadOnly()) {
            return db;
        }

        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE
                | SQLiteDatabase.CREATE_IF_NECESSARY);
        this.db = db;

        return this.db;
    }

    public String dbTest() {

        Log.d(TAG, "Inserting test entry");

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            if (db.isDbLockedByOtherThreads())
                return "DB locked by others";
            if (db.isDbLockedByCurrentThread())
                return "DB locked by me";
            if (db.isReadOnly())
                return "DB is READONLY";
            db.delete("settings", null, null);

            ContentValues values = new ContentValues();
            values.put("packageName", "com.tester.app1");
            values.put("deviceId", "1336");
            values.put("line1Number", "017678021324");
            db.insert("settings", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();

        Log.d(TAG, "Querying DB");

        Cursor c = null;
        String output = "";
        try {
            db = getReadableDatabase();
            c = db.query("settings", new String[] {
                    "packageName", "deviceId", "line1Number"
            }, null, null, null, null, null);

            Log.d(TAG, "Collecting results");

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    output += c.getString(0) + " " + c.getString(1) + " " + c.getString(2) + " | ";
                }
            } else {
                output = "No results";
            }

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "dbTest: output: " + output);

        return output;
    }
}
