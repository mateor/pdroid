package android.privacy;

import android.os.Parcel;
import android.os.Parcelable;

public class PrivacySettings implements Parcelable {

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

    @Override
    public String toString() {
        return "PrivacySettings [_id=" + _id + ", deviceId=" + deviceId + ", line1Number=" + line1Number
                + ", packageName=" + packageName + ", uid=" + uid + "]";
    }

    /**
     * Parcelable implementation
     */


    public static final Parcelable.Creator<PrivacySettings> CREATOR = new
            Parcelable.Creator<PrivacySettings>() {
                public PrivacySettings createFromParcel(Parcel in) {
                    return new PrivacySettings(in);
                }

                public PrivacySettings[] newArray(int size) {
                    return new PrivacySettings[size];
                }
            };
    
    public PrivacySettings(Parcel in) {
        int _id = in.readInt();
        this._id = (_id == -1) ? null : _id;
        
        this.packageName = in.readString();
        this.uid = in.readInt();
        
        this.deviceId = in.readString();
        this.line1Number = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt((_id == null) ? -1 : _id);
        
        dest.writeString(packageName);
        dest.writeInt(uid);
        
        dest.writeString(deviceId);
        dest.writeString(line1Number);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
}
