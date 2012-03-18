package android.privacy;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

import java.io.File;

/**
 * PrivacySettingsManager's counterpart running in the system process, which
 * allows write access to /data/
 * @author Anonymous
 * TODO: add selective contact access management API
 */
public class PrivacySettingsManagerService extends IPrivacySettingsManager.Stub {

    private static final String TAG = "PrivacySettingsManagerService";
    
    private static final String WRITE_PRIVACY_SETTINGS = "android.privacy.WRITE_PRIVACY_SETTINGS";

    private PrivacyPersistenceAdapter persistenceAdapter;

    private Context context;
    
    public static PrivacyFileObserver obs;
    
    private boolean enabled;
    private boolean notificationsEnabled;
    private boolean bootCompleted;
    
    private static final double VERSION = 1.32;
    
    /**
     * @hide - this should be instantiated through Context.getSystemService
     * @param context
     */
    public PrivacySettingsManagerService(Context context) {
        Log.i(TAG, "PrivacySettingsManagerService: initializing for package: " + context.getPackageName() + 
                " UID:" + Binder.getCallingUid());
        this.context = context;
        persistenceAdapter = new PrivacyPersistenceAdapter(context);
        obs = new PrivacyFileObserver("/data/system/privacy", this);
        
        enabled = persistenceAdapter.getValue(PrivacyPersistenceAdapter.SETTING_ENABLED).equals(PrivacyPersistenceAdapter.VALUE_TRUE);
        notificationsEnabled = persistenceAdapter.getValue(PrivacyPersistenceAdapter.SETTING_NOTIFICATIONS_ENABLED).equals(PrivacyPersistenceAdapter.VALUE_TRUE);
        bootCompleted = false;
    }
    
    public PrivacySettings getSettings(String packageName) {
//        Log.d(TAG, "getSettings - " + packageName);
        if (enabled || context.getPackageName().equals("com.privacy.pdroid")) 
            return persistenceAdapter.getSettings(packageName, false);
        else return null;
    }

    public boolean saveSettings(PrivacySettings settings) {
        Log.d(TAG, "saveSettings - checking if caller (UID: " + Binder.getCallingUid() + ") has sufficient permissions");
        // check permission if not being called by the system process
        if (Binder.getCallingUid() != 1000) 
            context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");
        Log.d(TAG, "saveSettings - " + settings);
        boolean result = persistenceAdapter.saveSettings(settings);
        if (result == true) obs.addObserver(settings.getPackageName());
        return result;
    }
    
    public boolean deleteSettings(String packageName) {
//        Log.d(TAG, "deleteSettings - " + packageName + " UID: " + uid + " " +
//        		"checking if caller (UID: " + Binder.getCallingUid() + ") has sufficient permissions");
        // check permission if not being called by the system process
        if (Binder.getCallingUid() != 1000)
            context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");
        boolean result = persistenceAdapter.deleteSettings(packageName);
        // update observer if directory exists
        String observePath = PrivacyPersistenceAdapter.SETTINGS_DIRECTORY + "/" + packageName;
        if (new File(observePath).exists() && result == true) {
            obs.addObserver(observePath);
        } else if (result == true) {
            obs.children.remove(observePath);
        }
        return result;
    }
    
    public double getVersion() {
        return VERSION;
    }
    
    public void notification(final String packageName, final byte accessMode, final String dataType, final String output) {
        if (bootCompleted && notificationsEnabled) {
            Intent intent = new Intent();
            intent.setAction(PrivacySettingsManager.ACTION_PRIVACY_NOTIFICATION);
            intent.putExtra("packageName", packageName);
            intent.putExtra("uid", PrivacyPersistenceAdapter.DUMMY_UID);
            intent.putExtra("accessMode", accessMode);
            intent.putExtra("dataType", dataType);
            intent.putExtra("output", output);
            context.sendBroadcast(intent);
        }
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
    
    public void setBootCompleted() {
        bootCompleted = true;
    }
    
    public boolean setNotificationsEnabled(boolean enable) {
        String value = enable ? PrivacyPersistenceAdapter.VALUE_TRUE : PrivacyPersistenceAdapter.VALUE_FALSE;
        if (persistenceAdapter.setValue(PrivacyPersistenceAdapter.SETTING_NOTIFICATIONS_ENABLED, value)) {
            this.notificationsEnabled = true;
            this.bootCompleted = true;
            return true;
        } else {
            return false;
        }
    }
    
    public boolean setEnabled(boolean enable) {
        String value = enable ? PrivacyPersistenceAdapter.VALUE_TRUE : PrivacyPersistenceAdapter.VALUE_FALSE;
        if (persistenceAdapter.setValue(PrivacyPersistenceAdapter.SETTING_ENABLED, value)) {
            this.enabled = true;
            return true;
        } else {
            return false;
        }
    }
}
