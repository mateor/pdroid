package android.privacy;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

public final class PrivacySettings implements Parcelable {
    
    /**
     * Real value, provided by the unmodified Android framework.
     */
    public static final byte REAL = 0;
    
    /**
     * Empty or unavailable, depending on setting type. For String settings, it is
     * setter method caller's responsibility to make sure that the corresponding 
     * setting field will contain an empty String.
     */
    public static final byte EMPTY = 1;
    
    /**
     * Custom specified output, appropriate for relevant setting. For String settings, 
     * it is setter method caller's responsibility to make sure that the corresponding 
     * setting field will contain a custom String.
     */
    public static final byte CUSTOM = 2;
    
    /**
     * Random output, appropriate for relevant setting. When this option is set, the
     * corresponding getter methods will generate appropriate random values automatically.
     * 
     * Device ID: a random string consisting of 15 numeric digits preceded by a "+"
     * Line1Number: a random string consisting of 13 numeric digits
     */
    public static final byte RANDOM = 3;
    
    // Database entry ID
    private final Integer _id;
    
    //
    // Application identifiers
    //
    
    private String packageName;
    private int uid;
    
    //
    // Privacy settings
    //
    
    private byte deviceIdSetting;
    private String deviceId;
    
    // Phone and Voice Mailbox Number
    private byte line1NumberSetting; 
    private String line1Number;
    
    private byte locationGpsSetting;
    private String locationGpsLat;
    private String locationGpsLon;
    private byte locationNetworkSetting;
    private String locationNetworkLat;
    private String locationNetworkLon;
    
    // CountryIso, Operator Code, Operator Name
    private byte networkInfoSetting;
    private byte simInfoSetting;
    
    private byte simSerialNumberSetting;
    private String simSerialNumber;
    private byte subscriberIdSetting;
    private String subscriberId;
    
    private byte accountsSetting;
    private byte accountsAuthTokensSetting;
    private byte outgoingCallsSetting;
    private byte incomingCallsSetting;
    
    private byte contactsSetting;
    private byte calendarSetting;
    private byte mmsSetting;
    private byte smsSetting;
    private byte callLogSetting;
    private byte bookmarksSetting; // browser bookmarks and history
    
    private byte systemLogsSetting;

    public PrivacySettings(Integer _id, String packageName, int uid) {
        this._id = _id;
        
        this.packageName = packageName;
        this.uid = uid;
        
        this.deviceIdSetting = REAL;
        this.deviceId = null;
        this.line1NumberSetting = REAL;
        this.line1Number = null;
        this.locationGpsSetting = REAL;
        this.locationGpsLat = null;
        this.locationGpsLon = null;
        this.locationNetworkSetting = REAL;
        this.locationNetworkLat = null;
        this.locationNetworkLon = null;
        this.networkInfoSetting = REAL;
        this.simInfoSetting = REAL;
        this.simSerialNumberSetting = REAL;
        this.simSerialNumber = null;
        this.subscriberIdSetting = REAL;
        this.subscriberId = null;
        this.accountsSetting = REAL;
        this.accountsAuthTokensSetting = REAL;
        this.outgoingCallsSetting = REAL;
        this.incomingCallsSetting = REAL;
        this.contactsSetting = REAL;
        this.calendarSetting = REAL;
        this.mmsSetting = REAL;
        this.smsSetting = REAL;
        this.callLogSetting = REAL;
        this.bookmarksSetting = REAL;
        this.systemLogsSetting = REAL;
    }
    
