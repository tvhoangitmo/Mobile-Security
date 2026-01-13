package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBISEC";

    private static final int REQ_1 = 1;
    private static final int REQ_2 = 2;
    private static final int REQ_3 = 3;
    private static final int REQ_4 = 4;

    private final String[] parts = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Client MainActivity started");

        // Bắt đầu hỏi phần 1
        askPartOne();
    }

    // ================== GỬI INTENT TỚI APP ĐỀ ==================

    private void askPartOne() {
        Log.i(TAG, "Starting PartOne...");
        Intent i = new Intent();
        i.setClassName("com.mobisec.justask",
                "com.mobisec.justask.PartOne");
        try {
            startActivityForResult(i, REQ_1);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start PartOne: " + e);
        }
    }

    private void askPartTwo() {
        Log.i(TAG, "Starting PartTwo...");
        Intent i = new Intent();
        i.setClassName("com.mobisec.justask",
                "com.mobisec.justask.PartTwo");
        try {
            startActivityForResult(i, REQ_2);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start PartTwo: " + e);
        }
    }

    private void askPartThree() {
        Log.i(TAG, "Starting PartThree...");
        Intent i = new Intent();
        i.setClassName("com.mobisec.justask",
                "com.mobisec.justask.PartThree");
        try {
            startActivityForResult(i, REQ_3);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start PartThree: " + e);
        }
    }

    private void askPartFour() {
        Log.i(TAG, "Starting PartFour...");
        Intent i = new Intent();
        i.setClassName("com.mobisec.justask",
                "com.mobisec.justask.PartFour");
        try {
            startActivityForResult(i, REQ_4);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start PartFour: " + e);
        }
    }

    // ================== NHẬN KẾT QUẢ ==================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult: req=" + requestCode + " result=" + resultCode);

        if (data == null) {
            Log.e(TAG, "No data for request " + requestCode);
            return;
        }

        // Log full intent để xem trên report MOBISEC
        dumpIntent("req" + requestCode, data);

        switch (requestCode) {
            case REQ_1: {
                // Part 1: extra "flag"
                String p1 = data.getStringExtra("flag");
                Log.i(TAG, "Part1 = " + p1);
                parts[0] = p1;
                askPartTwo();
                break;
            }
            case REQ_2: {
                // Part 2: extra "flag"
                String p2 = data.getStringExtra("flag");
                Log.i(TAG, "Part2 = " + p2);
                parts[1] = p2;
                askPartThree();
                break;
            }
            case REQ_3: {
                // Part 3: ẩn trong "hiddenFlag"
                String hidden = data.getStringExtra("hiddenFlag");
                String visible = data.getStringExtra("flag");
                Log.i(TAG, "req3 hiddenFlag = " + hidden);
                Log.i(TAG, "req3 flag      = " + visible);
                parts[2] = hidden;
                Log.i(TAG, "Part3 = " + hidden);
                askPartFour();
                break;
            }
            case REQ_4: {
                // Part 4: nằm sâu trong Bundle "follow"
                Bundle follow = data.getBundleExtra("follow");
                String p4 = findStringDeep(follow);
                Log.i(TAG, "Part4 = " + p4);
                parts[3] = p4;

                // Ghép flag cuối
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

    // ================== HÀM PHỤ ==================

    // Log toàn bộ nội dung Intent
    private void dumpIntent(String prefix, Intent data) {
        if (data.getAction() != null)
            Log.i(TAG, prefix + " action=" + data.getAction());
        if (data.getDataString() != null)
            Log.i(TAG, prefix + " data=" + data.getDataString());
        if (data.getType() != null)
            Log.i(TAG, prefix + " type=" + data.getType());

        if (data.getExtras() != null) {
            for (String k : data.getExtras().keySet()) {
                Object v = data.getExtras().get(k);
                Log.i(TAG, prefix + " extra[" + k + "] = " + v);
            }
        }
    }

    // Đệ quy đi sâu vào Bundle / nested Bundle để tìm String (phần 4)
    private String findStringDeep(Bundle b) {
        if (b == null) return null;

        for (String k : b.keySet()) {
            Object v = b.get(k);
            Log.i(TAG, "follow[" + k + "] = " + v);

            if (v instanceof String) {
                return (String) v;
            } else if (v instanceof Bundle) {
                String inner = findStringDeep((Bundle) v);
                if (inner != null) return inner;
            }
        }
        return null;
    }
}
