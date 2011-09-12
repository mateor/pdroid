package android.privacy;

import android.content.Context;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Provides API access to the privacy settings
 * @author Anonymous
 */
public class PrivacySettingsManager {

    private String TAG = "PrivacySettingsManager";
    
    private IPrivacySettingsManager service;
    
    /**
     * @hide - this should be instantiated through Context.getSystemService
     * @param context
     */
    public PrivacySettingsManager(Context context, IPrivacySettingsManager service) {
        Log.d(TAG, "PrivacySettingsManager - initializing for package: " + context.getPackageName() + 
                " UID:" + Binder.getCallingUid());
        this.service = service;
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        try {
            Log.d(TAG, "getSettings for package: " + packageName + " UID: " + uid);
            if (service != null) {
                return service.getSettings(packageName, uid);
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
            Log.d(TAG, "saveSettings - " + settings);
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
    
    public boolean deleteSettings(String packageName, int uid) {
        try {
            Log.d(TAG, "deleteSettings - "  + packageName + " UID: " + uid);
            if (service != null) {
                return service.deleteSettings(packageName, uid);
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
}