    public PrivacySettings(Integer id, String packageName, int uid, byte deviceIdSetting, String deviceId,
            byte line1NumberSetting, String line1Number, byte locationGpsSetting, String locationGpsLat,
            String locationGpsLon, byte locationNetworkSetting, String locationNetworkLat, 
            String locationNetworkLon, byte networkInfoSetting, byte simInfoSetting, byte simSerialNumberSetting,
            String simSerialNumber, byte subscriberIdSetting, String subscriberId, byte accountsSetting, 
            byte accountsAuthTokensSetting, byte outgoingCallsSetting, byte incomingCallsSetting, byte contactsSetting,
            byte calendarSetting, byte mmsSetting, byte smsSetting, byte callLogSetting, byte bookmarksSetting, 
            byte systemLogsSetting) {
        this._id = id;
        
        this.packageName = packageName;
        this.uid = uid;
        
        this.deviceIdSetting = deviceIdSetting;
        this.deviceId = deviceId;
        this.line1NumberSetting = line1NumberSetting;
        this.line1Number = line1Number;
        this.locationGpsSetting = locationGpsSetting;
        this.locationGpsLat = locationGpsLat;
        this.locationGpsLon = locationGpsLon;
        this.locationNetworkSetting = locationNetworkSetting;
        this.locationNetworkLat = locationNetworkLat;
        this.locationNetworkLon = locationNetworkLon;
        this.networkInfoSetting = networkInfoSetting;
        this.simInfoSetting = networkInfoSetting;
        this.simSerialNumberSetting = simSerialNumberSetting;
        this.simSerialNumber = simSerialNumber;
        this.subscriberIdSetting = subscriberIdSetting;
        this.subscriberId = subscriberId;
        this.accountsSetting = accountsSetting;
        this.accountsAuthTokensSetting = accountsAuthTokensSetting;
        this.outgoingCallsSetting = outgoingCallsSetting;
        this.incomingCallsSetting = incomingCallsSetting;
        this.contactsSetting = contactsSetting;
        this.calendarSetting = calendarSetting;
        this.mmsSetting = mmsSetting;
        this.smsSetting = smsSetting;
        this.callLogSetting = callLogSetting;
        this.bookmarksSetting = bookmarksSetting;
        this.systemLogsSetting = systemLogsSetting;
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

    public byte getDeviceIdSetting() {
        return deviceIdSetting;
    }

    public void setDeviceIdSetting(byte deviceIdSetting) {
        this.deviceIdSetting = deviceIdSetting;
    }

    public String getDeviceId() {
        if (deviceIdSetting == EMPTY) return "";
        if (deviceIdSetting == RANDOM) {
            Random rnd = new Random();
            String rndId = Math.abs(rnd.nextLong()) + "";
            return rndId.substring(0, 15);
        }
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public byte getLine1NumberSetting() {
        return line1NumberSetting;
    }

    public void setLine1NumberSetting(byte line1NumberSetting) {
        this.line1NumberSetting = line1NumberSetting;
    }

    public String getLine1Number() {
        if (line1NumberSetting == EMPTY) return "";
        if (line1NumberSetting == RANDOM) {
            Random rnd = new Random();
            String rndId = "+" + Math.abs(rnd.nextLong()) + "";
            return rndId.substring(0, 13);
        }
        return line1Number;
    }

    public void setLine1Number(String line1Number) {
        this.line1Number = line1Number;
    }

    public byte getLocationGpsSetting() {
        return locationGpsSetting;
    }

    public void setLocationGpsSetting(byte locationGpsSetting) {
        this.locationGpsSetting = locationGpsSetting;
    }

    public String getLocationGpsLat() {
        if (locationGpsSetting == EMPTY) return "";
        if (locationGpsSetting == RANDOM) return getRandomLat();
        return locationGpsLat;
    }

    public void setLocationGpsLat(String locationGpsLat) {
        this.locationGpsLat = locationGpsLat;
    }

    public String getLocationGpsLon() {
        if (locationGpsSetting == EMPTY) return "";        
        if (locationGpsSetting == RANDOM) return getRandomLon();
        return locationGpsLon;
    }

    public void setLocationGpsLon(String locationGpsLon) {
        this.locationGpsLon = locationGpsLon;
    }

    public byte getLocationNetworkSetting() {
        return locationNetworkSetting;
    }

    public void setLocationNetworkSetting(byte locationNetworkSetting) {
        this.locationNetworkSetting = locationNetworkSetting;
    }

    public String getLocationNetworkLat() {
        if (locationNetworkSetting == EMPTY) return "";
        if (locationNetworkSetting == RANDOM) return getRandomLat();  
        return locationNetworkLat;
    }

    public void setLocationNetworkLat(String locationNetworkLat) {
        this.locationNetworkLat = locationNetworkLat;
    }

    public String getLocationNetworkLon() {
        if (locationNetworkSetting == EMPTY) return "";
        if (locationNetworkSetting == RANDOM) return getRandomLon();
        return locationNetworkLon;
    }

    public void setLocationNetworkLon(String locationNetworkLon) {
        this.locationNetworkLon = locationNetworkLon;
    }

    public byte getNetworkInfoSetting() {
        return networkInfoSetting;
    }

    public void setNetworkInfoSetting(byte networkInfoSetting) {
        this.networkInfoSetting = networkInfoSetting;
    }

    public byte getSimInfoSetting() {
        return simInfoSetting;
    }

    public void setSimInfoSetting(byte simInfoSetting) {
        this.simInfoSetting = simInfoSetting;
    }

    public byte getSimSerialNumberSetting() {
        return simSerialNumberSetting;
    }

    public void setSimSerialNumberSetting(byte simSerialNumberSetting) {
        this.simSerialNumberSetting = simSerialNumberSetting;
    }

    public String getSimSerialNumber() {
        if (simSerialNumberSetting == EMPTY) return "";
        if (simSerialNumberSetting == RANDOM) {
            Random rnd = new Random();
            return Math.abs(rnd.nextLong()) + "";
        }
        return simSerialNumber;
    }

    public void setSimSerialNumber(String simSerialNumber) {
        this.simSerialNumber = simSerialNumber;
    }

    public byte getSubscriberIdSetting() {
        return subscriberIdSetting;
    }

    public void setSubscriberIdSetting(byte subscriberIdSetting) {
        this.subscriberIdSetting = subscriberIdSetting;
    }

    public String getSubscriberId() {
        if (subscriberIdSetting == EMPTY) return "";
        if (subscriberIdSetting == RANDOM) {
            Random rnd = new Random();
            String rndId = Math.abs(rnd.nextLong()) + "";
            return rndId.substring(0, 15);
        }
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public byte getAccountsSetting() {
        return accountsSetting;
    }

    public void setAccountsSetting(byte accountsSetting) {
        this.accountsSetting = accountsSetting;
    }

    public byte getAccountsAuthTokensSetting() {
        return accountsAuthTokensSetting;
    }

    public void setAccountsAuthTokensSetting(byte accountsAuthTokensSetting) {
        this.accountsAuthTokensSetting = accountsAuthTokensSetting;
    }

    public byte getOutgoingCallsSetting() {
        return outgoingCallsSetting;
    }

    public void setOutgoingCallsSetting(byte outgoingCallsSetting) {
        this.outgoingCallsSetting = outgoingCallsSetting;
    }
    
    public byte getIncomingCallsSetting() {
        return incomingCallsSetting;
    }
    
    public void setIncomingCallsSetting(byte incomingCallsSetting) {
        this.incomingCallsSetting = incomingCallsSetting;
    }

    public byte getContactsSetting() {
        return contactsSetting;
    }

    public void setContactsSetting(byte contactsSetting) {
        this.contactsSetting = contactsSetting;
    }

    public byte getCalendarSetting() {
        return calendarSetting;
    }

    public void setCalendarSetting(byte calendarSetting) {
        this.calendarSetting = calendarSetting;
    }

    public byte getMmsSetting() {
        return mmsSetting;
    }

    public void setMmsSetting(byte mmsSetting) {
        this.mmsSetting = mmsSetting;
    }

    public byte getSmsSetting() {
        return smsSetting;
    }

    public void setSmsSetting(byte smsSetting) {
        this.smsSetting = smsSetting;
    }

    public byte getCallLogSetting() {
        return callLogSetting;
    }

    public void setCallLogSetting(byte callLogSetting) {
        this.callLogSetting = callLogSetting;
    }

    public byte getBookmarksSetting() {
        return bookmarksSetting;
    }

    public void setBookmarksSetting(byte bookmarksSetting) {
        this.bookmarksSetting = bookmarksSetting;
    }

    public byte getSystemLogsSetting() {
        return systemLogsSetting;
    }

    public void setSystemLogsSetting(byte systemLogsSetting) {
        this.systemLogsSetting = systemLogsSetting;
    }

    @Override
    public String toString() {
        return "PrivacySettings [_id=" + _id + ", accountsAuthTokensSetting=" + accountsAuthTokensSetting
                + ", accountsSetting=" + accountsSetting + ", bookmarksSetting=" + bookmarksSetting
                + ", calendarSetting=" + calendarSetting + ", callLogSetting=" + callLogSetting + ", contactsSetting="
                + contactsSetting + ", deviceId=" + deviceId + ", deviceIdSetting=" + deviceIdSetting
                + ", incomingCallsSetting=" + incomingCallsSetting + ", line1Number=" + line1Number
                + ", line1NumberSetting=" + line1NumberSetting + ", locationGpsLat=" + locationGpsLat
                + ", locationGpsLon=" + locationGpsLon + ", locationGpsSetting=" + locationGpsSetting
                + ", locationNetworkLat=" + locationNetworkLat + ", locationNetworkLon=" + locationNetworkLon
                + ", locationNetworkSetting=" + locationNetworkSetting + ", mmsSetting=" + mmsSetting
                + ", networkInfoSetting=" + networkInfoSetting + ", outgoingCallsSetting=" + outgoingCallsSetting
                + ", packageName=" + packageName + ", simInfoSetting=" + simInfoSetting + ", simSerialNumber="
                + simSerialNumber + ", simSerialNumberSetting=" + simSerialNumberSetting + ", smsSetting=" + smsSetting
                + ", subscriberId=" + subscriberId + ", subscriberIdSetting=" + subscriberIdSetting
                + ", systemLogsSetting=" + systemLogsSetting + ", uid=" + uid + "]";
    }

    /**
     * Util methods
     */
    
    private String getRandomLat() {
        double lat = Math.random() * 180;
        if (lat > 90) return (lat - 90) + "";
        else return -lat + "";
    }
    
    private String getRandomLon() {
        double lon = Math.random() * 360;
        if (lon > 180) return (lon - 180) + "";
        else return -lon + "";
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
        
        this.deviceIdSetting = in.readByte();
        this.deviceId = in.readString();
        this.line1NumberSetting = in.readByte();
        this.line1Number = in.readString();
        this.locationGpsSetting = in.readByte();
        this.locationGpsLat = in.readString();
        this.locationGpsLon = in.readString();
        this.locationNetworkSetting = in.readByte();
        this.locationNetworkLat = in.readString();
        this.locationNetworkLon = in.readString();
        this.networkInfoSetting = in.readByte();
        this.simInfoSetting = in.readByte();
        this.simSerialNumberSetting = in.readByte();
        this.simSerialNumber = in.readString();
        this.subscriberIdSetting = in.readByte();
        this.subscriberId = in.readString();
        this.accountsSetting = in.readByte();
        this.accountsAuthTokensSetting = in.readByte();
        this.outgoingCallsSetting = in.readByte();
        this.incomingCallsSetting = in.readByte();
        this.contactsSetting = in.readByte();
        this.calendarSetting = in.readByte();
        this.mmsSetting = in.readByte();
        this.smsSetting = in.readByte();
        this.callLogSetting = in.readByte();
        this.bookmarksSetting = in.readByte();
        this.systemLogsSetting = in.readByte();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt((_id == null) ? -1 : _id);
        
        dest.writeString(packageName);
        dest.writeInt(uid);
        
        dest.writeByte(deviceIdSetting);
        dest.writeString(deviceId);
        dest.writeByte(line1NumberSetting);
        dest.writeString(line1Number);
        dest.writeByte(locationGpsSetting);
        dest.writeString(locationGpsLat);
        dest.writeString(locationGpsLon);
        dest.writeByte(locationNetworkSetting);
        dest.writeString(locationNetworkLat);
        dest.writeString(locationNetworkLon);
        dest.writeByte(networkInfoSetting);
        dest.writeByte(simInfoSetting);
        dest.writeByte(simSerialNumberSetting);
        dest.writeString(simSerialNumber);
        dest.writeByte(subscriberIdSetting);
        dest.writeString(subscriberId);
        dest.writeByte(accountsSetting);
        dest.writeByte(accountsAuthTokensSetting);
        dest.writeByte(outgoingCallsSetting);
        dest.writeByte(incomingCallsSetting);
        dest.writeByte(contactsSetting);
        dest.writeByte(calendarSetting);
        dest.writeByte(mmsSetting);
        dest.writeByte(smsSetting);
        dest.writeByte(callLogSetting);
        dest.writeByte(bookmarksSetting);
        dest.writeByte(systemLogsSetting);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
}
