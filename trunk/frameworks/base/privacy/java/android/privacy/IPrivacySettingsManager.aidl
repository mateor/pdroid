package android.privacy;
import android.privacy.PrivacySettings;

/** {@hide} */
interface IPrivacySettingsManager
{
    PrivacySettings getSettings(String packageName, int uid);
    boolean saveSettings(in PrivacySettings settings);
    boolean deleteSettings(String packageName, int uid);
    void notification(String packageName, int uid, byte accessMode, String dataType, String output);
    void registerObservers();
    void addObserver(String packageName);
    boolean purgeSettings();
    double getVersion();
    boolean setEnabled(boolean enable);
    boolean setNotificationsEnabled(boolean enable);
    void setBootCompleted();
}
