# Solution - gnirts challenge

## Description of the problem

The `gnirts` challenge provides an APK that validates a flag locally. The goal is to reverse engineer the validation logic and reconstruct the single flag string that satisfies all checks.

The validation logic is located in `com.mobisec.gnirts.FlagChecker.checkFlag()`, which performs multiple checks including format validation, string splitting, hash comparisons using reflection, and character position constraints.

## Solution

I've solved the challenge by analyzing the decompiled code from jadx, understanding each validation constraint, extracting constants from resources, and systematically reconstructing the flag.

### 1. Locate the validation code

In `MainActivity`, the input flag is validated:

```java
boolean result = FlagChecker.checkFlag(MainActivity.this, flag);
```

The validation logic is entirely in `com.mobisec.gnirts.FlagChecker.checkFlag()`.

### 2. Analyze basic format constraints

The validation starts with format checks:

```java
if (!flag.startsWith("MOBISEC{") || !flag.endsWith("}")) {
    return false;
}
```

The flag must start with `MOBISEC{` and end with `}`.

Then it extracts the "core" part:

```java
String core = flag.substring(8, 40);
if (core.length() != 32) {
    return false;
}
```

The core is the 32-character substring between `MOBISEC{` (8 chars) and `}` (position 40), so it's the inner part of the flag.

### 3. Recover the separator using foo()

The core is split using a separator obtained from `foo()`:

```java
String[] ps = core.split(foo());
```

The `foo()` function repeatedly Base64-decodes a string 10 times:

```java
public static String foo() {
    String s = "Vm0wd2QyVkZNVWRYV0docFVtMVNWVmx0ZEhkVlZscDBUVlpPVmsxWGVIbFdiVFZyVm0xS1IyTkliRmRXTTFKTVZsVmFWMVpWTVVWaGVqQTk=";
    for (int i = 0; i < 10; i++) {
        s = new String(Base64.decode(s, 0));
    }
    return s;
}
```

**Key observation**: After 10 Base64 decodings, the separator becomes a single character. This is used to split the core into 5 parts.

### 4. Derive separator from character constraint

The code checks 4 specific character positions:

```java
char[] syms = new char[4];
int[] idxs = {13, 21, 27, 32};
Set<Character> chars = new HashSet<>();
for (int i = 0; i < syms.length; i++) {
    syms[i] = flag.charAt(idxs[i]);
    chars.add(Character.valueOf(syms[i]));
}
int sum = 0;
for (char c : syms) {
    sum += c;
}
return sum == 180 && chars.size() == 1;
```

**Key findings**:
- Reads characters at positions 13, 21, 27, 32 (all within the core)
- All 4 characters must be identical (`chars.size() == 1`)
- Their ASCII sum must equal 180
- Therefore each character is `180 / 4 = 45`, which is `'-'`

This confirms the separator is `-`, so the core format is: `ps0-ps1-ps2-ps3-ps4` (5 parts separated by dashes).

### 5. Understand part type constraints

The code validates each part has a specific type:

```java
if (ps.length != 5 || !bim(ps[0]) || !bum(ps[2]) || !bam(ps[4])) {
    return false;
}
```

The helper methods check:
- `bim(ps[0])`: `ps[0]` must match `^[a-z]+$` (lowercase letters only)
- `bum(ps[2])`: `ps[2]` must match `^[A-Z]+$` (uppercase letters only)
- `bam(ps[4])`: `ps[4]` must match `^[0-9]+$` (digits only)

```java
private static boolean bim(String s) {
    return s.matches("^[a-z]+$");
}

private static boolean bum(String s) {
    return s.matches("^[A-Z]+$");
}

private static boolean bam(String s) {
    return s.matches("^[0-9]+$");
}
```

**Constraints**:
- `ps0`: lowercase letters only
- `ps1`: no specific constraint (but must match hash)
- `ps2`: uppercase letters only
- `ps3`: no specific constraint (but must match hash)
- `ps4`: digits only

### 6. Understand the hash check mechanism

The validation performs hash checks using reflection:

```java
me(ctx, dh(gs(ctx.getString(R.string.ct1), ctx.getString(R.string.k1)), ps[0]), ctx.getString(R.string.t1))
```

