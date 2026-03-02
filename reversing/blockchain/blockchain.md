# Solution - blockchain challenge

## Description of the problem

The `blockchain` challenge asks for a **KEY** and a **FLAG**. The app validates these inputs using a complex encryption scheme involving MD5 hashing and AES encryption. The goal is to reverse engineer the validation logic and find a valid (KEY, FLAG) pair that satisfies all checks.

The validation logic is located in `com.mobisec.blockchain.FlagChecker.checkFlag()`, which performs iterative AES encryption with MD5-derived keys. The effective key space is reduced to only 24 bits (3 bytes), making brute force feasible.

## Solution

I've solved the challenge by analyzing the decompiled code from jadx, understanding the key derivation and encryption process, and then brute-forcing the 3-byte key space to reverse decrypt the flag.

### 1. Locate the validation code

In `MainActivity`, when the check button is clicked, both KEY and FLAG are validated:

```java
boolean result = FlagChecker.checkFlag(key, flag);
```

The validation logic is in `com.mobisec.blockchain.FlagChecker.checkFlag(keyStr, flagStr)`.

### 2. Analyze FlagChecker.checkFlag() - Key derivation

The validation starts by deriving a 3-byte key from the user's KEY input:

```java
byte[] fullKey = keyStr.getBytes();
byte[] digest = hash(fullKey);
byte[] key = {digest[0], digest[digest.length / 2], digest[digest.length - 1]};
```

The `hash()` function computes MD5:

```java
public static byte[] hash(byte[] in) throws Exception {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(in);
    return md.digest();
}
```

The key consists of only 3 bytes: the first byte (`digest[0]`), the middle byte (`digest[8]`), and the last byte (`digest[15]`) of the MD5 hash of the full key. This reduces the effective key space from infinite (any length KEY) to only 256³ = 16,777,216 possibilities.

### 3. Analyze FlagChecker.checkFlag() - Initialization

The code initializes the encryption state:

```java
byte[] currKey = hash(key);
byte[] currPt = flagStr.getBytes();
```

`currKey` is the MD5 hash of the 3-byte key (16 bytes, used as AES key). `currPt` is the plaintext flag bytes.

### 4. Analyze FlagChecker.checkFlag() - Encryption loop

The flag is encrypted 10 times:

```java
for (int i = 0; i < 10; i++) {
    byte[] newPt = encrypt(currPt, currKey);
    currPt = newPt;
    currKey = hash(currKey);
}
```

After each encryption round, the key is updated with its own MD5 hash. So the 10 encryption rounds use keys: `MD5(key3)`, `MD5(MD5(key3))`, `MD5(MD5(MD5(key3)))`, and so on.

### 5. Analyze FlagChecker.checkFlag() - Encryption function

The `encrypt()` function performs AES-ECB encryption with PKCS5 padding:

```java
public static byte[] encrypt(byte[] in, byte[] key) throws Exception {
    Key aesKey = new SecretKeySpec(key, "AES");
    Cipher encryptCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    encryptCipher.init(1, aesKey);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
    cipherOutputStream.write(in);
    cipherOutputStream.flush();
    cipherOutputStream.close();
    byte[] out = outputStream.toByteArray();
    return out;
}
```

The encryption uses AES-ECB mode with PKCS5 padding. When decrypting, we need to handle the padding correctly.

### 6. Analyze FlagChecker.checkFlag() - Final comparison

After 10 encryption rounds, the result is compared:

```java
return toHex(currPt).equals("0eef68c5ef95b67428c178f045e6fc8389b36a67bbbd800148f7c285f938a24e696ee2925e12ecf7c11f35a345a2a142639fe87ab2dd7530b29db87ca71ffda2af558131d7da615b6966fb0360d5823b79c26608772580bf14558e6b7500183ed7dfd41dbb5686ea92111667fd1eff9cec8dc29f0cfe01e092607da9f7c2602f5463a361ce5c83922cb6c3f5b872dcc088eb85df80503c92232bf03feed304d669ddd5ed1992a26674ecf2513ab25c20f95a5db49fdf6167fda3465a74e0418b2ea99eb2673d4c7e1ff7c4921c4e2d7b");
```

The `toHex()` function converts bytes to hexadecimal:

```java
public static String toHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
        String hex = Integer.toHexString(b & 255);
        if (hex.length() == 1) {
            hexString.append('0');
        }
        hexString.append(hex);
    }
    return hexString.toString();
}
```

