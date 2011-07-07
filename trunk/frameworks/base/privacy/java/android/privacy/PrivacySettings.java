package android.privacy;

public class PrivacySettings {
    
    private String packageName;
    private int uid;
    
    private String deviceId;
    private String line1Number;
    
    public PrivacySettings(String packageName, int uid, String deviceId, String line1Number) {
        this.packageName = packageName;
        this.uid = uid;
        this.deviceId = deviceId;
        this.line1Number = line1Number;
    }

    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getLine1Number() {
        return line1Number;
    }
    
    public void setLine1Number(String line1Number) {
        this.line1Number = line1Number;
    }
    
}
