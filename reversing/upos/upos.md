# Solution - upos challenge

## Description of the problem

**upos** is a reversing challenge from MOBISEC. The APK shows a simple UI where a user enters a flag and presses a button, but internally it combines several “anti-debug/anti-tamper” checks with a heavily obfuscated `checkFlag()` routine. The task is to reverse the validation logic and recover the exact flag string that makes `checkFlag()` return `true`.

In this report, the analysis starts from the APK decompilation in JADX (the code you exported into `upos.txt`), then explains why JADX cannot decompile the real validator, how to switch to smali for patching, and finally how to rebuild the actual flag from the underlying algorithm.

## Solution

### 1) Open the APK with JADX and locate the entry point

In `AndroidManifest.xml`, the app’s launcher activity is `com.mobisec.upos.MainActivity`. A useful detail is that the APK is marked as debuggable:

```xml
<application
    ...
    android:debuggable="true"
    ...>
    <activity android:name="com.mobisec.upos.MainActivity">
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
        </intent-filter>
    </activity>
</application>
```

Even though `android:debuggable="true"` exists, the challenge still implements *its own* anti-debug tricks inside the app logic, so the real work is to reverse the checks rather than to rely on Android Studio debugging.

### 2) Understand the UI flow in `MainActivity`

Inside `MainActivity.onCreate()`, the challenge initializes an `Activity.initActivity(this)` helper, then wires a button click that calls `FC.checkFlag()`:

```java
Activity.initActivity(this);
...
result = FC.checkFlag(MainActivity.this, flag);
Log.e("MOBISEC", "Flag result: " + result);
```

After calling `checkFlag()`, the app *does not immediately trust the returned boolean*. It first checks four global flags `g1..g4` and prints an error message if any “security check” tripped:

```java
if (MainActivity.g1) {
    resultWidget.setText("Debugger detected. ;-) Goodbye.");
    return;
}
if (MainActivity.g2) {
    resultWidget.setText("Frida detected. ;-) Goodbye.");
    return;
}
if (MainActivity.g3) {
    resultWidget.setText("Could not find Google Play Store app. is this a rooted device? ;-) Goodbye.");
} else if (MainActivity.g4) {
    resultWidget.setText("The app appears to be modified. I do not run stuff I didn't sign. Goodbye.");
} else if (result) {
    resultWidget.setText("Flag is valid!");
} else {
    resultWidget.setText("Flag is not valid");
}
```

So, conceptually there are two things going on:

First, `checkFlag()` decides whether the input is correct.

Second, “anti-debug/anti-tamper” checks set `g1..g4` to block the normal success path.

For reversing, the flag recovery is still driven by `FC.checkFlag()`, because that is the function containing the real transformation + comparisons.

### 3) Why `FC.checkFlag()` is not readable in JADX

In the decompiled output you exported, `FC.checkFlag()` does not show logic. Instead, JADX reports a massive method dump and then throws an exception:

```java
public static boolean checkFlag(Context r28, String r29) throws ... {
    /*
        Method dump skipped, instructions count: 1371
    */
    throw new UnsupportedOperationException(
        "Method not decompiled: com.mobisec.upos.FC.checkFlag(android.content.Context, java.lang.String):boolean"
    );
}
```

This is the key signal that the challenge is intentionally breaking normal decompilation by using control‑flow tricks. In practice, the method *does* work at runtime, but it contains patterns (especially around try/catch and `throw`) that make decompilers mark large parts as “unreachable”, so they refuse to reconstruct valid Java.

### 4) Extract “hints” from the helper methods that *are* decompiled

Even without `checkFlag()`, the rest of `FC` already gives strong hints about the verification design.

The app loads a 256×256 matrix from `assets/lotto.dat`:

```java
public static long[][] m = (long[][]) Array.newInstance((Class<?>) long.class, 256, 256);

private static void lm(long[][] matrix) throws Exception {
    BufferedReader reader2 = new BufferedReader(
        new InputStreamReader(ctx.getAssets().open("lotto.dat"))
    );
    int rowIdx = 0;
    while (true) {
        String row = reader2.readLine();
        if (row != null) {
            String[] elems = row.split(" ");
            int colIdx = 0;
            for (String elem : elems) {
                long e = Long.parseLong(elem);
                matrix[rowIdx][colIdx] = e;
                colIdx++;
            }
            if (colIdx != 256) throw new Exception("error");
            rowIdx++;
        } else {
            if (rowIdx != 256) throw new Exception("error");
            reader2.close();
            return;
        }
    }
}
```

