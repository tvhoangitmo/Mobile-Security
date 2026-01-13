# Solution

## Description of the problem

The `justlisten` challenge requires writing an Android application that listens for a broadcast Intent from the MobiSec system and logs the flag when received. The MobiSec system sends a broadcast Intent with action `com.mobisec.intent.action.FLAG_ANNOUNCEMENT` and extra key `"flag"` containing the flag value. Our app must register a BroadcastReceiver to receive this Intent, extract the flag, and log it to Logcat.

## Solution

I've solved the challenge by creating a custom BroadcastReceiver class, registering it dynamically in Activity lifecycle methods (`onStart()` and `onStop()`), and handling the received Intent to extract and log the flag.

**FlagReceiver.java** - Custom BroadcastReceiver to handle broadcast Intent:

```java
public class FlagReceiver extends BroadcastReceiver {

    public static final String ACTION_FLAG =
            "com.mobisec.intent.action.FLAG_ANNOUNCEMENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (ACTION_FLAG.equals(action)) {
            String flag = intent.getStringExtra("flag");
            Log.i("MOBISEC", "Received flag: " + flag);
        }
    }
}
```

**MainActivity.java** - Register receiver in `onStart()`:

```java
@Override
protected void onStart() {
    super.onStart();
    flagReceiver = new FlagReceiver();
    IntentFilter filter = new IntentFilter(FlagReceiver.ACTION_FLAG);
    registerReceiver(flagReceiver, filter);
}
```

**MainActivity.java** - Unregister receiver in `onStop()`:

```java
@Override
protected void onStop() {
    super.onStop();
    if (flagReceiver != null) {
        unregisterReceiver(flagReceiver);
        flagReceiver = null;
    }
}
```

**Flag**: `MOBISEC{not_sure_Ive_heard_well_what_did_ya_say?!?}`
