package android.privacy.surrogate;

import android.content.Context;
import android.os.Binder;
import android.os.ServiceManager;
import android.privacy.IPrivacySettingsManager;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides privacy handling for {@link android.telephony.TelephonyManager}
 * @author Anonymous
 */
public final class PrivacyTelephonyManager extends TelephonyManager {

    private static final String TAG = "PrivacyTelephonyManager";
    
    private Context context;
    
    private PrivacySettingsManager pSetMan;
    
    public PrivacyTelephonyManager(Context context) {
        super(context);
        this.context = context;
//        pSetMan = (PrivacySettingsManager) context.getSystemService("privacy");
        // don't call getSystemService to avoid getting java.lang.IllegalStateException: 
        // System services not available to Activities before onCreate()
        pSetMan = new PrivacySettingsManager(context, IPrivacySettingsManager.Stub.asInterface(ServiceManager.getService("privacy")));
    }
    
    /**
     * IMEI
     */
    @Override
    public String getDeviceId() {
        String packageName = context.getPackageName();
        int uid = Binder.getCallingUid();
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        String output;
        if (pSet != null && pSet.getDeviceIdSetting() != PrivacySettings.REAL) {
            output = pSet.getDeviceId(); // can be empty, custom or random
            pSetMan.notification(packageName, uid, pSet.getDeviceIdSetting(), PrivacySettings.DATA_DEVICE_ID, output, pSet);
        } else {
            output = super.getDeviceId();
            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_DEVICE_ID, output, pSet);
        }
//        Log.d(TAG, "getDeviceId - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Phone number
     */
    @Override
    public String getLine1Number() {
        String packageName = context.getPackageName();
        int uid = Binder.getCallingUid();
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        String output;
        if (pSet != null && pSet.getLine1NumberSetting() != PrivacySettings.REAL) {
            output = pSet.getLine1Number(); // can be empty, custom or random
            pSetMan.notification(packageName, uid, pSet.getLine1NumberSetting(), PrivacySettings.DATA_LINE_1_NUMBER, output, pSet);
        } else {
            output = super.getLine1Number();
            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_LINE_1_NUMBER, output, pSet);
        }
//        Log.d(TAG, "getLine1Number - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Will be handled like the Line1Number, since voice mailbox numbers often
     * are similar to the phone number of the subscriber.
     */
    @Override
    public String getVoiceMailNumber() {
        String packageName = context.getPackageName();
        int uid = Binder.getCallingUid();
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        String output;
        if (pSet != null && pSet.getLine1NumberSetting() != PrivacySettings.REAL) {
            output = pSet.getLine1Number(); // can be empty, custom or random
            pSetMan.notification(packageName, uid, pSet.getLine1NumberSetting(), PrivacySettings.DATA_LINE_1_NUMBER, output, pSet);
        } else {
            output = super.getVoiceMailNumber();
            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_LINE_1_NUMBER, output, pSet);
        }
//        Log.d(TAG, "getVoiceMailNumber - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Intercept requests for mobile network cell information. This can be used for tracking network
     * based location.
     */
    @Override
    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        PrivacySettings pSet = pSetMan.getSettings(context.getPackageName(), Binder.getCallingUid());
        List<NeighboringCellInfo> output = null;
        String output_label = "[null]";
        
        if (pSet != null) {
            if (pSet.getLocationNetworkSetting() == PrivacySettings.EMPTY) {
                // output = null;
            } else if (pSet.getLocationNetworkSetting() != PrivacySettings.REAL) {
                output = new ArrayList<NeighboringCellInfo>();
                output_label = "[empty list of cells]";
            } else {
                output = super.getNeighboringCellInfo();
                String cells = "";
                for (NeighboringCellInfo i : output) cells += "\t" + i + "\n";
                output_label = "[real value]:\n" + cells;
            }
        }
        
//        Log.d(TAG, "getNeighboringCellInfo - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);
        return output;
    }
    
    @Override
    public String getNetworkCountryIso() {
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkCountryIso();
//        Log.d(TAG, "getNetworkCountryIso - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getNetworkOperator() {
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkOperator();
//        Log.d(TAG, "getNetworkOperator - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getNetworkOperatorName() {
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkOperatorName();
//        Log.d(TAG, "getNetworkOperatorName - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Handles following Network Information requests: CountryIso, Operator Code, Operator Name
     * @return value to return if applicable or null if real value should be returned
     */
    private String getNetworkInfo() {
        String packageName = context.getPackageName();
        int uid = Binder.getCallingUid();
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        if (pSet != null && pSet.getNetworkInfoSetting() != PrivacySettings.REAL) {
            pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_NETWORK_INFO_CURRENT, null, pSet);            
            return ""; // can only be empty
        } else {
            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_NETWORK_INFO_CURRENT, null, pSet);            
            return null;
        }        
    }
    
    @Override
    public String getSimCountryIso() {
        String output = getSimInfo();
        if (output == null) output = super.getSimCountryIso();
//        Log.d(TAG, "getSimCountryIso - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getSimOperator() {
        String output = getSimInfo();
        if (output == null) output = super.getSimOperator();
//        Log.d(TAG, "getSimOperator - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getSimOperatorName() {
        String output = getSimInfo();
        if (output == null) output = super.getSimOperatorName();
//        Log.d(TAG, "getSimOperatorName - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Handles following SIM Card information requests: CountryIso, Operator Code, Operator Name
     * @return value to return if applicable or null if real value should be returned
     */    
    private String getSimInfo() {
        String packageName = context.getPackageName();
        int uid = Binder.getCallingUid();
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        if (pSet != null && pSet.getSimInfoSetting() != PrivacySettings.REAL) {
            pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_NETWORK_INFO_SIM, null, pSet);            
            return ""; // can only be empty
        } else {
            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_NETWORK_INFO_SIM, null, pSet);            
            return null;
        }                
    }
    
    /**
     * ICCID
     */
    @Override
    public String getSimSerialNumber() {
        String packageName = context.getPackageName();
        int uid = Binder.getCallingUid();
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        String output;
        if (pSet != null && pSet.getSimSerialNumberSetting() != PrivacySettings.REAL) {
            output = pSet.getSimSerialNumber(); // can be empty, custom or random
            pSetMan.notification(packageName, uid, pSet.getSimSerialNumberSetting(), PrivacySettings.DATA_SIM_SERIAL, output, pSet);            
        } else {
            output = super.getSimSerialNumber();
            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_SIM_SERIAL, output, pSet);            
        }