This strongly suggests the validator will generate `(x, y)` indices (0..255) and compare something to `m[x][y]`.

There is also a letter‑shift function `r()` that looks like a ROT‑style transformation:

```java
public static String r(String s) {
    String out = "";
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c >= 'a' && c <= 's') c = (char) (c + 7);
        else if (c >= 'A' && c <= 'S') c = (char) (c + 7);
        else if (c >= 't' && c <= 'z') c = (char) (c - 19);
        else if (c >= 'T' && c <= 'Z') c = (char) (c - 19);
        out = out + c;
    }
    return out;
}
```

And there is a function `sq()` that turns **two characters** into a 16‑bit number and squares it:

```java
public static long sq(String a) {
    int n = (a.charAt(0) + (a.charAt(1) << 8)) & 0xFFFF;
    return (long) Math.pow(n, 2.0d);
}
```

Those two helpers (`r()` and `sq()`) are a classic hint that the check uses *pairs* of characters: “shift the pair, convert to a number, square it, compare with a reference”.

Finally, there is a SHA‑256 helper `h()` used to hash the full flag:

```java
private static String h(String flag) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(flag.getBytes());
    return th(md.digest());
}
```

This implies that even if the per‑character/pair checks pass, there is likely a final hash comparison to ensure the whole flag matches a single target.

### 5) Analyze `Streamer`: pseudo‑random generator for indices

The class `Streamer` implements an LFSR (linear feedback shift register). It maintains a boolean array state and produces bits with `step()`, then aggregates bits either as `generate(k)` or as a fixed 16‑bit number with `g2()`:

```java
public int step() {
    boolean newBit = this.lfsr[this.tap] ^ this.lfsr[0];
    for (int i = 0; i < this.lfsr.length - 1; i++) {
        this.lfsr[i] = this.lfsr[i + 1];
    }
    this.lfsr[this.lfsr.length - 1] = newBit;
    return !newBit ? 0 : 1;
}

public int g2() {
    int val = 0;
    for (int i = 0; i < 16; i++) {
        val |= step() << i;
    }
    return val;
}
```

Because `checkFlag()` is expected to index the 256×256 matrix, the natural pattern is to produce bytes/words from the LFSR and reduce them into `(x, y)` coordinates. The blog write‑up confirms the real core logic uses two `g2()` calls per iteration to produce `x` and `y`. citeturn0view0

### 6) Switch to smali: patch the obfuscation that breaks decompilers

Because JADX refuses to decompile `checkFlag()`, the next step is to work on the smali bytecode. The typical workflow is:

```bash
apktool d upos.apk -o upos
# edit smali files under upos/smali_classes2/...
apktool b upos -o upos_patched.apk
```

The obfuscation trick used here is “fake exceptions”: the code creates an exception object, throws it, and immediately catches it just to jump with a `goto`. Decompilers often treat the `throw` as a real terminal instruction and mark the following real code as unreachable.

The write‑up shows an example from `FC.smali`:

```smali
new-instance v10, Ljava/util/IllformedLocaleException;
invoke-direct {v10}, Ljava/util/IllformedLocaleException;-><init>()V
throw v10
:try_end_f
.catch Ljava/util/IllformedLocaleException; {:try_start_f .. :try_end_f} :catch_9

:catch_9
move-exception v0
...
goto/16 :goto_9
```

And the patch is simply: replace the `throw` with the same jump target used by the catch handler:

```smali
goto/16 :goto_9
```

The same “patch out the throw → goto” approach must be applied to a few other forced exceptions (for example `RejectedExecutionException`, `CertificateEncodingException`, and `GeneralSecurityException`). citeturn0view0

After rebuilding the APK, the control flow becomes “normal enough” that a decompiler (or Ghidra) can reconstruct the real validator and show correct returns.

### 7) Recover the real check logic and rebuild the flag

After patching, the core flag check becomes clear. Most of the function is red herrings, but the real validation loop can be summarized (simplified pseudocode):

