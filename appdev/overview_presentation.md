# MobiSec AppDev Challenges - Overview Presentation

## Introduction

This presentation covers 9 Android application development challenges from MobiSec, demonstrating various Android inter-component communication mechanisms and security concepts.

---

## 1. helloworld - Basic Logging

**Problem**: Print a specific string "hello-world-mobisec-edition" with tag "MOBISEC" using Android Logging API.

**Solution**: Use `Log.d()` in `onCreate()` method.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.d("MOBISEC", "hello-world-mobisec-edition");
}
```

**Function Explanation**:
- `onCreate(Bundle savedInstanceState)`: Called when Activity is first created. This is where we initialize the Activity and log the required message.
- `Log.d(String tag, String msg)`: Debug-level logging. First parameter is the tag "MOBISEC", second is the message to log.

**Key Concept**: Android Logging API basics.

**Flag**: `MOBISEC{here_there_is_your_first_and_last_charity_point}`

---

## 2. justlisten - BroadcastReceiver

**Problem**: Listen for a broadcast Intent with action `com.mobisec.intent.action.FLAG_ANNOUNCEMENT` and extract the flag from the Intent extra.

**Solution**: Create a custom BroadcastReceiver, register it dynamically in `onStart()`, and unregister in `onStop()`.

**FlagReceiver.java**:
```java
public class FlagReceiver extends BroadcastReceiver {
    public static final String ACTION_FLAG = "com.mobisec.intent.action.FLAG_ANNOUNCEMENT";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_FLAG.equals(intent.getAction())) {
            String flag = intent.getStringExtra("flag");
            Log.i("MOBISEC", "Received flag: " + flag);
        }
    }
}
```
- `onReceive(Context context, Intent intent)`: Called when a broadcast matching the IntentFilter is received. Checks if action matches, extracts flag from Intent extra, and logs it.

**MainActivity.java**:
```java
@Override
protected void onStart() {
    super.onStart();
    flagReceiver = new FlagReceiver();
    IntentFilter filter = new IntentFilter(FlagReceiver.ACTION_FLAG);
    registerReceiver(flagReceiver, filter);
}
```
- `onStart()`: Activity lifecycle method called when Activity becomes visible. This is where we register the receiver.
- `registerReceiver(BroadcastReceiver receiver, IntentFilter filter)`: Dynamically registers the receiver to listen for broadcasts matching the filter. Must be called in `onStart()`.

```java
@Override
protected void onStop() {
    super.onStop();
    if (flagReceiver != null) {
        unregisterReceiver(flagReceiver);
    }
}
```
- `onStop()`: Activity lifecycle method called when Activity is no longer visible. This is where we unregister the receiver to free resources.
- `unregisterReceiver(BroadcastReceiver receiver)`: Unregisters the receiver to prevent memory leaks. Must be called in `onStop()`.

**Key Concept**: Dynamic BroadcastReceiver registration and lifecycle management.

**Flag**: `MOBISEC{not_sure_Ive_heard_well_what_did_ya_say?!?}`

---

## 3. reachingout - HTTP Communication

**Problem**: Communicate with HTTP server at `http://10.0.2.2:31337/flag`, parse HTML form, solve math problem, and submit POST request.

**Solution**: 
1. GET request to retrieve HTML form
2. Parse HTML to extract math values (`val1`, `oper`, `val2`)
3. Calculate answer
4. POST request with answer

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<application android:usesCleartextTraffic="true">
```

**MainActivity.java** - Extract value from HTML:
```java
private String extractValue(String html, String id) {
    String marker = "id=\"" + id + "\"";
    int idx = html.indexOf(marker);
    int vIdx = html.indexOf("value=\"", idx) + "value=\"".length();
    int end = html.indexOf("\"", vIdx);
    return html.substring(vIdx, end);
}
```
- `extractValue(String html, String id)`: Parses HTML string to extract value from input field with given ID. Finds the marker `id="..."`, then locates the `value="..."` attribute and extracts its content.

**MainActivity.java** - HTTP POST request:
```java
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("POST");
conn.setDoOutput(true);
String body = "answer=" + result + "&val1=" + val1 + "&oper=" + oper + "&val2=" + val2;
```
- `HttpURLConnection.setRequestMethod(String method)`: Sets HTTP method (GET or POST).
- `HttpURLConnection.setDoOutput(boolean doOutput)`: Enables output stream for POST requests.
- `URLEncoder.encode(String s, String enc)`: URL-encodes string values for safe transmission in HTTP requests (used when building the body string).

**Key Concept**: HTTP GET/POST, HTML parsing, form submission.

**Flag**: `MOBISEC{I_was_told_by_liars_that_http_queries_were_easy}`

---

## 4. justask - Activity Results

**Problem**: Call four Activities (`PartOne`, `PartTwo`, `PartThree`, `PartFour`) and extract flag parts from their result Intents. Each returns flag part in different format.

**Solution**: Use `startActivityForResult()` sequentially, extract parts using different methods (direct extra, hidden extra, recursive Bundle search).

**MainActivity.java** - Call Activity:
```java
private void askPartOne() {
    Intent i = new Intent();
    i.setClassName("com.mobisec.justask", "com.mobisec.justask.PartOne");
    startActivityForResult(i, REQ_1);
}
```
- `startActivityForResult(Intent intent, int requestCode)`: Starts an Activity and expects a result back. The requestCode identifies which Activity call this result corresponds to.

**MainActivity.java** - Handle Activity result:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
        case REQ_1:
            parts[0] = data.getStringExtra("flag");
            askPartTwo();
            break;
        case REQ_3:
            parts[2] = data.getStringExtra("hiddenFlag");
            break;
        case REQ_4:
            Bundle follow = data.getBundleExtra("follow");
            parts[3] = findStringDeep(follow);
            break;
    }
}
```
- `onActivityResult(int requestCode, int resultCode, Intent data)`: Called when the started Activity finishes and returns a result. Checks requestCode to identify which Activity returned, extracts flag part from Intent extras, and chains to next Activity if needed.
- `getStringExtra(String name)`: Extracts String value from Intent extras by key name.
- `getBundleExtra(String name)`: Extracts Bundle object from Intent extras.

