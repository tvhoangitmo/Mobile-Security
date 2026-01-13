# Solution

## Description of the problem

The `serialintent` challenge requires writing an Android application that calls an Activity from the MobiSec system (`com.mobisec.serialintent.SerialActivity`). The Activity returns a `FlagContainer` object via result Intent. The flag is stored in a private method `getFlag()` of `FlagContainer`. Our app must extract the FlagContainer, use Java reflection to call the private method, and retrieve the flag.

## Solution

I've solved the challenge by calling the external Activity using `startActivityForResult()`, receiving the `FlagContainer` object from result Intent, using Java reflection to access the private `getFlag()` method, and invoking it to retrieve the flag.

**MainActivity.java** - Call SerialActivity:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    Intent i = new Intent();
    i.setClassName("com.mobisec.serialintent", "com.mobisec.serialintent.SerialActivity");
    startActivityForResult(i, REQ_SERIAL);
}
```

**MainActivity.java** - `onActivityResult()` uses reflection to call private method:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode != REQ_SERIAL || resultCode != Activity.RESULT_OK || data == null) {
        return;
    }
    
    Object o = data.getSerializableExtra("flag");
    if (!(o instanceof FlagContainer)) {
        return;
    }
    
    FlagContainer fc = (FlagContainer) o;
    
    try {
        // Call private getFlag() using reflection
        Method m = fc.getClass().getDeclaredMethod("getFlag");
        m.setAccessible(true);
        String flag = (String) m.invoke(fc);
        Log.i(TAG, "FLAG: " + flag);
    } catch (Exception e) {
        Log.e(TAG, "Error calling getFlag via reflection", e);
    }
}
```

**Key reflection steps**:
- `getClass().getDeclaredMethod("getFlag")`: Gets the private method
- `setAccessible(true)`: Allows access to private method
- `invoke(fc)`: Calls the method on the FlagContainer instance

**Flag**: `MOBISEC{HOW_DID_YOU_DO_IT_THAT_WAS_SERIALLY_PRIVATE_STUFF1!!1!eleven!}`
