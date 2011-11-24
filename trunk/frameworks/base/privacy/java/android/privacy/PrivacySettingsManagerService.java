package android.privacy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;

/**
 * PrivacySettingsManager's counterpart running in the system process, which
 * allows write access to /data/
 * @author Anonymous
 */
public class PrivacySettingsManagerService extends IPrivacySettingsManager.Stub {

    private static final String TAG = "PrivacySettingsManagerService";
    
    private static final String WRITE_PRIVACY_SETTINGS = "android.privacy.WRITE_PRIVACY_SETTINGS"; 

    private PrivacyPersistenceAdapter persistenceAdapter;

    private Context context;
    
    public static PrivacyFileObserver obs;
    
    /**
     * @hide - this should be instantiated through Context.getSystemService
     * @param context
     */
    public PrivacySettingsManagerService(Context context) {
//        Log.d(TAG, "PrivacySettingsManagerService: initializing for package: " + context.getPackageName() + 
//                " UID:" + Binder.getCallingUid());
        this.context = context;
        persistenceAdapter = new PrivacyPersistenceAdapter(context);
        obs = new PrivacyFileObserver("/data/system/privacy", this);
    }

    public PrivacySettings getSettings(String packageName, int uid) {
//        Log.d(TAG, "getSettings - " + packageName + " UID: " + uid);
        return persistenceAdapter.getSettings(packageName, uid);
    }

    public boolean saveSettings(PrivacySettings settings) {
//        Log.d(TAG, "saveSettings - checking if caller (UID: " + Binder.getCallingUid() + ") has sufficient permissions");
        context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");
//        Log.d(TAG, "saveSettings - " + settings);
        boolean result = persistenceAdapter.saveSettings(settings);
        if (result == true) obs.addObserver(settings.getPackageName());
        return result;
    }

    public boolean deleteSettings(String packageName, int uid) {
//        Log.d(TAG, "deleteSettings - " + packageName + " UID: " + uid + " " +
//        		"checking if caller (UID: " + Binder.getCallingUid() + ") has sufficient permissions");
        context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");
        boolean result = persistenceAdapter.deleteSettings(packageName, uid);
        // update observer if directory exists
        String observePath = PrivacyPersistenceAdapter.SETTINGS_DIRECTORY + "/" + packageName;
        if (new File(observePath).exists() && result == true) {
            obs.addObserver(observePath);
        } else if (result == true) {
            obs.children.remove(observePath);
        }
        return result;
    }

    public void notification(final String packageName, final int uid, final byte accessMode, final String dataType, final String output) {
        Intent intent = new Intent();
        intent.setAction(PrivacySettingsManager.ACTION_PRIVACY_NOTIFICATION);
        intent.putExtra("packageName", packageName);
        intent.putExtra("uid", uid);
        intent.putExtra("accessMode", accessMode);
        intent.putExtra("dataType", dataType);
        intent.putExtra("output", output);
        context.sendBroadcast(intent);
    }
    
    public void registerObservers() {
        context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");        
        obs = new PrivacyFileObserver("/data/system/privacy", this);
    }
    
    public void addObserver(String packageName) {
        context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");        
        obs.addObserver(packageName);
    }
    
    public boolean purgeSettings() {
        return persistenceAdapter.purgeSettings();
    }
}
