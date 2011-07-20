package android.privacy.surrogate;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Criteria;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.GpsStatus.NmeaListener;
import android.os.Binder;
import android.os.Looper;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.util.Log;

public class PrivacyLocationManager extends LocationManager {

    private static final String TAG = "PrivacyLocationManager";
    
    private Context mContext;
    
    private PrivacySettingsManager mPrivSetManager;
    
    public PrivacyLocationManager(ILocationManager service, Context context) {
        super(service);
        this.mContext = context;
        mPrivSetManager = (PrivacySettingsManager) mContext.getSystemService("privacy");
    }

    @Override
    public boolean addNmeaListener(NmeaListener listener) {
        // TODO: implement a custom listener
        Log.d(TAG, "addNmeaListener request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        if (pSet.getLocationGpsSetting() != PrivacySettings.REAL) return false;
        
        return super.addNmeaListener(listener);
    }

    @Override
    public Location getLastKnownLocation(String provider) {
        if (provider == null) return super.getLastKnownLocation(provider);
        
        Log.d(TAG, "getLastKnownLocation request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            switch (pSet.getLocationGpsSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                    return null;
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    Location l = new Location(provider);
                    l.setLatitude(Double.parseDouble(pSet.getLocationGpsLat()));
                    l.setLongitude(Double.parseDouble(pSet.getLocationGpsLon()));
                    return l;
            }
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            switch (pSet.getLocationNetworkSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                    return null;
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    Location l = new Location(provider);
                    l.setLatitude(Double.parseDouble(pSet.getLocationNetworkLat()));
                    l.setLongitude(Double.parseDouble(pSet.getLocationNetworkLon()));
                    return l;
            }
        } else if (provider.equals(LocationManager.PASSIVE_PROVIDER)) { // could get location from any of above
            byte settingGps = pSet.getLocationGpsSetting();
            byte settingNetwork = pSet.getLocationNetworkSetting();
            if (settingGps != PrivacySettings.REAL || settingNetwork != PrivacySettings.REAL) {
                // assuming setting priorities: EMPTY < CUSTOM < RANDOM
                if (settingGps > settingNetwork) {
                    switch (settingGps) {
                        case PrivacySettings.REAL:
                            break;
                        case PrivacySettings.EMPTY:
                            return null;
                        case PrivacySettings.CUSTOM:
                        case PrivacySettings.RANDOM:
                            Location l = new Location(provider);
                            l.setLatitude(Double.parseDouble(pSet.getLocationGpsLat()));
                            l.setLongitude(Double.parseDouble(pSet.getLocationGpsLon()));
                            return l;
                    }
                } else {
                    switch (settingNetwork) {
                        case PrivacySettings.REAL:
                            break;
                        case PrivacySettings.EMPTY:
                            return null;
                        case PrivacySettings.CUSTOM:
                        case PrivacySettings.RANDOM:
                            Location l = new Location(provider);
                            l.setLatitude(Double.parseDouble(pSet.getLocationNetworkLat()));
                            l.setLongitude(Double.parseDouble(pSet.getLocationNetworkLon()));
                            return l;
                    }
                }
                
            }
            
        }

        return super.getLastKnownLocation(provider);
    }

    @Override
    public LocationProvider getProvider(String name) {
        Log.d(TAG, "getProvider request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        
        if (name.equals(LocationManager.GPS_PROVIDER)) {
            switch (pSet.getLocationGpsSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                    return null;
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    // TODO: implement custom provider
                    return null;
            }
        } else if (name.equals(LocationManager.NETWORK_PROVIDER)) {
            switch (pSet.getLocationNetworkSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                    return null;
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    // TODO: implement custom provider
                    return null;
            }
        } else if (name.equals(LocationManager.PASSIVE_PROVIDER)) { // could get location from any of above
            if (pSet.getLocationGpsSetting() != PrivacySettings.REAL || 
                    pSet.getLocationNetworkSetting() != PrivacySettings.REAL) {
                // TODO: implement custom provider
                return null;
            }
        }
            
        return super.getProvider(name);
    }

    @Override
    public boolean isProviderEnabled(String provider) {
        Log.d(TAG, "getProvider request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            switch (pSet.getLocationGpsSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    // TODO: implement custom provider
                    return false;
            }
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            switch (pSet.getLocationNetworkSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    // TODO: implement custom provider
                    return false;
            }
        } else if (provider.equals(LocationManager.PASSIVE_PROVIDER)) { // could get location from any of above
            if (pSet.getLocationGpsSetting() != PrivacySettings.REAL || 
                    pSet.getLocationNetworkSetting() != PrivacySettings.REAL) {
                // TODO: implement custom provider
                return false;
            }
        }
        
        return super.isProviderEnabled(provider);
    }

