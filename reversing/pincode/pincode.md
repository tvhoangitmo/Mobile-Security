# Solution - pincode challenge

## Description of the problem

The `pincode` challenge asks for a correct 6-digit PIN. If the PIN is correct, the app contacts a remote server endpoint at `https://challs.reyammer.io/pincode/<pin>` and returns the real flag. The goal is to reverse engineer the APK, recover the PIN by analyzing the validation logic, and then retrieve the flag from the server.

The validation logic is located in `com.mobisec.pincode.PinChecker.checkPin()`, which performs MD5 hashing iterations on the PIN and compares the result against a hardcoded hash value.

## Solution

I've solved the challenge by analyzing the decompiled code from jadx, understanding the PIN validation algorithm, brute-forcing the 6-digit PIN space, and then querying the server endpoint to retrieve the flag.

### 1. Locate the PIN validation code

In `MainActivity`, when the check button is clicked, the PIN is validated:

```java
     boolean pinValid = PinChecker.checkPin(MainActivity.this, pin);
```

This calls the validation method in `PinChecker` class. If the PIN is valid, the app fetches the flag from the server:

```java
flag = MainActivity.this.getFlag(pin);
```

The `getFlag()` method constructs the URL and makes an HTTP GET request:

```java
String url = "https://challs.reyammer.io/pincode/" + pin;
String ans = getUrlContent(url);
```

So the flow is: validate PIN locally → if valid, fetch flag from remote server.

### 2. Analyze PinChecker.checkPin() - Length check

The validation starts with a length check:

```java
if (pin.length() != 6) {
    return false;
}
```

The PIN must be exactly **6 characters** long. This limits the search space to 1,000,000 possibilities (000000 to 999999), making brute force feasible.

### 3. Analyze PinChecker.checkPin() - Iterative MD5 hashing

The core validation performs iterative MD5 hashing:

```java
byte[] pinBytes = pin.getBytes();
     for (int i = 0; i < 25; i++) {
       for (int j = 0; j < 400; j++) {
        MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(pinBytes);
        byte[] digest = md.digest();
        pinBytes = (byte[]) digest.clone();
       }
     }
```

**Key observations**:
- The PIN bytes are hashed using **MD5 10,000 times** (25 iterations × 400 iterations = 10,000 total)
- Each MD5 digest becomes the input for the next MD5 operation (chained hashing)
- This creates a one-way transformation: PIN → MD5(PIN) → MD5(MD5(PIN)) → ... (10,000 times)

### 4. Analyze PinChecker.checkPin() - Hash comparison

After 10,000 MD5 iterations, the result is compared:

```java
String hexPinBytes = toHexString(pinBytes);
return hexPinBytes.equals("d04988522ddfed3133cc24fb6924eae9");
```

The `toHexString()` method converts the byte array to hexadecimal:

```java
String hex = Integer.toHexString(b & 255);
if (hex.length() == 1) {
    hexString.append('0');  // Pad with leading zero if needed
}
```

**Key finding**: The correct PIN is a 6-character string whose **MD5 iterated 10,000 times** equals `d04988522ddfed3133cc24fb6924eae9`.

### 5. Brute force the 6-digit PIN

Since the PIN is 6 digits (000000 to 999999), the search space is only 1,000,000 possibilities. This makes brute force feasible, especially with multi-threading.

**Python brute-force script**:
```python
     import hashlib

     TARGET = "d04988522ddfed3133cc24fb6924eae9"

     for i in range(1_000_000):
    pin = f"{i:06d}".encode()  # Format as 6-digit string
         b = pin
    for _ in range(25 * 400):  # 10,000 MD5 iterations
             b = hashlib.md5(b).digest()
         if b.hex() == TARGET:
             print("PIN:", pin.decode())
             break
```

**Explanation**:
- Iterates through all possible 6-digit PINs (000000 to 999999)
- For each PIN, performs 10,000 MD5 iterations (matching the app's logic)
- Compares the final hash against the target
- When a match is found, prints the PIN

**Result**: The brute force yields the correct PIN: `703958`

### 6. Retrieve the flag from server

Once the correct PIN is found, query the server endpoint:

**Request**:
```
GET https://challs.reyammer.io/pincode/703958
```

**Response**: The server returns the flag message:
```
MOBISEC{local_checks_can_be_very_bad_for_security}
```

**Flag**: `MOBISEC{local_checks_can_be_very_bad_for_security}`
