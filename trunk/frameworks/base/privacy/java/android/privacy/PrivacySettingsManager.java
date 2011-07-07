package android.privacy;

import android.content.Context;
import android.os.Process;

public class PrivacySettingsManager {
    
    private PrivacyDBAdapter DBAdapter;
    private Context mContext;
    
    public PrivacySettingsManager(Context context) {
        mContext = context;
        DBAdapter = new PrivacyDBAdapter(mContext);
    }
    
    public PrivacySettings getSettings(String packageName, int uid) {
        return DBAdapter.getSettings(packageName, uid);
    }
    
    public String apiTest() {
        return "Privacy API working";
    }
    
    public String dbTest() {
        return DBAdapter.dbTest();
    }
}