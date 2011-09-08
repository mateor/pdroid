
package android.privacy;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;


public class PrivacySettingsManager {

    private String TAG = "PrivacySettingsManager";
    
    private IPrivacySettingsManager mService;
    
    private Context mContext;
    
    /**
     * @hide - this should be instantiated through Context.getSystemService
     * @param context
     */
    public PrivacySettingsManager(Context context, IPrivacySettingsManager service) {
        Log.d(TAG, "PrivacySettingsManager - initializing for package: " + context.getPackageName() + 
                " UID:" + Binder.getCallingUid());
        mService = service;
        mContext = context;
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        try {
            Log.d(TAG, "getSettings for package: " + packageName + " UID: " + uid);
            if (mService != null) {
                return mService.getSettings(packageName, uid);
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
            if (mService != null) {            
                return mService.saveSettings(settings);
            } else {
                Log.e(TAG, "saveSettings - PrivacySettingsManagerService is null");
                return false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in saveSettings: ", e);
            return false;
        }
    }
    
    /**
     * Checks whether the PrivacySettingsManagerService is available. For some reason,
     * occasionally it appears to be null. In this case it should be initialized again.
     */
    public boolean isServiceAvailable() {
        if (mService != null) return true;
        return false;
    }
}