package com.example.myapplication;

import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FlagReceiver flagReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // UI mặc định, không quan trọng
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Tạo instance receiver và đăng ký
        flagReceiver = new FlagReceiver();
        IntentFilter filter =
                new IntentFilter(FlagReceiver.ACTION_FLAG);
        registerReceiver(flagReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký để tránh leak
        if (flagReceiver != null) {
            unregisterReceiver(flagReceiver);
            flagReceiver = null;
        }
    }
}
