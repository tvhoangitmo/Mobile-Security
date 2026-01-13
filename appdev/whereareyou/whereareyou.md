# Solution

## Description of the problem

The `whereareyou` challenge requires writing an Android application that provides a location service. The MobiSec system sends an Intent with action `com.mobisec.intent.action.GIMMELOCATION` to start the service. The service must retrieve the device's location (GPS or Network) and broadcast it via an Intent with action `com.mobisec.intent.action.LOCATION_ANNOUNCEMENT` and extra key `"location"` containing the Location object.

## Solution

I've solved the challenge by creating a Service that can be started by other applications, using LocationManager to retrieve device location (tries GPS first, then Network provider), and broadcasting the location via a broadcast Intent.

**AndroidManifest.xml** - Added location permissions and Service declaration:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<service
    android:name=".LocationService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.mobisec.intent.action.GIMMELOCATION" />
    </intent-filter>
</service>
```

**LocationService.java** - `onStartCommand()` retrieves location:

```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    
    try {
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null) {
            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        
        if (loc != null) {
            broadcastLocation(loc);
        } else {
            lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    broadcastLocation(location);
                }
                // ... other methods
            }, null);
        }
    } catch (SecurityException e) {
        e.printStackTrace();
    }
    
    return START_NOT_STICKY;
}
```

**LocationService.java** - `broadcastLocation()` broadcasts the location:

```java
private void broadcastLocation(Location loc) {
    Intent i = new Intent();
    i.setAction("com.mobisec.intent.action.LOCATION_ANNOUNCEMENT");
    i.putExtra("location", loc);
    sendBroadcast(i);
}
```

**Flag**: `MOBISEC{Where_are_you_bro?_Will_not_tell_anybody_I_swear}`