**MainActivity.java** - Recursive search for nested Bundle:
```java
private String findStringDeep(Bundle b) {
    for (String k : b.keySet()) {
        Object v = b.get(k);
        if (v instanceof String) return (String) v;
        if (v instanceof Bundle) {
            String inner = findStringDeep((Bundle) v);
            if (inner != null) return inner;
        }
    }
    return null;
}
```
- `findStringDeep(Bundle b)`: Recursively searches through Bundle and nested Bundles to find the first String value. Iterates through all keys, checks if value is String (returns it) or Bundle (recursively searches), handles nested structures.

**Key Concept**: Activity result handling, Intent extras, nested Bundle parsing.

**Flag**: `MOBISEC{Ive_asked_and_I_got_the_flag_how_nice!}`

---

## 5. filehasher - Intent Service & SHA-256

**Problem**: Act as SHA-256 hash service. Receive Intent with file URI, calculate SHA-256, return hash via result Intent.

**Solution**: Handle Intent with action `com.mobisec.intent.action.HASHFILE`, read file via ContentResolver, calculate SHA-256, return result.

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<activity android:exported="true">
    <intent-filter>
        <action android:name="com.mobisec.intent.action.HASHFILE" />
    </intent-filter>
</activity>
```

**MainActivity.java** - Handle incoming Intent:
```java
private void handleIntent(Intent intent) {
    Uri fileUri = intent.getData();
    String hashHex = computeSha256OfUri(fileUri);
    
    Intent resultIntent = new Intent();
    resultIntent.putExtra("hash", hashHex);
    setResult(Activity.RESULT_OK, resultIntent);
    finish();
}
```
- `handleIntent(Intent intent)`: Processes incoming Intent. Checks if action matches `HASHFILE`, extracts file URI from Intent data, computes SHA-256 hash, creates result Intent with hash, sets result and finishes Activity.
- `setResult(int resultCode, Intent data)`: Sets result to return to calling Activity with result code and data Intent.

**MainActivity.java** - Compute SHA-256 hash:
```java
private String computeSha256OfUri(Uri uri) {
    InputStream is = getContentResolver().openInputStream(uri);
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] buffer = new byte[4096];
    int read;
    while ((read = is.read(buffer)) != -1) {
        digest.update(buffer, 0, read);
    }
    return bytesToHex(digest.digest());
}
```
- `computeSha256OfUri(Uri uri)`: Reads file content via ContentResolver, calculates SHA-256 hash using MessageDigest. Opens InputStream from URI, reads file in chunks (4096 bytes), updates digest with each chunk, converts final digest to hex string.
- `getContentResolver().openInputStream(Uri uri)`: Opens InputStream for reading file content from URI (handles file://, content:// schemes).
- `MessageDigest.getInstance(String algorithm)`: Gets MessageDigest instance for SHA-256 algorithm.
- `digest.update(byte[] input, int offset, int len)`: Updates digest with data chunk.
- `digest.digest()`: Completes hash computation and returns final hash bytes.
- `bytesToHex(byte[] bytes)`: Converts byte array to hexadecimal string. Each byte is converted to 2 hex characters (0-9, a-f).

**Key Concept**: Intent filters, ContentResolver, SHA-256 hashing, file I/O.

**Flag**: `MOBISEC{Was_it_known_that_these_one_way_functions_give_you_back_flags?}`

---

## 6. whereareyou - Location Service

**Problem**: Provide location service. Receive Intent to start service, retrieve device location, broadcast it.

**Solution**: Create Service with exported=true, use LocationManager to get location (GPS or Network), broadcast via Intent.

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<service android:name=".LocationService" android:exported="true">
    <intent-filter>
        <action android:name="com.mobisec.intent.action.GIMMELOCATION" />
    </intent-filter>
</service>
```

