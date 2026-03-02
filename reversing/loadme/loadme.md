# Solution - loadme challenge

## Description of the problem

The `loadme` challenge is about **dynamic code loading**. The APK does not contain the real flag check in its own classes; instead it decrypts some hidden strings (URL, class name, method name), downloads a secondary APK/DEX, XOR-decrypts it, loads it with `DexClassLoader`, and calls a method inside the downloaded code to validate the input.

The goal is to follow the loader chain until the final stage where the flag is revealed. This demonstrates how Android apps can dynamically load code at runtime to obfuscate validation logic.

## Solution

I've solved the challenge by analyzing the decompiled code from jadx, understanding the loader chain, decrypting hidden strings, extracting and analyzing the dynamically loaded DEX file, and following the chain to the final validation stage.

### 1. Locate the validation entry point

In `MainActivity`, when the check button is clicked, the flag is validated:

```java
DoStuff doStuff = new DoStuff();
result = doStuff.start(MainActivity.this, flag);
```

The validation logic is in `com.mobisec.dexclassloader.DoStuff.start()`, which handles the dynamic code loading process.

### 2. Understand DoStuff.start() - Main flow

The `start()` method orchestrates the entire loading process:

```java
public boolean start(Context ctx, String flag) throws IOException {
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    setContext(ctx);
    setUserInput(flag);
    String path = dc(gu());
    da(path);
    return lc(path);
}
```

The method:
1. Sets up context and flag
2. Downloads a file from URL returned by `gu()` using `dc()`
3. Decrypts the downloaded file using `da()`
4. Loads and executes the decrypted DEX using `lc()`

### 3. Analyze DoStuff.gu() - Get download URL

The `gu()` method returns a URL by decrypting a Base64-encoded string:

```java
private String gu() throws BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
    String url = ds("MAi9CEe4K9a+JzgsNqdYYh13dk7SQQ/yo5BN5HF39nYtgnOBmO4EV9Y2sQDthTG9");
    return url;
}
```

The `ds()` method performs AES-CBC decryption (explained below). The decrypted URL is: `https://challs.reyammer.io/loadme/stage1.apk`

### 4. Analyze DoStuff.gf() - Get filename

The `gf()` method returns the filename for the downloaded file:

```java
private String gf() {
    return ds("QLrdlqkhOkxIe5FEfpCLWw==");
}
```

The decrypted filename is: `test.dex`

### 5. Analyze DoStuff.gc() - Get class name

The `gc()` method returns the class name to load:

```java
private String gc() {
    return ds("ca9O9YbCZ/+vIYUvxPQUHA4SgyL/m3cwlvVZ4ArkBFQ=");
}
```

The decrypted class name is: `com.mobisec.stage1.LoadImage`

### 6. Analyze DoStuff.gm() - Get method name

The `gm()` method returns the method name to call:

```java
private String gm() {
    return ds("6RSjLOfRkvb/qXa34Y7cOQ==");
}
```

The decrypted method name is: `load`

### 7. Understand DoStuff.ds() - AES-CBC decryption

The `ds()` method decrypts Base64-encoded strings using AES-CBC:

```java
private String ds(String enc) throws BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
    try {
        String[] parts = this.context.getPackageName().split(Pattern.quote("."));
        String key = parts[1] + parts[0] + "key!!!";
        IvParameterSpec iv = new IvParameterSpec(initVector);
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(2, skeySpec, iv);
        byte[] original = cipher.doFinal(Base64.decode(enc.getBytes(), 0));
        return new String(original);
    } catch (Exception ex) {
        ex.printStackTrace();
        return null;
    }
}
```

**AES parameters**:
- Mode: `AES/CBC/PKCS5PADDING`
- IV: `initVector = {-34, -83, -66, -17, -34, -83, -66, -17, -34, -83, -66, -17, -34, -83, -66, -17}` which is `0xDEADBEEF` repeated 4 times
- Key: Derived from package name:
  ```java
  parts = packageName.split("\\.");
  key = parts[1] + parts[0] + "key!!!";
  ```
  For `com.mobisec.dexclassloader`:
  - `parts[0] = "com"`, `parts[1] = "mobisec"`
  - `key = "mobiseccomkey!!!"`