This complex expression can be broken down:

**Step 1**: `gs(ct, k)` - XOR decryption:

```java
private static String gs(String a, String b) {
    String s = "";
    for (int i = 0; i < a.length(); i++) {
        s = s + Character.toString((char) (a.charAt(i) ^ b.charAt(i % b.length())));
    }
    return s;
}
```

This XOR-decrypts string `a` using key `b` (repeating the key if needed). The result is the hash algorithm name (e.g., "md5", "sha256").

**Step 2**: `dh(algorithm, part)` - Hash computation:

```java
private static String dh(String hash, String s) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(hash);
    md.update(s.getBytes());
    byte[] digest = md.digest();
    return toHexString(digest);
}
```

This computes the hash digest of the part using the algorithm name from step 1.

**Step 3**: `me(ctx, digest, target)` - Reflection-based comparison:

```java
private static boolean me(Context ctx, String s1, String s2) throws NoSuchMethodException {
    Class c = s1.getClass();
    Method m = c.getMethod(r(ctx.getString(R.string.m1)), Object.class);
    boolean res = ((Boolean) m.invoke(s1, s2)).booleanValue();
    return res;
}
```

The `r()` function reverses a string:

```java
public static String r(String s) {
    return new StringBuffer(s).reverse().toString();
}
```

From resources: `m1 = "slauqe"` → reversed = `"equals"`

So `me()` effectively calls `s1.equals(s2)`, comparing the computed hash against the target digest.

**Summary**: Each check `me(ctx, dh(gs(ct, k), part), t)` reduces to:
```
hash(part) == expected_digest
```

### 7. Extract constants from resources

From `resources.arsc/res/values/strings.xml`, the target digests are:

```text
t1 = 6e9a4d130a9b316e9201238844dd5124
t2 = 7c51a5e6ea3214af970a86df89793b19
t3 = e5f20324ae520a11a86c7602e29ecbb8
t4 = 1885eca5a40bc32d5e1bca61fcd308a5
t5 = da5062d64347e5e020c5419cebd149a2
t6 = 1c4d1410a4071880411f02ff46370e46b464ab2f87e8a487a09e13040d64e396
```

The ciphertexts and keys are:
- `ct1="xwe"`, `k1="53P"` → XOR decrypts to hash algorithm name
- `ct2="asd"`, `k2=",7Q"` → ...
- And so on for ct3-k3, ct4-k4, ct5-k5, ct6-k6

### 8. Crack the 5 parts using hash cracking

With the constraints:
- `ps0`: lowercase letters only → brute force against `t1`
- `ps1`: no constraint → brute force against `t2`
- `ps2`: uppercase letters only → brute force against `t3`
- `ps3`: no constraint → brute force against `t4`
- `ps4`: digits only → brute force against `t5`

**Python script** (conceptual):
```python
For each part, determine hash algorithm from XOR decryption
Then brute force the part against the target digest
ps0: lowercase only, hash(ps0) == t1
ps1: any chars, hash(ps1) == t2
ps2: uppercase only, hash(ps2) == t3
ps3: any chars, hash(ps3) == t4
ps4: digits only, hash(ps4) == t5
```

**Results**:
- `ps0 = peppa` (lowercase, matches t1)
- `ps1 = 9876543` (matches t2)
- `ps2 = BAAAM` (uppercase, matches t3)
- `ps3 = A1z9` (matches t4)
- `ps4 = 3133337` (digits, matches t5)

### 9. Verify the full flag hash

The final check validates the entire flag:

```java
me(ctx, dh(gs(ctx.getString(R.string.ct6), ctx.getString(R.string.k6)), flag), ctx.getString(R.string.t6))
```

This computes the hash of the entire flag and compares it against `t6`. The flag `MOBISEC{peppa-9876543-BAAAM-A1z9-3133337}` should satisfy this check.

### 10. Assemble the final flag

**Core**: `peppa-9876543-BAAAM-A1z9-3133337`

**Final flag**: `MOBISEC{peppa-9876543-BAAAM-A1z9-3133337}`
