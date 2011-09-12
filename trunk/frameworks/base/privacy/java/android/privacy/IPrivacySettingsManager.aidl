package android.privacy;
import android.privacy.PrivacySettings;

/** {@hide} */
interface IPrivacySettingsManager
{
    PrivacySettings getSettings(String packageName, int uid);
    boolean saveSettings(in PrivacySettings settings);
    boolean deleteSettings(String packageName, int uid);
}