# MobiSec Challenge – `justlisten` - Presentation Script

## 1. Introduction

Good morning/afternoon, teacher. Today I'll present my solution for the MobiSec `justlisten` challenge.

The challenge requires building an Android app that listens for a broadcast Intent from the MobiSec system. When MobiSec sends a broadcast with the flag, our app needs to receive it and log it to Logcat.

---

## 2. Solution Overview

Let me explain the approach briefly:

The MobiSec system sends a broadcast Intent with action `FLAG_ANNOUNCEMENT` and the flag in an extra. Our app needs to create a BroadcastReceiver to listen for this broadcast, register it when the Activity starts, and unregister it when the Activity stops to prevent memory leaks.

---

## 3. FlagReceiver Implementation

I created a custom BroadcastReceiver class called `FlagReceiver`. This class extends `BroadcastReceiver` and handles the incoming broadcast.

Here's the code:

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

Let me explain what this does:

First, I define a constant `ACTION_FLAG` that stores the action string. This makes it easy to reuse when creating the IntentFilter later.

In the `onReceive()` method, which is called automatically when a matching broadcast arrives, I first check if the Intent is null to avoid crashes.

Then I verify that the action matches our expected action. If it does, I extract the flag from the Intent extra using the key `"flag"`, and then log it to Logcat with the tag "MOBISEC" so we can easily filter the logs.

---

## 4. MainActivity - Receiver Registration

Now, I need to register this receiver so it can actually receive broadcasts. I do this in the MainActivity lifecycle methods.

I added a field to store the receiver instance, and then I register it in `onStart()` and unregister it in `onStop()`.

Here's the code:

```java
private FlagReceiver flagReceiver;

@Override
protected void onStart() {
    super.onStart();
    flagReceiver = new FlagReceiver();
    IntentFilter filter = new IntentFilter(FlagReceiver.ACTION_FLAG);
    registerReceiver(flagReceiver, filter);
}

@Override
protected void onStop() {
    super.onStop();
    if (flagReceiver != null) {
        unregisterReceiver(flagReceiver);
        flagReceiver = null;
    }
}
```

Let me explain why I do this:

In `onStart()`, I create a new receiver instance, create an IntentFilter with the action we want to listen for, and then register the receiver. This makes the receiver active and able to receive broadcasts.

In `onStop()`, I unregister the receiver and set it to null. This is crucial to prevent memory leaks. If we don't unregister, the receiver stays active even when the Activity is stopped, which wastes resources and can cause leaks.

I use `onStart()` and `onStop()` instead of `onCreate()` and `onDestroy()` because the receiver should only be active when the Activity is visible. This follows Android best practices.

---

## 5. Important Points

Let me highlight the key points:

**First**, this is a **dynamic receiver** - it's registered in code, not in the manifest. That's why we don't need to add anything to AndroidManifest.xml.

**Second**, proper lifecycle management is essential. Register in `onStart()` and unregister in `onStop()` to prevent memory leaks.

**Third**, the action must match exactly: `com.mobisec.intent.action.FLAG_ANNOUNCEMENT`. I store it as a constant for reuse.

**Fourth**, the extra key must be `"flag"` to extract the flag value correctly.

**Fifth**, I use Log.i() with tag "MOBISEC" so we can easily filter logs in Logcat using `adb logcat | grep MOBISEC`.

---

## 6. Complete Workflow

Let me summarize the complete workflow:

1. MobiSec system sends a broadcast Intent with action `FLAG_ANNOUNCEMENT` and extra `"flag"`.

2. Android System delivers the broadcast to all registered receivers matching the action.

3. Our app's `FlagReceiver.onReceive()` is called automatically.

4. The receiver checks the action, extracts the flag, and logs it.

5. We can see the flag in Logcat with tag "MOBISEC".

---

## 7. Conclusion

To summarize, my solution:
- Creates a custom BroadcastReceiver class
- Registers it dynamically in `onStart()`
- Unregisters it in `onStop()` to prevent leaks
- Extracts and logs the flag when received

The key to success is proper lifecycle management and correctly matching the Intent action and extra key.

Thank you for listening. I'm ready for any questions.

