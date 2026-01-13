# MobiSec Challenge – `filehasher` - Presentation

## Problem Description

The challenge requires writing an Android app that calculates SHA-256 hash for files. The MobiSec system creates a file, sends an Intent with action `HASHFILE` and file URI. The app must read the file, calculate SHA-256, and return the hash via result Intent with key `"hash"`. If the hash is correct, the system prints the flag.

---

## AndroidManifest.xml

In file AndroidManifest, I add intent-filter to receive Intent with action HASHFILE:

```xml
<intent-filter>
    <action android:name="com.mobisec.intent.action.HASHFILE" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

---

## MainActivity.java
In file  MainActivity, I wrote some functions as follows:
### onCreate() and onNewIntent()

In `onCreate()`, I call `handleIntent(getIntent())` to process the Intent when the Activity is created:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    handleIntent(getIntent());
}
```

In `onNewIntent()`, I call `handleIntent(intent)` to process a new Intent when the Activity already exists:

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIntent(intent);
}
```

### handleIntent(Intent intent)

This function handles the Intent from MobiSec system.

**This part checks if the action is `HASHFILE`. If not, return:**

```java
if (intent == null) {
    return;
}

String action = intent.getAction();
if (!ACTION_HASHFILE.equals(action)) {
    return;
}
```

**This part gets file Uri from `intent.getData()`. If there's no Uri, set result `RESULT_CANCELED` and finish:**

```java
Uri fileUri = intent.getData();
if (fileUri == null) {
    setResult(Activity.RESULT_CANCELED);
    finish();
    return;
}
```

**This part calls `computeSha256OfUri()` to calculate hash. If error, set result `RESULT_CANCELED` and finish:**

```java
String hashHex = computeSha256OfUri(fileUri);
if (hashHex == null) {
    setResult(Activity.RESULT_CANCELED);
    finish();
    return;
}
```

**This part creates result Intent, puts extra with key `"hash"` and hash value, sets result `RESULT_OK`, then finishes:**

```java
Intent resultIntent = new Intent();
resultIntent.putExtra("hash", hashHex);
setResult(Activity.RESULT_OK, resultIntent);
finish();
```

### computeSha256OfUri(Uri uri)

This function reads the file and calculates SHA-256.

**This part uses `getContentResolver().openInputStream(uri)` to open InputStream from Uri:**

```java
is = getContentResolver().openInputStream(uri);
if (is == null) {
    return null;
}
```

**This part initializes `MessageDigest.getInstance("SHA-256")`:**

```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
```

**This part reads file in chunks of 4096 bytes: creates buffer, reads from InputStream, updates digest with `digest.update(buffer, 0, read)`:**

```java
byte[] buffer = new byte[4096];
int read;
while ((read = is.read(buffer)) != -1) {
    digest.update(buffer, 0, read);
}
```

**This part calls `digest.digest()` to get hash bytes after reading all:**

```java
byte[] hashBytes = digest.digest();
```

**This part converts hash bytes to hex string using `bytesToHex()`:**

```java
return bytesToHex(hashBytes);
```

**This part closes InputStream in finally block:**

```java
finally {
    if (is != null) {
        try { is.close(); } catch (Exception ignored) {}
    }
}
```

### bytesToHex(byte[] bytes)

This function converts byte array to hex string.

**This part iterates through each byte in the array:**

```java
for (byte b : bytes) {
```

**This part converts byte to int with `b & 0xFF` to handle signed byte:**

```java
int v = b & 0xFF;
```

**This part adds '0' in front if value < 0x10 to ensure 2 hex characters:**

```java
if (v < 0x10) {
    sb.append('0');
}
```

**This part uses `Integer.toHexString(v)` to convert to hex and appends to StringBuilder:**

```java
sb.append(Integer.toHexString(v));
```

**This part returns hex string (64 characters for SHA-256):**

```java
return sb.toString();
```

---

## Log Analysis

Here is the log output showing the complete workflow:

**First log - App launched normally:**
```
I MOBISEC : handleIntent, action = null
I MOBISEC : Launched normally, nothing to do.
```

This shows when the app is opened from launcher, the action is `null` (not `HASHFILE`), so the function returns early and does nothing.

**Second log - MobiSec system prepares and sends Intent:**
```
E MOBISEC : app:onCreate
E MOBISEC : Building explicit intent targeting component: ComponentInfo{com.example.myapplication/com.example.myapplication.MainActivity}
E MOBISEC : writing 'aGdqQ6jq9d5TACEytxx3' to '/storage/emulated/0/3d32rMIv.dat'
E MOBISEC : About to send intent: Intent { act=com.mobisec.intent.action.HASHFILE dat=file:///storage/emulated/0/3d32rMIv.dat cmp=com.example.myapplication/.MainActivity }
E MOBISEC : I am expecting to receive the following hash: 42270535dde6eb03e0330bcd7d2d33faddcf5439f5396796365b34863793c35b
```

This shows MobiSec system:
- Creates a file at `/storage/emulated/0/3d32rMIv.dat` with content `'aGdqQ6jq9d5TACEytxx3'`
- Calculates expected SHA-256 hash: `42270535dde6eb03e0330bcd7d2d33faddcf5439f5396796365b34863793c35b`
- Sends Intent with action `HASHFILE` and file URI

**Third log - Our app processes the Intent:**
```
I MOBISEC : handleIntent, action = com.mobisec.intent.action.HASHFILE
I MOBISEC : Got HASHFILE request for Uri: file:///storage/emulated/0/3d32rMIv.dat
I MOBISEC : Computed SHA-256: 42270535dde6eb03e0330bcd7d2d33faddcf5439f5396796365b34863793c35b
```

This shows our app:
- Receives Intent with action `HASHFILE` (matches our check)
- Gets file URI from Intent
- Calculates SHA-256 hash successfully
- The computed hash matches the expected hash

**Fourth log - MobiSec receives result and prints flag:**
```
E MOBISEC : app:onActivityResult
E MOBISEC : app:onActivityResult. got intent: Intent { (has extras) }
E MOBISEC : I have received the following hash: 42270535dde6eb03e0330bcd7d2d33faddcf5439f5396796365b34863793c35b
E MOBISEC : I was expecting the following hash: 42270535dde6eb03e0330bcd7d2d33faddcf5439f5396796365b34863793c35b
E MOBISEC : They match! good job, here is the flag: MOBISEC{Was_it_known_that_these_one_way_functions_give_you_back_flags?}
```

This shows MobiSec system:
- Receives result in `onActivityResult()`
- Extracts hash from Intent extra (key `"hash"`)
- Compares received hash with expected hash
- They match, so prints the flag

The log confirms that the solution works correctly: the app receives the Intent, calculates the correct SHA-256 hash, returns it via result Intent, and MobiSec verifies it matches.
