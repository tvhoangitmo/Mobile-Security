# Solution

## Description of the problem

The `filehasher` challenge requires writing an Android application that acts as a SHA-256 hash calculation service for files. The MobiSec system creates a file on external storage, calculates its expected SHA-256 hash, and sends an Intent to our app with action `com.mobisec.intent.action.HASHFILE` and file URI. Our app must read the file, calculate SHA-256, and return the hash via result Intent with extra key `"hash"`. If the hash matches, the system prints the flag.

## Solution

I've solved the challenge by developing an app that receives the Intent from MobiSec system, reads the file via ContentResolver, calculates SHA-256 using MessageDigest, and returns the result via `setResult()`.

**AndroidManifest.xml** - Added permission and intent-filter:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<activity android:exported="true">
    <intent-filter>
        <action android:name="com.mobisec.intent.action.HASHFILE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

**MainActivity.java** - `handleIntent()` function processes the Intent:

```java
private void handleIntent(Intent intent) {
    if (intent == null || !ACTION_HASHFILE.equals(intent.getAction())) {
        return;
    }

    Uri fileUri = intent.getData();
    if (fileUri == null) {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return;
    }

    String hashHex = computeSha256OfUri(fileUri);
    if (hashHex == null) {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return;
    }

    Intent resultIntent = new Intent();
    resultIntent.putExtra("hash", hashHex);
    setResult(Activity.RESULT_OK, resultIntent);
    finish();
}
```

**MainActivity.java** - `computeSha256OfUri()` function reads file and calculates SHA-256:

```java
private String computeSha256OfUri(Uri uri) {
    InputStream is = null;
    try {
        is = getContentResolver().openInputStream(uri);
        if (is == null) return null;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }

        return bytesToHex(digest.digest());
    } catch (Exception e) {
        return null;
    } finally {
        if (is != null) {
            try { is.close(); } catch (Exception ignored) {}
        }
    }
}
```

**MainActivity.java** - `bytesToHex()` function converts byte array to hex string:

```java
private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
        int v = b & 0xFF;
        if (v < 0x10) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(v));
    }
    return sb.toString();
}
```

**Flag**: `MOBISEC{Was_it_known_that_these_one_way_functions_give_you_back_flags?}`

