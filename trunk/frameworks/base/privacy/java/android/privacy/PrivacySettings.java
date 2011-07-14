package android.privacy;

import java.io.Serializable;

public class PrivacySettings implements Serializable {
    
    private static final long serialVersionUID = -5985415481586295138L;

    private final Integer _id;
    
    private String packageName;
    private int uid;
    
    private String deviceId;
    private String line1Number;
    
    public PrivacySettings(Integer _id, String packageName, int uid, String deviceId, String line1Number) {
        this._id = _id;
        
        this.packageName = packageName;
        this.uid = uid;
        
        this.deviceId = deviceId;
        this.line1Number = line1Number;
    }
    
    
    
    public Integer get_id() {
        return _id;
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
