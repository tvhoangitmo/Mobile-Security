# Solution

## Description of the problem

The `justask` challenge requires writing an Android application that calls four Activities from the MobiSec system (`PartOne`, `PartTwo`, `PartThree`, `PartFour`) and extracts flag parts from their result Intents. Each Activity returns a different part of the flag in various formats: direct extra, hidden extra, or nested in a Bundle. The app must combine all four parts to form the complete flag.

## Solution

I've solved the challenge by using `startActivityForResult()` to call each Activity sequentially, extracting flag parts from result Intents using different methods (direct extra, hidden extra, recursive Bundle search), storing them in an array, and combining them into the final flag.

**MainActivity.java** - Call Activities and handle results:

```java
private void askPartOne() {
    Intent i = new Intent();
    i.setClassName("com.mobisec.justask", "com.mobisec.justask.PartOne");
    startActivityForResult(i, REQ_1);
}
```

**MainActivity.java** - `onActivityResult()` extracts flag parts:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (data == null) return;
    
    switch (requestCode) {
        case REQ_1: {
            String p1 = data.getStringExtra("flag");
            parts[0] = p1;
            askPartTwo();
            break;
        }
        case REQ_3: {
            String hidden = data.getStringExtra("hiddenFlag");
            parts[2] = hidden;
            askPartFour();
            break;
        }
        case REQ_4: {
            Bundle follow = data.getBundleExtra("follow");
            String p4 = findStringDeep(follow);
            parts[3] = p4;
            
            // Combine all parts
            StringBuilder sb = new StringBuilder();
            for (String s : parts) {
                if (s != null) sb.append(s);
            }
            String flag = sb.toString();
            Log.i(TAG, "FLAG: " + flag);
            break;
        }
    }
}
```

**MainActivity.java** - `findStringDeep()` recursively searches nested Bundles:

```java
private String findStringDeep(Bundle b) {
    if (b == null) return null;
    
    for (String k : b.keySet()) {
        Object v = b.get(k);
        if (v instanceof String) {
            return (String) v;
        } else if (v instanceof Bundle) {
            String inner = findStringDeep((Bundle) v);
            if (inner != null) return inner;
        }
    }
    return null;
}
```

**Flag**: `MOBISEC{Ive_asked_and_I_got_the_flag_how_nice!}`
