# Solution

## Description of the problem

The `jokeprovider` challenge requires writing an Android application that queries a Content Provider from the MobiSec system. The Content Provider at URI `content://com.mobisec.provider.Joke/jokes` contains jokes with authors. Our app must query for jokes where author equals `"reyammer"`, extract the joke text, and combine them to form the flag.

## Solution

I've solved the challenge by using ContentResolver to query the Content Provider with a selection filter for author `"reyammer"`, iterating through the Cursor results, extracting joke text, and combining them into the final flag.

**MainActivity.java** - `fetchFlag()` queries Content Provider:

```java
private void fetchFlag() {
    StringBuilder flag = new StringBuilder();
    Uri JOKES_URI = Uri.parse("content://com.mobisec.provider.Joke/jokes");
    
    String selection = "author = ?";
    String[] selectionArgs = new String[]{"reyammer"};
    
    Cursor c = null;
    try {
        c = getContentResolver().query(JOKES_URI, null, selection, selectionArgs, null);
        if (c == null) return;
        
        int authorIdx = c.getColumnIndex("author");
        int jokeIdx = c.getColumnIndex("joke");
        
        while (c.moveToNext()) {
            String author = (authorIdx >= 0) ? c.getString(authorIdx) : "";
            String joke = (jokeIdx >= 0) ? c.getString(jokeIdx) : "";
            
            if ("reyammer".equals(author)) {
                flag.append(joke);
            }
        }
        
        Log.i(TAG, "FLAG: " + flag.toString());
    } catch (Exception e) {
        Log.e(TAG, "Error querying jokes", e);
    } finally {
        if (c != null) c.close();
    }
}
```

**Flag**: `MOBISEC{lol_roftl_ahahah_:D_REYAMMER_TELLS_THE_BEST_JOKES!}`