    @Override
    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener,
            Looper looper) {
        if (requestLocationUpdates(criteria)) return;
        super.requestLocationUpdates(minTime, minDistance, criteria, listener, looper);
    }

    @Override
    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent) {
        if (requestLocationUpdates(criteria)) return;
        super.requestLocationUpdates(minTime, minDistance, criteria, intent);
    }

    @Override
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener,
            Looper looper) {
        if (requestLocationUpdates(provider)) return;
        super.requestLocationUpdates(provider, minTime, minDistance, listener, looper);
    }

    @Override
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener) {
        if (requestLocationUpdates(provider)) return;
        super.requestLocationUpdates(provider, minTime, minDistance, listener);
    }

    @Override
    public void requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent) {
        if (requestLocationUpdates(provider)) return;
        super.requestLocationUpdates(provider, minTime, minDistance, intent);
    }

    @Override
    public void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper) {
        if (requestLocationUpdates(criteria)) return;
        super.requestSingleUpdate(criteria, listener, looper);
    }

    @Override
    public void requestSingleUpdate(Criteria criteria, PendingIntent intent) {
        if (requestLocationUpdates(criteria)) return;
        super.requestSingleUpdate(criteria, intent);
    }

    @Override
    public void requestSingleUpdate(String provider, LocationListener listener, Looper looper) {
        if (requestLocationUpdates(provider)) return;
        super.requestSingleUpdate(provider, listener, looper);
    }

    @Override
    public void requestSingleUpdate(String provider, PendingIntent intent) {
        if (requestLocationUpdates(provider)) return;
        super.requestSingleUpdate(provider, intent);
    }
    
    /**
     * @return true, if action has been taken
     *         false, if the processing needs to be passed to the default method
     */
    private boolean requestLocationUpdates(String provider) {
        Log.d(TAG, "requestLocationUpdates request from package: " + mContext.getPackageName() + " UID: " + Binder.getCallingUid());
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            switch (pSet.getLocationGpsSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    // TODO: implement custom provider
                    return true;
            }
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            switch (pSet.getLocationNetworkSetting()) {
                case PrivacySettings.REAL:
                    break;
                case PrivacySettings.EMPTY:
                case PrivacySettings.CUSTOM:
                case PrivacySettings.RANDOM:
                    // TODO: implement custom provider
                    return true;
            }
        } else if (provider.equals(LocationManager.PASSIVE_PROVIDER)) { // could get location from any of above
            if (pSet.getLocationGpsSetting() != PrivacySettings.REAL || 
                    pSet.getLocationNetworkSetting() != PrivacySettings.REAL) {
                // TODO: implement custom provider
                return true;
            }
        }
        
        return false;
    }
    
    private boolean requestLocationUpdates(Criteria criteria) {
        if (criteria == null) return false;
            // treat providers with high accuracy as GPS providers
        else if (criteria.getAccuracy() == Criteria.ACCURACY_FINE || 
                criteria.getBearingAccuracy() == Criteria.ACCURACY_HIGH || 
                criteria.getHorizontalAccuracy() == Criteria.ACCURACY_HIGH || 
                criteria.getVerticalAccuracy() == Criteria.ACCURACY_HIGH || 
                criteria.getSpeedAccuracy() == Criteria.ACCURACY_HIGH) {
            return requestLocationUpdates(LocationManager.GPS_PROVIDER);
        } else { // treat all others as network providers
            return requestLocationUpdates(LocationManager.NETWORK_PROVIDER);
        }
    }

}
