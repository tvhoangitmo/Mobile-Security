# Solution

## Description of the problem

The `babyrev` challenge is a basic reversing task. The APK provides a text field where a candidate flag is entered and validated. The goal is to reverse engineer the validation logic and reconstruct the exact `MOBISEC{...}` flag that satisfies all checks.

The validation logic is located in `com.mobisec.babyrev.FlagChecker.checkFlag()`, which performs multiple checks on the flag string including:
- Format and length constraints
- Character position requirements
- ROT13 cipher check
- Hidden suffix from resources
- Alternating case regex pattern

## Solution

This solution was obtained by analyzing the decompiled code from jadx, understanding each validation constraint, and systematically reconstructing the flag.

### 1. Locate the validation code

In `MainActivity`, the input is passed directly to:

```java
boolean result = FlagChecker.checkFlag(MainActivity.this, flag);
```

Therefore, the real validation logic is located in `com.mobisec.babyrev.FlagChecker`.

### 2. Analyze `FlagChecker.checkFlag()`

**MainActivity.java** (entry point):

```java
checkFlag.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String flag = flagWidget.getText().toString();
        boolean result = FlagChecker.checkFlag(MainActivity.this, flag);
        // ... display result
    }
});
```

**FlagChecker.java** (validation method):

```java
public static boolean checkFlag(Context ctx, String flag) {
    if (!flag.startsWith("MOBISEC{") ||
        new StringBuilder(flag).reverse().toString().charAt(0) != '}' ||
        flag.length() != 35 ||
        !flag.toLowerCase().substring(8).startsWith("this_is_") ||
        !new StringBuilder(flag).reverse().toString().toLowerCase().substring(1)
            .startsWith(ctx.getString(R.string.last_part)) ||
        flag.charAt(17) != '_' ||
        flag.charAt((int) (getY() * Math.pow(getX(), getY()))) !=
            flag.charAt(((int) Math.pow(Math.pow(2.0d, 2.0d), 2.0d)) + 1) ||
        !bam(flag.toUpperCase().substring(getY() * getX() * getY(),
            (int) (Math.pow(getZ(), getX()) - 1.0d))).equals("ERNYYL") ||
        flag.toLowerCase().charAt(16) != 'a' ||
        flag.charAt(16) != flag.charAt(26) ||
        flag.toUpperCase().charAt(25) != flag.toUpperCase().charAt(26) + 1) {
        return false;
    }
    String r = getR();
    return flag.substring(8, flag.length() - 1).matches(r);
}
```

### 3. Simplify helper methods (constant folding)

The helper methods return constants:

- `getX() = 2`
- `getY() = 3`
- `getZ() = 5`

```java
private static int getX() { return 2; }
private static int getY() { return 3; }
private static int getZ() { return 5; }
```

Now we can simplify the numeric expressions:

- `getY() * getX() * getY()` = `3 * 2 * 3` = `18`
- `(int) (Math.pow(getZ(), getX()) - 1)` = `5^2 - 1` = `24`
- `((int) Math.pow(Math.pow(2.0d, 2.0d), 2.0d)) + 1` = `16 + 1` = `17`
- **Important**: `getY() * Math.pow(getX(), getY())` = `3 * 2^3` = `3 * 8` = **`24`**

So the “mystery comparison” becomes:

```java
flag.charAt(24) == flag.charAt(17)
```

Because the code also enforces `flag.charAt(17) == '_'`, this implies:

- `flag.charAt(24) == '_'`

(There is **no** requirement that index 18 is `'_'`; index 18 is part of the ROT13 substring check below.)

### 4. Recognize `bam()` as ROT13

The `bam()` function shifts letters by 13 positions, which is **ROT13**.

```java
private static String bam(String s) {
    String out = "";
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c >= 'a' && c <= 'm') c = (char) (c + 13);
        else if (c >= 'A' && c <= 'M') c = (char) (c + 13);
        else if (c >= 'n' && c <= 'z') c = (char) (c - 13);
        else if (c >= 'N' && c <= 'Z') c = (char) (c - 13);
        out = out + c;
    }
    return out;
}
```

The check is:

```java
bam(flag.toUpperCase().substring(18, 24)).equals("ERNYYL")
```

`substring(18, 24)` covers indices **18..23** (6 characters). Because ROT13("REALLY") = "ERNYYL", the substring must be "REALLY" (case-insensitive due to `toUpperCase()`).

To also satisfy the final alternating-case regex, choose:

- indices 18..23 = `ReAlLy`

### 5. Extract hidden suffix from resources

Another condition checks a reversed suffix:

```java
new StringBuilder(flag).reverse().toString().toLowerCase().substring(1)
    .startsWith(ctx.getString(R.string.last_part))
```

From `res/values/strings.xml`:

- `R.string.last_part = "ver_cisab_"`

Because the code compares against the **reversed flag** (and skips the final `}`), the real flag (without `}`) must end with:

- reverse("ver_cisab_") = `_basic_rev`

With alternating case to satisfy the regex, this becomes:

- `_bAsIc_rEv`

### 6. Apply the final regex constraint (alternating case)

After passing the checks, the code enforces:

```java
String r = getR();
return flag.substring(8, flag.length() - 1).matches(r);
```

Regex builder:

```java
public static String getR() {
    String r = "";
    boolean upper = true;
    for (int i = 0; i < 26; i++) {
        r = upper ? r + "[A-Z_]" : r + "[a-z_]";
        upper = !upper;
    }
    return r;
}
```

So the inner part (between `{` and `}`) is exactly 26 characters and must alternate:

- Inner index 0: `[A-Z_]`
- Inner index 1: `[a-z_]`
- Inner index 2: `[A-Z_]`
- ...

### 7. Additional character constraints

From the remaining conditions:

- `flag.toLowerCase().charAt(16) == 'a'`  
  So index 16 is either `'a'` or `'A'`. Due to the alternating regex (inner index 8 is even), choose `'A'`.

- `flag.charAt(16) == flag.charAt(26)`  
  So index 26 must match index 16 → choose `'A'`.

- `flag.toUpperCase().charAt(25) == flag.toUpperCase().charAt(26) + 1`  
  If index 26 is `'A'`, then `toUpperCase(flag[25])` must be `'B'`. Due to alternating regex (inner index 17 is odd → lowercase), choose index 25 as `'b'`.

### 8. Assemble the final flag

**Constraints summary (by absolute index):**

- Format: `MOBISEC{...}` and total length = 35
- Inner part length = 26 and must match the alternating regex
- Indices 8..15 must spell `this_is_` (case-insensitive) → pick `ThIs_iS_`
- Index 16 = `A`
- Index 17 = `_`
- Indices 18..23 = `ReAlLy` (ROT13 check)
- Index 24 = `_` (because `flag[24] == flag[17]`)
- Index 25 = `b` (because `toUpperCase(flag[25])` must be `B`)
- Index 26 = `A`
- Suffix ends with `_bAsIc_rEv` (from `last_part` reversed, with alternating case)

**Final flag**:

```
MOBISEC{ThIs_iS_A_ReAlLy_bAsIc_rEv}
```

**Flag**: `MOBISEC{ThIs_iS_A_ReAlLy_bAsIc_rEv}`
