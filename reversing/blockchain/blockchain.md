# Solution

## Description of the problem

This challenge involves AES encryption and MD5 hashing. The goal is to find any (KEY, FLAG) pair that makes the `checkFlag` function return true. The challenge uses a custom encryption scheme where the flag is encrypted 10 times using AES-ECB with keys derived from MD5 hashes. The effective key space is only 3 bytes (derived from MD5 hash positions 0, 16, 31), making brute force feasible.

## Solution

I've solved the challenge by brute forcing all possible 3-byte keys (256^3 = 16,777,216 possibilities), generating the key chain for each key, reverse decrypting the known ciphertext, and checking if the decrypted result is a valid flag.

**Python Script** - Brute force and reverse decryption:

```python
import hashlib
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad

TARGET_HEX = "0eef68c5ef95b67428c178f045e6fc8389b36a67bbbd800148f7c285f938a24e696ee2925e12ecf7c11f35a345a2a142639fe87ab2dd7530b29db87ca71ffda2af558131d7da615b6966fb0360d5823b79c26608772580bf14558e6b7500183ed7dfd41dbb5686ea92111667fd1eff9cec8dc29f0cfe01e092607da9f7c2602f5463a361ce5c83922cb6c3f5b872dcc088eb85df80503c92232bf03feed304d669ddd5ed1992a26674ecf2513ab25c20f95a5db49fdf6167fda3465a74e0418b2ea99eb2673d4c7e1ff7c4921c4e2d7b"

def md5(data):
    return hashlib.md5(data).digest()

def aes_ecb_decrypt(ciphertext, key):
    cipher = AES.new(key, AES.MODE_ECB)
    decrypted = cipher.decrypt(ciphertext)
    try:
        return unpad(decrypted, AES.block_size)
    except ValueError:
        return decrypted

def main():
    target_bytes = bytes.fromhex(TARGET_HEX)
    
    for key_int in range(0x1000000):
        key_3bytes = key_int.to_bytes(3, 'big')
        
        # Generate key chain
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
            print(f"FLAG: {curr_ct.decode('utf-8')}")
            break
```

**Key functions**:
- `md5()`: Computes MD5 hash
- `aes_ecb_decrypt()`: Decrypts AES-ECB and removes PKCS5 padding
- `main()`: Iterates through all 3-byte keys, generates key chain, reverse decrypts, and checks for valid flag

**Flag**: `MOBISEC{blockchain_failed_to_deliver_once_again}`

## Optional Feedback

I really liked this challenge because it demonstrated that even with multiple rounds of encryption and key chaining, if the key space is small enough, brute force remains a viable approach. It also taught me how to reverse engineer encryption schemes and use Python for cryptographic operations.