The final ciphertext after 10 rounds must match this hardcoded hex string (512 hex characters = 256 bytes).

### 7. Brute force approach

Since the effective key space is only 24 bits (3 bytes), brute force is feasible. Instead of trying to guess the plaintext, we can reverse the encryption:

1. Treat the 3-byte value (extracted from MD5 of `keyStr`) as the brute-force target
2. For each 3-byte candidate `k`:
   - Compute `k0 = MD5(k)` (this is the first AES key)
   - Derive the 10 round keys: `k1 = MD5(k0)`, `k2 = MD5(k1)`, …, `k9`
   - Start from the hardcoded ciphertext and decrypt 10 times using keys in reverse order (`k9 … k0`)
3. When the plaintext becomes readable ASCII and matches the expected `MOBISEC{...}` format, the correct flag is found

### 8. Python brute-force script

**Script**:
```python
import hashlib
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad

TARGET_HEX = "0eef68c5ef95b67428c178f045e6fc8389b36a67bbbd800148f7c285f938a24e696ee2925e12ecf7c11f35a345a2a142639fe87ab2dd7530b29db87ca71ffda2af558131d7da615b6966fb0360d5823b79c26608772580bf14558e6b7500183ed7dfd41dbb5686ea92111667fd1eff9cec8dc29f0cfe01e092607da9f7c2602f5463a361ce5c83922cb6c3f5b872dcc088eb85df80503c92232bf03feed304d669ddd5ed1992a26674ecf2513ab25c20f95a5db49fdf6167fda3465a74e0418b2ea99eb2673d4c7e1ff7c4921c4e2d7b"

def md5(data):
    return hashlib.md5(data).digest()

def aes_ecb_decrypt(ciphertext, key):
    """Decrypt AES-ECB and remove PKCS5/PKCS7 padding."""
    cipher = AES.new(key, AES.MODE_ECB)
    decrypted = cipher.decrypt(ciphertext)
    try:
        return unpad(decrypted, AES.block_size)
    except ValueError:
        return decrypted

def is_printable_flag(data):
    """Check if decrypted data is a valid flag."""
    try:
        text = data.decode('utf-8')
        if all(32 <= ord(c) < 127 for c in text):
            if len(text) < 100 and ('MOBISEC' in text or 'flag' in text.lower() or '{' in text):
                return True
            if len(text) < 50:
                return True
    except UnicodeDecodeError:
        pass
    return False

def main():
    target_bytes = bytes.fromhex(TARGET_HEX)
    
    for key_int in range(0x1000000):
        key_3bytes = key_int.to_bytes(3, 'big')
        
        # Generate key chain (10 keys)
        key_chain = []
        curr_key = key_3bytes
        for _ in range(10):
            curr_key = md5(curr_key)
            key_chain.append(curr_key)
        
        # Reverse decrypt
        curr_ct = target_bytes
        try:
            for i in range(9, -1, -1):
                aes_key = key_chain[i]
                curr_ct = aes_ecb_decrypt(curr_ct, aes_key)
        except Exception:
            continue
        
        # Check if valid flag
        if is_printable_flag(curr_ct):
            print(f"KEY 3-byte: {key_3bytes.hex()}")
            print(f"FLAG: {curr_ct.decode('utf-8')}")
            break

if __name__ == "__main__":
    main()
```

**Explanation**:
- Iterates through all possible 3-byte keys (0 to 0xFFFFFF)
- For each key, generates 10 AES keys by repeatedly hashing with MD5
- Decrypts the target ciphertext 10 times in reverse order (using keys k9 to k0)
- Checks if the result is a valid printable flag starting with "MOBISEC{"

### 9. Result

The brute force finds:
- **KEY 3-byte**: `8e09ae`
- **FLAG**: `MOBISEC{blockchain_failed_to_deliver_once_again}`

The 3-byte key `8e09ae` can be derived from any KEY string whose MD5 hash has `digest[0]=0x8e`, `digest[8]=0x09`, and `digest[15]=0xae`. For example, one such KEY is `q>6fHk:d`$!(-OK ` (MD5: `8ea551aa3d18c2a809e22231e9cc4bae`).

**Flag**: `MOBISEC{blockchain_failed_to_deliver_once_again}`
