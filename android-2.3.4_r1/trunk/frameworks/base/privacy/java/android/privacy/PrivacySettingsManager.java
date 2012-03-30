package android.privacy;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

/**
 * Provides API access to the privacy settings
 * @author Anonymous
 * TODO: selective contacts access
 */
public class PrivacySettingsManager {

    private static final String TAG = "PrivacySettingsManager";
    
    public static final String ACTION_PRIVACY_NOTIFICATION = "com.privacy.pdroid.PRIVACY_NOTIFICATION";
    
    private IPrivacySettingsManager service;
    
    /**
     * @hide - this should be instantiated through Context.getSystemService
     * @param context
     */
    public PrivacySettingsManager(Context context, IPrivacySettingsManager service) {
//        Log.d(TAG, "PrivacySettingsManager - initializing for package: " + context.getPackageName() + 
//                " UID:" + Binder.getCallingUid());
        this.service = service;
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        return getSettings(packageName);
    }
    
    public PrivacySettings getSettings(String packageName) {
        try {
//            Log.d(TAG, "getSettings for package: " + packageName + " UID: " + uid);
            if (service != null) {
                return service.getSettings(packageName);
            } else {
                Log.e(TAG, "getSettings - PrivacySettingsManagerService is null");
                return null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveSettings(PrivacySettings settings) {
        try {
//            Log.d(TAG, "saveSettings - " + settings);
            if (service != null) {            
                return service.saveSettings(settings);
            } else {
                Log.e(TAG, "saveSettings - PrivacySettingsManagerService is null");
                return false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in saveSettings: ", e);
            return false;
        }
    }
    
    public boolean deleteSettings(String packageName) {
        return deleteSettings(packageName);
    }
    
    public boolean deleteSettings(String packageName, int uid) {
        try {
//            Log.d(TAG, "deleteSettings - "  + packageName + " UID: " + uid);
            if (service != null) {
                return service.deleteSettings(packageName);
            } else {
                Log.e(TAG, "deleteSettings - PrivacySettingsManagerService is null");
                return false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in deleteSettings: ", e);
            return false;
        }
    }
    
    /**
     * Checks whether the PrivacySettingsManagerService is available. For some reason,
     * occasionally it appears to be null. In this case it should be initialized again.
     */
    public boolean isServiceAvailable() {
        if (service != null) return true;
        return false;
    }
    
    public void notification(String packageName, int uid, byte accessMode, String dataType, String output, PrivacySettings pSet) {
        notification(packageName, accessMode, dataType, output, pSet);
    }
    
    public void notification(String packageName, byte accessMode, String dataType, String output, PrivacySettings pSet) {
//        if (pSet != null && pSet.getNotificationSetting() == PrivacySettings.SETTING_NOTIFY_ON) {
            try {
                if (service != null) {
                    service.notification(packageName, accessMode, dataType, output);
                } else {
                    Log.e(TAG, "deleteSettings - PrivacySettingsManagerService is null");
                }            
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in notification: ", e);
            }
//        }
    }
    
    public void registerObservers() {
        try {
            if (service != null) {
                service.registerObservers();
            } else {
                Log.e(TAG, "deleteSettings - PrivacySettingsManagerService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in registerObservers: ", e);
        }
    }
    
    public void addObserver(String packageName) {
        try {
            if (service != null) {
                service.addObserver(packageName);
            } else {
                Log.e(TAG, "deleteSettings - PrivacySettingsManagerService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in addObserver: ", e);
        }
    }
    
    public boolean purgeSettings() {
        try {
            if (service != null) {
                return service.purgeSettings();
            } else {
                Log.e(TAG, "deleteSettings - PrivacySettingsManagerService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in purgeSettings: ", e);
        }
        return false;
    }
    
    public double getVersion() {
        try {
            if (service != null) {
                return service.getVersion();
            } else {
                Log.e(TAG, "getVersion - PrivacySettingsManagerService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getVersion: ", e);
        }
        return 0;
    }
    
    public boolean setEnabled(boolean enable) {
        try {
            if (service != null) {
                return service.setEnabled(enable);
            } else {
                Log.e(TAG, "setEnabled - PrivacySettingsManagerService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setEnabled: ", e);
        }
        return false;
    }
    
    public boolean setNotificationsEnabled(boolean enable) {
        try {
            if (service != null) {
                return service.setNotificationsEnabled(enable);
            } else {
                Log.e(TAG, "setNotificationsEnabled - PrivacySettingsManagerService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setNotificationsEnabled: ", e);
        }
        return false;
    }
    
    public void setBootCompleted() {
        try {
            if (service != null) {
                service.setBootCompleted();
            } else {
                Log.e(TAG, "setBootCompleted - PrivacySettingsManagerService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setBootCompleted: ", e);
        }
    }
}
