
package android.privacy;

import android.content.Context;
import android.util.Log;

public class PrivacySettingsManager {

    private String TAG = "PrivacySettingsManager";

    private PrivacyDBAdapter DBAdapter;

    private Context mContext;

    public PrivacySettingsManager(Context context) {
        mContext = context;
        DBAdapter = new PrivacyDBAdapter(mContext);
        Log.d(TAG, "Initialized for package: " + context.getPackageName());
    }

    public PrivacySettings getSettings(String packageName, int uid) {
        return DBAdapter.getSettings(packageName, uid);
    }

    public boolean saveSettings(PrivacySettings settings) throws InsufficientAppIdentifierException {
        return DBAdapter.saveSettings(settings);
    }
}