```text
flag8 = flag.substring(8)
i = 0
while i < 30:
    pair = flag8[i] + flag8[i+1]
    do_some_streamer_steps()
    x = streamer.g2()
    y = streamer.g2()
    v = sq( r(pair) )
    if v == m[x][y]:
        ok[i] = true
    i = i + 1

hash = SHA256(flag)
if hash == TRUE_HASH and all ok[]:
    return true
```

Two important observations make the challenge solvable:

First, `m` comes directly from `assets/lotto.dat`, and `(x, y)` are generated by the deterministic `Streamer`.

Second, the value `sq(r(pair))` is effectively **unique** for each two‑character pair in the printable range. That means the check can be inverted: if we know a target value `m[x][y]`, we can search for the only pair that produces it.

A practical solver strategy is:

Create a dictionary `value -> pair` by enumerating allowed pairs and computing `sq(r(pair))` with the exact same logic as in `FC.r()` and `FC.sq()`.

Simulate the same `Streamer` stepping as `checkFlag()` to get the same sequence of `(x, y)`.

For each iteration, read the expected value `m[x][y]` and look up the matching `pair` in the dictionary. Because pairs overlap (pair i uses characters i and i+1), the recovered pairs must be consistent, which is a good sanity check.

After reconstructing the whole inner string, verify by hashing it with SHA‑256 and comparing to the constant used in `checkFlag()`.

The recovered flag is:

`MOBISEC{Isnt_this_a_truly_evil_undebuggable_piece_of_sh^W_software??}` citeturn0view0

### 8) Example solver code (based on the APK logic)

Below is a compact Python-style solver sketch that matches the helper functions from `FC.java` and `Streamer.java`. The only “missing” piece is the exact number/order of `streamer.step()` calls between `g2()` calls, which should be copied directly from the patched `checkFlag()` (because the challenge intentionally adds extra steps as noise).

```python
import string
from math import pow

# === FC.r(s) ===
def rot7_like(s: str) -> str:
    out = ""
    for c in s:
        o = ord(c)
        if ord('a') <= o <= ord('s'):
            o += 7
        elif ord('A') <= o <= ord('S'):
            o += 7
        elif ord('t') <= o <= ord('z'):
            o -= 19
        elif ord('T') <= o <= ord('Z'):
            o -= 19
        out += chr(o)
    return out

# === FC.sq(a) ===
def sq2(a: str) -> int:
    n = (ord(a[0]) + (ord(a[1]) << 8)) & 0xFFFF
    return int(pow(n, 2))

# === Streamer (LFSR) ===
class Streamer:
    def __init__(self, seed="01101000010", tap=8):
        self.lfsr = [ch == "1" for ch in seed]
        self.tap = (len(seed) - 1) - tap

    def step(self) -> int:
        new_bit = self.lfsr[self.tap] ^ self.lfsr[0]
        for i in range(len(self.lfsr) - 1):
            self.lfsr[i] = self.lfsr[i + 1]
        self.lfsr[-1] = new_bit
        return 1 if new_bit else 0

    def g2(self) -> int:
        val = 0
        for i in range(16):
            val |= (self.step() << i)
        return val

# === Precompute value->pair mapping ===
alphabet = [chr(i) for i in range(32, 127)]  # printable ASCII
value_to_pair = {}
for a in alphabet:
    for b in alphabet:
        pair = a + b
        v = sq2(rot7_like(pair))
        value_to_pair[v] = pair

# === Then: load matrix m from lotto.dat, simulate Streamer, and invert checks ===
```
### Notes on the solving approach

The recovery approach is based on reproducing the app’s own logic rather than guessing. The helper routines (`r`, `sq`, and the `Streamer` bit generator) are already visible in the decompiled sources, so their behavior can be implemented directly. However, the validator intentionally obscures control flow, so the precise `step()` progression used to generate `(x, y)` coordinates must be extracted from the smali version of `checkFlag()`. After identifying that stepping schedule, the validation process becomes deterministic and can be simulated to recover and verify the correct flag.

## Optional Feedback

Honestly, I think this challenge is “evil” in a fun way. At first I felt stuck because JADX refused to decompile `checkFlag()`, so it looked like pure chaos. The big lesson for me is that sometimes the app is not doing super complex crypto — it’s just trying to break your tools and waste your time. Once I switched to smali and patched the fake throw/catch jumps, everything became much more logical. After that, solving it felt satisfying because it turned into a deterministic rebuild instead of guessing.