**LocationService.java** - Service start command:
```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    if (loc == null) {
        loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
    if (loc != null) {
        broadcastLocation(loc);
    }
    return START_NOT_STICKY;
}
```
- `onStartCommand(Intent intent, int flags, int startId)`: Called when Service is started via `startService()`. Gets LocationManager system service, tries to get last known location from GPS provider first, falls back to Network provider if GPS unavailable, broadcasts location if found, returns `START_NOT_STICKY` (Service won't restart if killed).
- `getSystemService(String name)`: Gets system service (LOCATION_SERVICE returns LocationManager).
- `getLastKnownLocation(String provider)`: Gets last known location from specified provider (GPS_PROVIDER or NETWORK_PROVIDER). Returns null if no location available.

**LocationService.java** - Broadcast location:
```java
private void broadcastLocation(Location loc) {
    Intent i = new Intent("com.mobisec.intent.action.LOCATION_ANNOUNCEMENT");
    i.putExtra("location", loc);
    sendBroadcast(i);
}
```
- `broadcastLocation(Location loc)`: Creates broadcast Intent with action `LOCATION_ANNOUNCEMENT`, adds Location object as extra, sends broadcast.
- `sendBroadcast(Intent intent)`: Sends broadcast Intent to all registered receivers.

**Key Concept**: Android Services, LocationManager, location permissions, broadcast from service.

**Flag**: `MOBISEC{Where_are_you_bro?_Will_not_tell_anybody_I_swear}`

---

## 7. jokeprovider - Content Provider

**Problem**: Query Content Provider at `content://com.mobisec.provider.Joke/jokes`, filter by author "reyammer", extract jokes to form flag.

**Solution**: Use ContentResolver to query with selection filter, iterate Cursor, extract joke text.

**MainActivity.java** - Query Content Provider:
```java
private void fetchFlag() {
    StringBuilder flag = new StringBuilder();
    Uri JOKES_URI = Uri.parse("content://com.mobisec.provider.Joke/jokes");
    
    String selection = "author = ?";
    String[] selectionArgs = new String[]{"reyammer"};
    
    Cursor c = getContentResolver().query(JOKES_URI, null, selection, selectionArgs, null);
    
    int jokeIdx = c.getColumnIndex("joke");
    while (c.moveToNext()) {
        String joke = c.getString(jokeIdx);
        flag.append(joke);
    }
    c.close();
    
    Log.i(TAG, "FLAG: " + flag.toString());
}
```
- `fetchFlag()`: Main function to query Content Provider and extract flag. Creates URI, sets up selection filter for author "reyammer", queries Content Provider, iterates Cursor results, extracts joke text, combines into flag string.
- `Uri.parse(String uriString)`: Parses URI string into Uri object for Content Provider access.
- `getContentResolver().query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)`: Queries Content Provider. Parameters: URI to query, columns to return (null = all), WHERE clause, selection arguments, sort order. Returns Cursor with results.
- `getColumnIndex(String columnName)`: Gets index of column in Cursor by name.
- `moveToNext()`: Moves Cursor to next row. Returns false if no more rows.
- `getString(int columnIndex)`: Gets String value from current row at specified column index.
- `close()`: Closes Cursor to free resources.

**Key Concept**: ContentResolver, Content Provider queries, Cursor handling, SQL-like selection.

**Flag**: `MOBISEC{lol_roftl_ahahah_:D_REYAMMER_TELLS_THE_BEST_JOKES!}`

---

## 8. unbindable - Service Binding & Messenger

**Problem**: Bind to external Service `com.mobisec.unbindable.UnbindableService`, communicate via Messenger, request flag using `MSG_GET_FLAG`.

**Solution**: Bind to service, create Messenger objects, register as client, send message, receive reply.

**MainActivity.java** - Bind to service:
```java
private void bindToUnbindableService() {
    Intent i = new Intent();
    i.setClassName("com.mobisec.unbindable", "com.mobisec.unbindable.UnbindableService");
    bindService(i, conn, Context.BIND_AUTO_CREATE);
}
```
- `bindToUnbindableService()`: Initiates binding to external service. Creates Intent targeting specific service class, calls `bindService()` with ServiceConnection callback.
- `bindService(Intent service, ServiceConnection conn, int flags)`: Binds to service. ServiceConnection receives callbacks when binding succeeds/fails. `BIND_AUTO_CREATE` flag creates service if not running.

**MainActivity.java** - Service connection callback:
```java
private final ServiceConnection conn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        remoteMessenger = new Messenger(service);
        replyMessenger = new Messenger(new IncomingHandler());
        
        // Register client
        Message reg = Message.obtain(null, MSG_REGISTER_CLIENT);
        reg.replyTo = replyMessenger;
        remoteMessenger.send(reg);
        
        // Request flag
        Message getFlag = Message.obtain(null, MSG_GET_FLAG);
        getFlag.replyTo = replyMessenger;
        remoteMessenger.send(getFlag);
    }
};
```
- `onServiceConnected(ComponentName name, IBinder service)`: Called when service binding succeeds. Creates Messenger from IBinder (for sending to service), creates reply Messenger with IncomingHandler (for receiving replies), registers client by sending MSG_REGISTER_CLIENT, requests flag by sending MSG_GET_FLAG.
- `Message.obtain(Handler h, int what)`: Creates Message object. `what` is message type identifier.
- `Messenger.send(Message msg)`: Sends message to service via Messenger.

**MainActivity.java** - Handle incoming messages:
```java
private class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MSG_GET_FLAG && msg.obj instanceof Bundle) {
            Bundle b = (Bundle) msg.obj;
            String flag = b.getString("flag");
            Log.i(TAG, "FLAG: " + flag);
        }
    }
}
```
- `handleMessage(Message msg)`: Handler method called when message is received from service. Checks message type (`msg.what`), extracts Bundle from `msg.obj`, gets flag string from Bundle, logs flag.

**Key Concept**: Service binding, Messenger IPC, Handler for message handling, bidirectional communication.

**Flag**: `MOBISEC{please_respect_my_will_you_shall_not_bind_me_my_friend}`

---

## 9. serialintent - Java Reflection

**Problem**: Call Activity `com.mobisec.serialintent.SerialActivity`, receive `FlagContainer` object, extract flag from private method `getFlag()`.

**Solution**: Call Activity, receive Serializable object, use Java reflection to access and call private method.

**MainActivity.java** - Launch Activity:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent i = new Intent();
    i.setClassName("com.mobisec.serialintent", "com.mobisec.serialintent.SerialActivity");
    startActivityForResult(i, REQ_SERIAL);
}
```
- `onCreate(Bundle savedInstanceState)`: Creates Intent targeting external SerialActivity, calls `startActivityForResult()` to launch it and wait for result.

**MainActivity.java** - Extract flag using reflection:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Object o = data.getSerializableExtra("flag");
    FlagContainer fc = (FlagContainer) o;
    
    try {
        // Use reflection to call private method
        Method m = fc.getClass().getDeclaredMethod("getFlag");
        m.setAccessible(true);
        String flag = (String) m.invoke(fc);
        Log.i(TAG, "FLAG: " + flag);
    } catch (Exception e) {
        Log.e(TAG, "Error", e);
    }
}
```
- `onActivityResult(int requestCode, int resultCode, Intent data)`: Called when SerialActivity finishes. Extracts Serializable object from Intent extra, casts to FlagContainer, uses reflection to call private method.
- `getSerializableExtra(String name)`: Extracts Serializable object from Intent extras. FlagContainer implements Serializable interface.
- `getClass()`: Gets Class object representing the object's type.
- `getDeclaredMethod(String name, Class<?>... parameterTypes)`: Gets Method object for declared method (including private methods). First parameter is method name "getFlag", no parameters needed.
- `setAccessible(boolean flag)`: Allows access to private/protected members. Must set to `true` to call private method.
- `invoke(Object obj, Object... args)`: Invokes the method on the object instance. Returns the method's return value (String flag).

**Key Concept**: Java reflection (`getDeclaredMethod`, `setAccessible`, `invoke`), private method access, serialization.

**Flag**: `MOBISEC{HOW_DID_YOU_DO_IT_THAT_WAS_SERIALLY_PRIVATE_STUFF1!!1!eleven!}`

---

## Summary

These 9 challenges demonstrate:

1. **Basic Android APIs**: Logging
2. **Broadcast Communication**: BroadcastReceiver
3. **Network Communication**: HTTP GET/POST
4. **Activity Communication**: Activity results, Intent extras
5. **Intent Services**: Intent filters, ContentResolver
6. **Background Services**: Location services, Service lifecycle
7. **Data Access**: Content Providers, Cursor queries
8. **IPC Mechanisms**: Service binding, Messenger
9. **Reflection & Security**: Private method access, serialization

Each challenge builds on Android's inter-component communication mechanisms, demonstrating practical security concepts and Android development patterns.