//        Log.d(TAG, "getSimSerialNumber - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * IMSI
     */
    @Override
    public String getSubscriberId() {
        String packageName = context.getPackageName();
        int uid = Binder.getCallingUid();
        PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
        String output;
        if (pSet != null && pSet.getSubscriberIdSetting() != PrivacySettings.REAL) {
            output = pSet.getSubscriberId(); // can be empty, custom or random
            pSetMan.notification(packageName, uid, pSet.getSubscriberIdSetting(), PrivacySettings.DATA_SUBSCRIBER_ID, output, pSet);            
        } else {
            output = super.getSubscriberId();
            pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_SUBSCRIBER_ID, output, pSet);            
        }
//        Log.d(TAG, "getSubscriberId - " + context.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    /**
     * For monitoring purposes only
     */    
//    @Override
//    public void enableLocationUpdates() {
////        Log.d(TAG, "enableLocationUpdates - " + context.getPackageName() + " (" + Binder.getCallingUid() + ")");
//        super.enableLocationUpdates();
//    }

    @Override
    public void listen(PhoneStateListener listener, int events) {
        if (((events & PhoneStateListener.LISTEN_CELL_LOCATION) != 0) || 
                ((events & PhoneStateListener.LISTEN_CALL_STATE) != 0)) {
            super.listen(new PrivacyPhoneStateListener(listener, context.getPackageName(), 
                    Binder.getCallingUid()), events);
//            Log.d(TAG, "listen for cell location or call state - " + context.getPackageName() + " (" + 
//                    Binder.getCallingUid() + ") output: custom listener");
        } else {
            super.listen(listener, events);
        }
    }
    
    private class PrivacyPhoneStateListener extends PhoneStateListener {
        
        private PhoneStateListener realListener;
        
        private String packageName;
        
        private int uid;
        
        public PrivacyPhoneStateListener(PhoneStateListener realListener, String packageName, int uid) {
            this.realListener = realListener;
            this.packageName = packageName;
            this.uid = uid;
        }
        
        /**
         * Replace the incoming phone number with an empty string and pass the call to 
         * the real phone state listener, if it is still there
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (realListener != null) {
                // only take action if an incoming phone number is actually transmitted
                if (incomingNumber != null && !incomingNumber.isEmpty()) {
                    PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
                    String output;
                    if (pSet != null && pSet.getIncomingCallsSetting() != PrivacySettings.REAL) {
                        output = "";
                        realListener.onCallStateChanged(state, output);
                        pSetMan.notification(packageName, uid, PrivacySettings.EMPTY, PrivacySettings.DATA_INCOMING_CALL, output, pSet);
                    } else {
                        output = incomingNumber;
                        realListener.onCallStateChanged(state, incomingNumber);
                        pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_INCOMING_CALL, output, pSet);
                    }
                } else {
                    realListener.onCallStateChanged(state, incomingNumber);
                }
    //            Log.d(TAG, "onCallStateChanged (incoming number) - " + context.getPackageName() + " (" + 
    //                    Binder.getCallingUid() + ") output: " + output);
            }
        }
        
        /**
         * Does not call the real listeners method if network location is restricted
         * by privacy settings
         */
        @Override
        public void onCellLocationChanged(CellLocation location) {
            PrivacySettings pSet = pSetMan.getSettings(packageName, uid);
            String output;
            if (pSet != null && pSet.getLocationNetworkSetting() != PrivacySettings.REAL) {
                // simply block the method call, since simulating cell location is not feasible
                output = "[no output]";
                pSetMan.notification(packageName, uid, pSet.getLocationNetworkSetting(), PrivacySettings.DATA_LOCATION_NETWORK, null, pSet);            
            } else { 
                output = location.toString();
                realListener.onCellLocationChanged(location);
                pSetMan.notification(packageName, uid, PrivacySettings.REAL, PrivacySettings.DATA_LOCATION_NETWORK, null, pSet);            
            }
//            Log.d(TAG, "onCellLocationChanged - " + context.getPackageName() + " (" + 
//                    Binder.getCallingUid() + ") output: " + output);
        }

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            realListener.onCallForwardingIndicatorChanged(cfi);
        }

        @Override
        public void onDataActivity(int direction) {
            realListener.onDataActivity(direction);
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            realListener.onDataConnectionStateChanged(state, networkType);
        }

        @Override
        public void onDataConnectionStateChanged(int state) {
            realListener.onDataConnectionStateChanged(state);
        }

        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            realListener.onMessageWaitingIndicatorChanged(mwi);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            realListener.onServiceStateChanged(serviceState);
        }

        @Override
        public void onSignalStrengthChanged(int asu) {
            realListener.onSignalStrengthChanged(asu);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            realListener.onSignalStrengthsChanged(signalStrength);
        }
        
    }
}
