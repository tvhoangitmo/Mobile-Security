package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FlagReceiver extends BroadcastReceiver {

    // Cho MainActivity dùng lại khi tạo IntentFilter
    public static final String ACTION_FLAG =
            "com.mobisec.intent.action.FLAG_ANNOUNCEMENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (ACTION_FLAG.equals(action)) {
            String flag = intent.getStringExtra("flag");
            Log.i("MOBISEC", "Received flag: " + flag);
        }
    }
}
