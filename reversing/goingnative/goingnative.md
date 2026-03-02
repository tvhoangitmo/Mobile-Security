# Solution - goingnative challenge

## Description of the problem

The `goingnative` challenge validates a flag where part of the check is implemented in native code (JNI). The goal is to reverse engineer both the Java-side format checks and the native library function to reconstruct the correct `MOBISEC{...}` flag.

The app uses a native library `native-lib` that contains the core validation function. This is a common Android reverse engineering scenario where sensitive logic is moved to native code to make analysis more difficult.

## Solution

I've solved the challenge by analyzing the decompiled Java code from jadx, understanding the flag format requirements, extracting and analyzing the native library, and reconstructing the flag based on the validation logic.

### 1. Locate the validation code

In `MainActivity`, when the check button is clicked, the flag is validated:

```java
boolean r5 = com.mobisec.gonative.FlagChecker.checkFlag(r5)
```

The validation logic is in `com.mobisec.gonative.FlagChecker.checkFlag()`, which calls a native method.

### 2. Understand the native library setup

The native library is loaded:

```java
static {
    System.loadLibrary("native-lib");
}
```

The native function is declared:

```java
       private static native boolean helloFromTheOtherSide(String str, int i);
```

The native library `libnative-lib.so` contains the actual validation logic. The method takes a String (first part) and an int (6-digit number), and returns a boolean indicating if the flag is valid.

### 3. Analyze FlagChecker.checkFlag() - Format validation

The validation starts with format checks:

```java
String[] strArrSplit = str.split("-");
if (strArrSplit.length != 2 || !strArrSplit[0].startsWith("MOBISEC{") || !strArrSplit[1].endsWith("}")) {
    return false;
}
```

The flag is split by `-` into 2 parts. The first part must start with `MOBISEC{` and the second part must end with `}`. So the format is: `MOBISEC{...}-...}`

### 4. Analyze FlagChecker.checkFlag() - Extract parts

The code extracts the two parts:

```java
String strReplace = strArrSplit[0].replace("MOBISEC{", "");
String strReplace2 = strArrSplit[1].replace("}", "");
```

`strReplace` is the inner part of the first segment (after removing "MOBISEC{"), and `strReplace2` is the inner part of the second segment (after removing "}").

### 5. Analyze FlagChecker.checkFlag() - Second part validation

The second part is validated:

```java
if (strReplace2.matches("^[0-9]*$") && strReplace2.length() == 6) {
    return helloFromTheOtherSide(strReplace, Integer.parseInt(strReplace2));
}
```

`strReplace2` must be exactly 6 digits (numeric only). If valid, it calls the native method `helloFromTheOtherSide()` with the first parameter as `strReplace` (the inner part of first segment) and the second parameter as `Integer.parseInt(strReplace2)` (the 6-digit number parsed as integer).

So the Java-side format is: `MOBISEC{<LEFT>-<6DIGITS>}`

### 6. Extract and analyze the native library

To understand the complete validation, the native library must be extracted and analyzed:

**Extract the library from APK**:
```bash
     unzip -q goingnative.apk -d out
     # for emulator (x86_64):
     out/lib/x86_64/libnative-lib.so
```

The exported JNI symbol is:
```
     Java_com_mobisec_gonative_FlagChecker_helloFromTheOtherSide
```

### 7. Reverse the native function - PIN check

Disassembling `Java_com_mobisec_gonative_FlagChecker_helloFromTheOtherSide(String left, int pin)` shows:

The integer parameter `pin` must equal `0x7a69`:
     - `0x7a69` (hex) = `31337` (decimal)

So the native function requires the PIN to be 31337.

### 8. Reverse the native function - LEFT string check

The native function validates the `left` string with these checks:

- `strlen(left) == 12` - The string must be exactly 12 characters long
- `left[0] == 'n'` - First character is 'n'
- `left[1..5] == "ative"` - Characters 1-5 form "ative"
- Together with the first char: `"native"`
- `left[6] == '_'` - Character 6 is underscore
- `left[7] == 'i'` - Character 7 is 'i'
- `left[8] == 's'` - Character 8 is 's'
- `left[9] == '_'` - Character 9 is underscore
- `left[10..11] == "so"` - Characters 10-11 form "so"

This reconstructs: `LEFT = "native_is_so"`

### 9. Handle the 6-digit requirement

There's a conflict between Java and native requirements:
- Java requires the numeric part to be exactly 6 digits: `strReplace2.length() == 6`
- Native requires the parsed integer to be 31337: `pin == 31337`

The solution is to use a 6-digit string that parses to 31337:
```
031337
```

This string has 6 digits and `Integer.parseInt("031337")` equals 31337.

### 10. Assemble the final flag

**LEFT part**: `native_is_so`
**6-digit part**: `031337`

**Final flag**: `MOBISEC{native_is_so-031337}`

**Flag**: `MOBISEC{native_is_so-031337}`
