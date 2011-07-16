
package android.privacy;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;


public class PrivacySettingsManager {

    private String TAG = "PrivacySettingsManager";
    
    private IPrivacySettingsManager mService;
    
    private Context mContext;
    
    private static final String WRITE_PRIVACY_SETTINGS = "android.permission.WRITE_PRIVACY_SETTINGS";
    
    /**
     * @hide - this should be instantiated through Context.getSystemService
     * @param context
     */
    public PrivacySettingsManager(Context context, IPrivacySettingsManager service) {
        mService = service;
        mContext = context;
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        try {
            Log.d(TAG, "getSettings for package: " + packageName + " UID: " + uid);
            return mService.getSettings(packageName, uid);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveSettings(PrivacySettings settings) {
        mContext.enforceCallingOrSelfPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");
        try {
            Log.d(TAG, "saveSettings: " + settings);
            return mService.saveSettings(settings);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in saveSettings: ", e);
            return false;
        }
    }
}