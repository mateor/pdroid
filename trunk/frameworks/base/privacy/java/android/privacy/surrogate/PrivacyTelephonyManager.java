/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.privacy.surrogate;

import android.content.Context;
import android.os.Binder;
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

public final class PrivacyTelephonyManager extends TelephonyManager {

    private static final String TAG = "PrivacyTelephonyManager";
    private Context mContext;
    private PrivacySettingsManager mPrivSetManager;

    public PrivacyTelephonyManager(Context context) {
        super(context);
        mContext = context;
        mPrivSetManager = (PrivacySettingsManager) context.getSystemService("privacy");
    }
    
    @Override
    public String getDeviceId() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output;
        if (pSet != null && pSet.getDeviceIdSetting() != PrivacySettings.REAL) {
            output = pSet.getDeviceId(); // can be empty, custom or random
        } else {
            output = super.getDeviceId();
        }
        Log.d(TAG, "getDeviceId - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    @Override
    public String getLine1Number() {
        String output = getLine1NumberOrVoiceMailNumber();
        if (output == null) output = super.getLine1Number();
        Log.d(TAG, "getLine1Number - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Will be handled alongside the Line1Number, since voice mailbox numbers often
     * are similar to the phone number of the subscriber.
     */
    @Override
    public String getVoiceMailNumber() {
        String output = getLine1NumberOrVoiceMailNumber();
        if (output == null) output = super.getVoiceMailNumber();
        Log.d(TAG, "getVoiceMailNumber - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Handles Line1Number and VoiceMailNumber
     * @return value to return if applicable or null if real value should be returned
     */
    private String getLine1NumberOrVoiceMailNumber() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        if (pSet != null && pSet.getLine1NumberSetting() != PrivacySettings.REAL) {
            return pSet.getLine1Number(); // can be empty, custom or random
        } else {
            return null;
        }
    }
    
    /**
     * Intercept requests for mobile network cell information. This can be used for tracking network
     * based location.
     */
    @Override
    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
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
        
        Log.d(TAG, "getNeighboringCellInfo - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output_label);
        return output;
    }
    
    @Override
    public String getNetworkCountryIso() {
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkCountryIso();
        Log.d(TAG, "getNetworkCountryIso - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getNetworkOperator() {
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkOperator();
        Log.d(TAG, "getNetworkOperator - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getNetworkOperatorName() {
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkOperatorName();
        Log.d(TAG, "getNetworkOperatorName - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Handles following Network Information requests: CountryIso, Operator Code, Operator Name
     * @return value to return if applicable or null if real value should be returned
     */
    private String getNetworkInfo() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        if (pSet != null && pSet.getNetworkInfoSetting() != PrivacySettings.REAL) {
            return ""; // can only be empty
        } else {
            return null;
        }        
    }
    
    @Override
    public String getSimCountryIso() {
        String output = getSimInfo();
        if (output == null) output = super.getSimCountryIso();
        Log.d(TAG, "getSimCountryIso - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getSimOperator() {
        String output = getSimInfo();
        if (output == null) output = super.getSimOperator();
        Log.d(TAG, "getSimOperator - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    @Override
    public String getSimOperatorName() {
        String output = getSimInfo();
        if (output == null) output = super.getSimOperatorName();
        Log.d(TAG, "getSimOperatorName - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * Handles following SIM Card information requests: CountryIso, Operator Code, Operator Name
     * @return value to return if applicable or null if real value should be returned
     */    
    private String getSimInfo() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        if (pSet != null && pSet.getSimInfoSetting() != PrivacySettings.REAL) {
            return ""; // can only be empty
        } else {
            return null;
        }                
    }
    
    /**
     * ICCID
     */
    @Override
    public String getSimSerialNumber() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output;
        if (pSet != null && pSet.getSimSerialNumberSetting() != PrivacySettings.REAL) {
            output = pSet.getSimSerialNumber(); // can be empty, custom or random
        } else {
            output = super.getSimSerialNumber();
        }
        Log.d(TAG, "getSimSerialNumber - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }
    
    /**
     * IMSI
     */
    @Override
    public String getSubscriberId() {
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output;
        if (pSet != null && pSet.getSubscriberIdSetting() != PrivacySettings.REAL) {
            output = pSet.getSubscriberId(); // can be empty, custom or random
        } else {
            output = super.getSubscriberId();
        }
        Log.d(TAG, "getSubscriberId - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + output);
        return output;
    }

    /**
     * For monitoring purposes only
     */    
    @Override
    public void enableLocationUpdates() {
        Log.d(TAG, "enableLocationUpdates - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ")");
        super.enableLocationUpdates();
    }

    @Override
    public void listen(PhoneStateListener listener, int events) {
        if (((events & PhoneStateListener.LISTEN_CELL_LOCATION) != 0) || 
                ((events & PhoneStateListener.LISTEN_CALL_STATE) != 0)) {
            super.listen(new PrivacyPhoneStateListener(listener, mContext.getPackageName(), 
                    Binder.getCallingUid()), events);
            Log.d(TAG, "listen for cell location or call state - " + mContext.getPackageName() + " (" + 
                    Binder.getCallingUid() + ") output: custom listener");
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
         * the real phone state listener 
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            PrivacySettings pSet = mPrivSetManager.getSettings(packageName, uid);
            String output;
            if (pSet != null && pSet.getIncomingCallsSetting() != PrivacySettings.REAL) {
                output = "";
                realListener.onCallStateChanged(state, output);
            } else {
                output = incomingNumber;
                realListener.onCallStateChanged(state, incomingNumber);
            }
            Log.d(TAG, "onCallStateChanged (incoming number) - " + mContext.getPackageName() + " (" + 
                    Binder.getCallingUid() + ") output: " + output);
        }
        
        /**
         * Does not call the real listeners method if network location is restricted
         * by privacy settings
         */
        @Override
        public void onCellLocationChanged(CellLocation location) {
            // TODO: save settings locally and invalidate when updated to avoid continuous calls
            PrivacySettings pSet = mPrivSetManager.getSettings(packageName, uid);
            String output;
            if (pSet != null && pSet.getLocationNetworkSetting() != PrivacySettings.REAL) {
                // simply block the method call, since simulating cell location is not feasible
                output = "[no output]";
            } else {
                output = location.toString();
                realListener.onCellLocationChanged(location);
            }
            Log.d(TAG, "onCellLocationChanged - " + mContext.getPackageName() + " (" + 
                    Binder.getCallingUid() + ") output: " + output);
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