### 8. Analyze DoStuff.dc() - Download file

The `dc()` method downloads a file from the URL:

```java
private String dc(String url) throws IOException {
    try {
        URL downloaded_url = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) downloaded_url.openConnection();
        urlConnection.connect();
        File file = new File(this.context.getCodeCacheDir(), gf());
        FileOutputStream fileOutput = new FileOutputStream(file);
        InputStream inputStream = urlConnection.getInputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int bufferLength = inputStream.read(buffer);
            if (bufferLength <= 0) {
                fileOutput.close();
                return file.getAbsolutePath();
            }
            fileOutput.write(buffer, 0, bufferLength);
        }
    } catch (Exception e) {
        return null;
    }
}
```

The file is downloaded from the URL (from `gu()`) and saved to `code_cache/` directory with the filename from `gf()` (`test.dex`).

### 9. Analyze DoStuff.da() - XOR decryption

The `da()` method XOR-decrypts the downloaded file in-place:

```java
private void da(String path) throws IOException {
    byte[] xorKey = this.context.getPackageName().getBytes();
    File file = new File(path);
    int size = (int) file.length();
    byte[] bytes = new byte[size];
    byte[] decbytes = new byte[size];
    try {
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();
        for (int i = 0; i < size; i++) {
            decbytes[i] = (byte) (bytes[i] ^ xorKey[i % xorKey.length]);
        }
        File outFile = new File(path);
        FileOutputStream out = new FileOutputStream(outFile, false);
        out.write(decbytes);
        out.flush();
        out.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

The XOR key is the package name bytes (`com.mobisec.dexclassloader`). Each byte of the file is XORed with the corresponding byte of the key (repeating the key if needed).

### 10. Analyze DoStuff.lc() - Load and execute DEX

The `lc()` method loads the decrypted DEX file and calls the validation method:

```java
private boolean lc(String path) throws NoSuchMethodException, SecurityException {
    File tmpDir = new File(this.context.getFilesDir().getAbsolutePath());
    File file = new File(path);
    DexClassLoader classloader = new DexClassLoader(file.getAbsolutePath(), tmpDir.getAbsolutePath(), null, ClassLoader.getSystemClassLoader());
    file.delete();
    File[] ftemp = tmpDir.listFiles();
    for (File f : ftemp) {
        f.delete();
    }
    try {
        Class<?> classToLoad = classloader.loadClass(gc());
        Method method = classToLoad.getDeclaredMethod(gm(), Context.class, String.class);
        boolean res = ((Boolean) method.invoke(classToLoad, this.context, this.flag)).booleanValue();
        return res;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

The method:
1. Creates a `DexClassLoader` with the decrypted DEX file
2. Loads the class from `gc()` (`com.mobisec.stage1.LoadImage`)
3. Gets the method from `gm()` (`load`)
4. Invokes the method with `Context` and flag string
5. Returns the boolean result

### 11. Extract and analyze stage1

To continue the analysis, we need to:
1. Download `stage1.apk` from `https://challs.reyammer.io/loadme/stage1.apk`
2. XOR-decrypt it using the package name as key
3. Decompile the decrypted DEX/APK using jadx

After decompiling `stage1.apk`, it shows that `com.mobisec.stage1.LoadImage.load()` loads an additional payload from the original APK's resources (a file named `logo.png`), XOR-decrypts it, loads that final stage, and calls `checkFlag(...)`.

### 12. Follow the chain to final stage

The final stage is loaded from `logo.png` in the original APK's resources. After XOR decryption and loading, the final validation method `checkFlag()` is called, which contains the flag in plain text.

**Flag**: `MOBISEC{dynamic_code_loading_can_make_everything_tricky_eh?}`
