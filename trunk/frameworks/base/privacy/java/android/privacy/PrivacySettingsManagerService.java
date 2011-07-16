
package android.privacy;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;

public class PrivacySettingsManagerService extends IPrivacySettingsManager.Stub {

    private String TAG = "PrivacySettingsManagerService";

    private PrivacyDBAdapter DBAdapter;

    private Context mContext;
    
    /**
     * @hide - this should be instantiated through Context.getSystemService
     * @param context
     */
    public PrivacySettingsManagerService(Context context) {
        mContext = context;
        DBAdapter = new PrivacyDBAdapter(mContext);
        Log.d(TAG, "Initialized for package: " + context.getPackageName());
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        Log.d(TAG, "getSettings for package: " + packageName + " UID: " + uid);
        return DBAdapter.getSettings(packageName, uid);
    }

    public boolean saveSettings(PrivacySettings settings) {
        Log.d(TAG, "saveSettings: " + settings);
        return DBAdapter.saveSettings(settings);
    }
}