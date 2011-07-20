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
import android.telephony.TelephonyManager;
import android.util.Log;

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
        Log.d(TAG, "Device ID request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output;
        if (pSet != null && pSet.getDeviceIdSetting() != PrivacySettings.REAL) {
            output = pSet.getDeviceId(); // can be empty, custom or random
        } else {
            output = super.getDeviceId();
        }
        Log.d(TAG, "Device ID output: " + output);
        return output;
    }
    
    @Override
    public String getLine1Number() {
        Log.d(TAG, "Phone number request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String output;
        if (pSet != null && pSet.getDeviceIdSetting() != PrivacySettings.REAL) {
            output = pSet.getLine1Number(); // can be empty, custom or random
        } else {
            output = super.getLine1Number();
        }
        Log.d(TAG, "Phone number output: " + output);
        return output;
    }
}
