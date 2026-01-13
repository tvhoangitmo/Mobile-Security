package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBISEC";
    private static final String ACTION_HASHFILE =
            "com.mobisec.intent.action.HASHFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // UI không quan trọng

        // Khi hệ thống MOBISEC start activity với HASHFILE
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Phòng trường hợp activity đã tồn tại, Intent mới tới:
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "No intent received");
            return;
        }

        String action = intent.getAction();
        Log.i(TAG, "handleIntent, action = " + action);

        // Nếu app được mở bình thường (MAIN/LAUNCHER) thì không làm gì đặc biệt
        if (!ACTION_HASHFILE.equals(action)) {
            Log.i(TAG, "Launched normally, nothing to do.");
            return;
        }

        // ----- 1. Lấy Uri file cần hash -----
        Uri fileUri = intent.getData();
        if (fileUri == null) {
            Log.e(TAG, "HASHFILE intent without data Uri");
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        Log.i(TAG, "Got HASHFILE request for Uri: " + fileUri);

        // ----- 2. Tính SHA-256 -----
        String hashHex = computeSha256OfUri(fileUri);
        if (hashHex == null) {
            Log.e(TAG, "Failed to compute hash");
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        Log.i(TAG, "Computed SHA-256: " + hashHex);

        // ----- 3. Trả kết quả cho hệ thống -----
        Intent resultIntent = new Intent();
        // Tên key PHẢI đúng "hash" như đề bài
        resultIntent.putExtra("hash", hashHex);
        setResult(Activity.RESULT_OK, resultIntent);

        // Kết thúc activity: control quay về app MOBISEC
        finish();
    }

    /**
     * Đọc nội dung file từ Uri và trả về SHA-256 dạng hex.
     */
    private String computeSha256OfUri(Uri uri) {
        InputStream is = null;
        try {
            // Dùng ContentResolver để hỗ trợ cả file:// và content://
            is = getContentResolver().openInputStream(uri);
            if (is == null) {
                Log.e(TAG, "Cannot open InputStream for uri: " + uri);
                return null;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[4096];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }

            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);

        } catch (Exception e) {
            Log.e(TAG, "Error computing SHA-256", e);
            return null;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Chuyển mảng byte thành chuỗi hex chữ thường, không có khoảng trắng.
     * Mỗi byte -> 2 kí tự [0-9a-f], nên SHA-256 (32 byte) -> 64 kí tự.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v < 0x10) {
                sb.append('0'); // thêm 0 cho đủ 2 chữ số
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }
}
