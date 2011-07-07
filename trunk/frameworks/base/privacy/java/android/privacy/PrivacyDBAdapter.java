package android.privacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

public class PrivacyDBAdapter {
    
    private static final String TAG = "PrivacyDBAdapter";
    
    private static final String DATABASE_NAME = "/data/system/privacy.db";
    private static final String DATABASE_TABLE = "settings";
    private static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ( "
                            + " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + " packageName TEXT, "
                            + " uid INTEGER, "
                            + " deviceId TEXT, "
                            + " line1Number TEXT);";
    
    private final Context mContext;
    private SQLiteDatabase db;
    
    public PrivacyDBAdapter(Context context) {
        mContext = context;
        
        // create the database if it does not exist
        if (!new File(DATABASE_NAME).exists()) {
            Log.d(TAG, "PrivacyDBAdapter: Creating privacy.db in /data/system; FLags: OPEN_READWRITE CREATE_IF_NECESSARY");
            SQLiteDatabase db = getWritableDatabase();
            Log.d(TAG, "PrivacyDBAdapter: Executing database create statement on privacy.db");
            db.execSQL(DATABASE_CREATE);
            Log.d(TAG, "PrivacyDBAdapter: Closing connection to privacy.db");
            db.close();
        }
    }
    
    public PrivacySettings getSettings(String packageName, int uid) {
        PrivacySettings settings = null;
        Log.d(TAG, "getSettings: opening database in readable mode");
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        
        try {
            c = db.query(DATABASE_TABLE, new String[] {"packageName", "uid", "deviceId", "line1Number"}, 
                    null, null, null, null, null);
            
            if (c != null && c.moveToFirst() && c.getCount() == 1) {
                settings = new PrivacySettings(c.getString(0), c.getInt(1), c.getString(2), c.getString(3));
            } else if (c != null && c.moveToFirst() && c.getCount() > 1) {
                // multiple applications have same package name AND uid, this should NEVER happen
                Log.e(TAG, "FATAL ERROR: Duplicate entries in the privacy.db");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }
        
        return settings;
    }

    public String dbTest() {
        
        Log.d(TAG, "Inserting test entry");
        
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            if (db.isDbLockedByOtherThreads()) return "DB locked by others";
            if (db.isDbLockedByCurrentThread()) return "DB locked by me";
            if (db.isReadOnly()) return "DB is READONLY";
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
            c = db.query("settings", new String[] {"packageName", "deviceId", "line1Number"}, 
                    null, null, null, null, null);
            
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
        
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_NAME, null, 
                SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        this.db = db;
        
        return this.db;
    } 
    
//    public Object getSetting(String packageName, int uid, String settingName) {
//        String setting = null;
//        Log.d(TAG, "getSetting: opening database in readable mode");
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor c = null;
//        
//        try {
//            Log.d(TAG, "getSetting: reading settings");
//            c = db.query("settings", new String[] {settingName},
//                    "packageName=?", new String[] {packageName}, null, null, null);
//            
//            if (c != null && c.moveToFirst() && c.getCount() == 1) {
//                setting = c.getString(0);
//                if (setting.isEmpty()) setting = null;
//            } else if (c != null && c.moveToFirst() && c.getCount() > 1) {
//                // TODO: handle the situation where multiple apps have same package name 
//                // (find an app unique ID instead of package name?)
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (c != null) c.close();
//            if (db != null) db.close();
//        }
//        
//        return setting;
//    }
}