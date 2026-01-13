package com.mobisec.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBISEC";
    // URI của Content Provider
    private static final Uri JOKES_URI =
            Uri.parse("content://com.mobisec.provider.Joke/jokes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // layout mặc định, không quan trọng
        setContentView(R.layout.activity_main);

        fetchFlag();
    }

    private void fetchFlag() {
        StringBuilder flag = new StringBuilder();

        Cursor c = null;
        try {
            // selection: chỉ lấy joke của author = 'reyammer'
            String selection = "author = ?";
            String[] selectionArgs = new String[]{"reyammer"};

            c = getContentResolver().query(
                    JOKES_URI,
                    null,           // tất cả cột
                    selection,
                    selectionArgs,
                    null            // sort order
            );

            if (c == null) {
                Log.e(TAG, "Query returned null cursor");
                return;
            }

            int authorIdx = c.getColumnIndex("author");
            int jokeIdx   = c.getColumnIndex("joke");

            while (c.moveToNext()) {
                String author = (authorIdx >= 0) ? c.getString(authorIdx) : "";
                String joke   = (jokeIdx   >= 0) ? c.getString(jokeIdx)   : "";

                Log.i(TAG, "row: author=" + author + " joke=" + joke);

                // an toàn: nếu vì lý do gì selection không hoạt động,
                // vẫn kiểm tra lại author ở đây
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
}
