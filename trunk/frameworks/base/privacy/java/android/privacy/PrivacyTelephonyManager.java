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

package android.privacy;

import android.content.Context;
import android.os.Binder;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PrivacyTelephonyManager extends TelephonyManager {

    private static final String TAG = "PrivacyTelephonyManager";
    private Context mContext;
    private PrivacySettingsManager mPrivSetManager;

    public PrivacyTelephonyManager(Context context) {
        super(context);
        mContext = context;
        mPrivSetManager = new PrivacySettingsManager(mContext);
    }
    
    @Override
    public String getDeviceId() {
        PrivacySettings pSettings = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        String customId = pSettings.getDeviceId();
        if (customId != null) return customId;
        return super.getDeviceId();
    }
    
    @Override
    public String getLine1Number() {
        // TODO Auto-generated method stub
        return super.getLine1Number();
    }
}
