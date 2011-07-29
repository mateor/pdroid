package android.privacy.surrogate;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.GpsStatus.NmeaListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.util.Log;

public class PrivacyLocationManager extends LocationManager {

    private static final String TAG = "PrivacyLocationManager";
    
    private Context mContext;
    
    private PrivacySettingsManager mPrivSetManager;
    
    private Object lock = new Object();
    
    public PrivacyLocationManager(ILocationManager service, Context context) {
        super(service);
        this.mContext = context;
        mPrivSetManager = (PrivacySettingsManager) mContext.getSystemService("privacy");
    }

    @Override
    public boolean addNmeaListener(NmeaListener listener) {
        // TODO: implement a custom listener
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        if (pSet.getLocationGpsSetting() != PrivacySettings.REAL) return false;
        Log.d(TAG, "addNmeaListener - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: [real value]");
        
        return super.addNmeaListener(listener);
    }

    @Override
    public Location getLastKnownLocation(String provider) {
        if (provider == null) return super.getLastKnownLocation(provider);
        
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        Location output = null;
        
        if (pSet != null) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                switch (pSet.getLocationGpsSetting()) {
                    case PrivacySettings.REAL:
                        output = super.getLastKnownLocation(provider);
                        break;
                    case PrivacySettings.EMPTY:
                        break;
                    case PrivacySettings.CUSTOM:
                    case PrivacySettings.RANDOM:
                        output = new Location(provider);
                        output.setLatitude(Double.parseDouble(pSet.getLocationGpsLat()));
                        output.setLongitude(Double.parseDouble(pSet.getLocationGpsLon()));
                        break;
                }
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                switch (pSet.getLocationNetworkSetting()) {
                    case PrivacySettings.REAL:
                        output = super.getLastKnownLocation(provider);
                        break;
                    case PrivacySettings.EMPTY:
                        break;
                    case PrivacySettings.CUSTOM:
                    case PrivacySettings.RANDOM:
                        output = new Location(provider);
                        output.setLatitude(Double.parseDouble(pSet.getLocationNetworkLat()));
                        output.setLongitude(Double.parseDouble(pSet.getLocationNetworkLon()));
                        break;
                }
            } else if (provider.equals(LocationManager.PASSIVE_PROVIDER) && 
                    (pSet.getLocationGpsSetting() == PrivacySettings.REAL || 
                            pSet.getLocationNetworkSetting() == PrivacySettings.REAL)) {
                // only output real location if both gps and network are allowed
                output = super.getLastKnownLocation(provider);
            }
        } else {
            output = super.getLastKnownLocation(provider);
        }
        
        Log.d(TAG, "getLastKnownLocation - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + 
                ") output: " + output);
        return output;
    }

    @Override
    public LocationProvider getProvider(String name) {
        if (name == null) return super.getProvider(name);
        
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        LocationProvider output = null;
        
        if (pSet != null) {
            if (name.equals(LocationManager.GPS_PROVIDER)) {
                switch (pSet.getLocationGpsSetting()) {
                    case PrivacySettings.REAL:
                    case PrivacySettings.CUSTOM:
                    case PrivacySettings.RANDOM:
                        output = super.getProvider(name);
                        break;
                    case PrivacySettings.EMPTY:
                        break;
                }
            } else if (name.equals(LocationManager.NETWORK_PROVIDER)) {
                switch (pSet.getLocationNetworkSetting()) {
                    case PrivacySettings.REAL:
                    case PrivacySettings.CUSTOM:
                    case PrivacySettings.RANDOM:
                        output = super.getProvider(name);
                        break;
                    case PrivacySettings.EMPTY:
                        break;
                }
            } else if (name.equals(LocationManager.PASSIVE_PROVIDER)) { // could get location from any of above
                if (pSet.getLocationGpsSetting() == PrivacySettings.REAL || 
                        pSet.getLocationNetworkSetting() == PrivacySettings.REAL) {
                    output = super.getProvider(name);
                }
            }
        } else {
            output = super.getProvider(name);
        }
            
        Log.d(TAG, "getProvider - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") output: " + 
                (output != null ? "[real value]" : "[null]"));
        return output;
    }

    @Override
    public boolean isProviderEnabled(String provider) {
        if (provider == null) return super.isProviderEnabled(provider);
        
        PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
        boolean output = false;
        
        if (pSet != null) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                switch (pSet.getLocationGpsSetting()) {
                    case PrivacySettings.REAL:
                        output = super.isProviderEnabled(provider);
                        break;
                    case PrivacySettings.EMPTY:
                        break;
                    case PrivacySettings.CUSTOM:
                    case PrivacySettings.RANDOM:
                        output = true;
                        break;
                }
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                switch (pSet.getLocationNetworkSetting()) {
                    case PrivacySettings.REAL:
                        output = super.isProviderEnabled(provider);
                        break;
                    case PrivacySettings.EMPTY:
                        break;
                    case PrivacySettings.CUSTOM:
                    case PrivacySettings.RANDOM:
                        output = true;
                        break;
                }
            } else if (provider.equals(LocationManager.PASSIVE_PROVIDER)) { // could get location from any of above
                if (pSet.getLocationGpsSetting() == PrivacySettings.REAL || 
                        pSet.getLocationNetworkSetting() == PrivacySettings.REAL) {
                    output = super.isProviderEnabled(provider);
                } else {
                    output = false;
                }
            }
        } else { // if querying unknown provider
            output = super.isProviderEnabled(provider);
        }
        
        Log.d(TAG, "isProviderEnabled - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ") provider: " 
                + provider + "output: " + output);
        return output;
    }

    @Override
    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener,
            Looper looper) {
        if (criteria == null || listener == null) {
            super.requestLocationUpdates(minTime, minDistance, criteria, listener, looper);
            return;
        }
        if (requestLocationUpdates(criteria, listener, null)) return;
        super.requestLocationUpdates(minTime, minDistance, criteria, listener, looper);
    }

    @Override
    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent) {
        if (criteria == null || intent == null) {
            super.requestLocationUpdates(minTime, minDistance, criteria, intent);
            return;
        }
        if (requestLocationUpdates(criteria, null, intent)) return;
        super.requestLocationUpdates(minTime, minDistance, criteria, intent);
    }

    @Override
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener,
            Looper looper) {
        if (provider == null || listener == null) {
            super.requestLocationUpdates(provider, minTime, minDistance, listener, looper);
            return;
        }
        if (requestLocationUpdates(provider, listener, null)) return;
        super.requestLocationUpdates(provider, minTime, minDistance, listener, looper);
    }

    @Override
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener) {
        if (provider == null || listener == null) {
            super.requestLocationUpdates(provider, minTime, minDistance, listener);
            return;
        }
        if (requestLocationUpdates(provider, listener, null)) return;
        super.requestLocationUpdates(provider, minTime, minDistance, listener);
    }

    @Override
    public void requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent) {
        if (provider == null || intent == null) {
            super.requestLocationUpdates(provider, minTime, minDistance, intent);
            return;
        }
        if (requestLocationUpdates(provider, null, intent)) return;
        super.requestLocationUpdates(provider, minTime, minDistance, intent);
    }

    @Override
    public void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper) {
        if (criteria == null || listener == null) {
            super.requestSingleUpdate(criteria, listener, looper);
            return;
        }
        if (requestLocationUpdates(criteria, listener, null)) return;
        super.requestSingleUpdate(criteria, listener, looper);
    }

    @Override
    public void requestSingleUpdate(Criteria criteria, PendingIntent intent) {
        if (criteria == null || intent == null) {
            super.requestSingleUpdate(criteria, intent);
            return;
        }
        if (requestLocationUpdates(criteria, null, intent)) return;
        super.requestSingleUpdate(criteria, intent);
    }

    @Override
    public void requestSingleUpdate(String provider, LocationListener listener, Looper looper) {
        if (provider == null || listener == null) {
            super.requestSingleUpdate(provider, listener, looper);
            return;
        }
        if (requestLocationUpdates(provider, listener, null)) return;
        super.requestSingleUpdate(provider, listener, looper);
    }

    @Override
    public void requestSingleUpdate(String provider, PendingIntent intent) {
        if (provider == null || intent == null) {
            super.requestSingleUpdate(provider, intent);
            return;
        }
        if (requestLocationUpdates(provider, null, intent)) return;
        super.requestSingleUpdate(provider, intent);
    }
    
    @Override
    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        Log.d(TAG, "sendExtraCommand - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + ")");
        return super.sendExtraCommand(provider, command, extras);
    }

    /**
     * Handles calls to requestLocationUpdates and requestSingleUpdate methods
     * @return true, if action has been taken
     *         false, if the processing needs to be passed to the default method
     */
    private boolean requestLocationUpdates(String provider, LocationListener listener, PendingIntent intent) {
        synchronized (lock) { // custom listener should only return a value after this method has returned

            PrivacySettings pSet = mPrivSetManager.getSettings(mContext.getPackageName(), Binder.getCallingUid());
            boolean output = false;
            
            if (pSet != null) {
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    switch (pSet.getLocationGpsSetting()) {
                        case PrivacySettings.REAL:
                            break;
                        case PrivacySettings.EMPTY:
                            output = true;
                            break;
                        case PrivacySettings.CUSTOM:
                        case PrivacySettings.RANDOM:
                            try {
                                new PrivacyLocationUpdater(provider, listener, intent, 
                                        Double.parseDouble(pSet.getLocationGpsLat()), 
                                        Double.parseDouble(pSet.getLocationGpsLon())).start();
                                output = true;
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "requestLocationUpdates: invalid coordinates");
                                output = true;
                            }
                    }
                } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    switch (pSet.getLocationNetworkSetting()) {
                        case PrivacySettings.REAL:
                            break;
                        case PrivacySettings.EMPTY:
                            output = true;
                            break;
                        case PrivacySettings.CUSTOM:
                        case PrivacySettings.RANDOM:
                            try {
                                new PrivacyLocationUpdater(provider, listener, intent, 
                                        Double.parseDouble(pSet.getLocationNetworkLat()), 
                                        Double.parseDouble(pSet.getLocationNetworkLon())).start();
                                output = true;
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "requestLocationUpdates: invalid coordinates");
                                output = true;
                            }
                    }
                } else if (provider.equals(LocationManager.PASSIVE_PROVIDER)) { // could get location from any of above
                    if (pSet.getLocationGpsSetting() == PrivacySettings.REAL || 
                            pSet.getLocationNetworkSetting() == PrivacySettings.REAL) {
                        output = false;
                    } else {
                        output = true;
                    }
                }
            }
            
            Log.d(TAG, "requestLocationUpdates - " + mContext.getPackageName() + " (" + Binder.getCallingUid() + 
                    ") output: " + (output == true ? "[custom location]" : "[real value]"));
            return output;
        }
    }
    
    private boolean requestLocationUpdates(Criteria criteria, LocationListener listener, PendingIntent intent) {
        if (criteria == null) return false;
            // treat providers with high accuracy as GPS providers
        else if (criteria.getAccuracy() == Criteria.ACCURACY_FINE || 
                criteria.getBearingAccuracy() == Criteria.ACCURACY_HIGH || 
                criteria.getHorizontalAccuracy() == Criteria.ACCURACY_HIGH || 
                criteria.getVerticalAccuracy() == Criteria.ACCURACY_HIGH || 
                criteria.getSpeedAccuracy() == Criteria.ACCURACY_HIGH) {
            return requestLocationUpdates(LocationManager.GPS_PROVIDER, listener, intent);
        } else { // treat all others as network providers
            return requestLocationUpdates(LocationManager.NETWORK_PROVIDER, listener, intent);
        }
    }
    
    private class PrivacyLocationUpdater extends Thread {
        
        private String provider;
        
        private LocationListener listener;
        
        private PendingIntent intent;
        
        private double latitude;
        
        private double longitude;

        public PrivacyLocationUpdater(String provider, LocationListener listener, PendingIntent intent,
                double latitude, double longitude) {
            this.provider = provider;
            this.listener = listener;
            this.intent = intent;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public void run() {
            if (provider != null) {
                Location location = new Location(provider);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                
                synchronized (lock) {
                    if (listener != null) {
                        listener.onLocationChanged(location);
                    } else if (intent != null) {
                        Intent i = new Intent();
                        i.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
                    }
                }
            }
        }
        
    }

}
