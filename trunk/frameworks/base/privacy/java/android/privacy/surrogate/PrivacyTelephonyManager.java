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
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

public class PrivacyTelephonyManager extends TelephonyManager {

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
        Log.d(TAG, "getDeviceId request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output;
        if (pSet != null && pSet.getDeviceIdSetting() != PrivacySettings.REAL) {
            output = pSet.getDeviceId(); // can be empty, custom or random
        } else {
            output = super.getDeviceId();
        }
        Log.d(TAG, "getDeviceId output: " + output);
        return output;
    }
    
    @Override
    public String getLine1Number() {
        Log.d(TAG, "getLine1Number request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getLine1NumberOrVoiceMailNumber();
        if (output == null) output = super.getLine1Number();
        Log.d(TAG, "getLine1Number output: " + output);
        return output;
    }
    
    /**
     * Will be handled alongside the Line1Number, since voice mailbox numbers often
     * are similar to the phone number of the subscriber.
     */
    @Override
    public String getVoiceMailNumber() {
        Log.d(TAG, "getVoiceMailNumber request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getLine1NumberOrVoiceMailNumber();
        if (output == null) output = super.getVoiceMailNumber();
        Log.d(TAG, "getVoiceMailNumber output: " + output);
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
     * For monitoring purposes only
     */
    @Override
    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        Log.d(TAG, "getNeighboringCellInfo request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        return super.getNeighboringCellInfo();
    }
    
    @Override
    public String getNetworkCountryIso() {
        Log.d(TAG, "getNetworkCountryIso request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkCountryIso();
        Log.d(TAG, "getNetworkCountryIso output: " + output);
        return output;
    }

    @Override
    public String getNetworkOperator() {
        Log.d(TAG, "getNetworkOperator request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkOperator();
        Log.d(TAG, "getNetworkOperator output: " + output);
        return output;
    }

    @Override
    public String getNetworkOperatorName() {
        Log.d(TAG, "getNetworkOperatorName request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getNetworkInfo();
        if (output == null) output = super.getNetworkOperatorName();
        Log.d(TAG, "getNetworkOperatorName output: " + output);
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
        Log.d(TAG, "getSimCountryIso request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getSimInfo();
        if (output == null) output = super.getSimCountryIso();
        Log.d(TAG, "getSimCountryIso output: " + output);        
        return output;
    }

    @Override
    public String getSimOperator() {
        Log.d(TAG, "getSimOperator request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getSimInfo();
        if (output == null) output = super.getSimOperator();
        Log.d(TAG, "getSimOperator output: " + output); 
        return output;
    }

    @Override
    public String getSimOperatorName() {
        Log.d(TAG, "getSimOperatorName request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        String output = getSimInfo();
        if (output == null) output = super.getSimOperatorName();
        Log.d(TAG, "getSimOperatorName output: " + output);         
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
        // TODO Auto-generated method stub
        return super.getSimSerialNumber();
    }
    
    /**
     * IMSI
     */
    @Override
    public String getSubscriberId() {
        // TODO Auto-generated method stub
        return super.getSubscriberId();
    }

    @Override
    public void enableLocationUpdates() {
        Log.d(TAG, "enableLocationUpdates request from package: " + mContext.getPackageName() + " UID: " + 
                Binder.getCallingUid());
        super.enableLocationUpdates();
    }

    @Override
    public void listen(PhoneStateListener listener, int events) {
        Log.d(TAG, "listen request from package: " + mContext.getPackageName() + " UID: " + 
                Binder.getCallingUid() + "; listener: " + listener + "; events: " + events);
        
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        if (pSet != null && pSet.getLocationNetworkSetting() != PrivacySettings.REAL && 
                ((events & PhoneStateListener.LISTEN_CELL_LOCATION) != 0)) {
            Log.d(TAG, "listen for cell location request: " + mContext.getPackageName() + " UID: " + 
                    Binder.getCallingUid());
            switch (pSet.getLocationNetworkSetting()) {
                case PrivacySettings.EMPTY:
                    return;
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    // TODO: implement custom listener
                    return;
            }
        }
        
        super.listen(listener, events);
    }
}
