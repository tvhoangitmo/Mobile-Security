package com.mobisec.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBISEC";

    // trùng với UnbindableService
    private static final int MSG_REGISTER_CLIENT = 1;
    private static final int MSG_UNREGISTER_CLIENT = 2;
    private static final int MSG_SET_VALUE       = 3;
    private static final int MSG_GET_FLAG        = 4;

    private Messenger remoteMessenger;  // tới service
    private Messenger replyMessenger;   // từ service

    // Handler nhận message từ service
    private class IncomingHandler extends Handler {
        IncomingHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_VALUE:
                    Log.i(TAG, "Received SET_VALUE = " + msg.arg1);
                    break;

                case MSG_GET_FLAG:
                    if (msg.obj instanceof Bundle) {
                        Bundle b = (Bundle) msg.obj;
                        String flag = b.getString("flag");
                        Log.i(TAG, "FLAG: " + flag);
                    } else {
                        Log.i(TAG, "MSG_GET_FLAG without bundle");
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected: " + name);

            remoteMessenger = new Messenger(service);
            replyMessenger  = new Messenger(new IncomingHandler());

            try {
                // đăng ký client
                Message reg = Message.obtain(null, MSG_REGISTER_CLIENT);
                reg.replyTo = replyMessenger;
                remoteMessenger.send(reg);

                // yêu cầu flag
                Message getFlag = Message.obtain(null, MSG_GET_FLAG);
                getFlag.replyTo = replyMessenger;
                remoteMessenger.send(getFlag);

            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to service", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            remoteMessenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // giữ nguyên layout mặc định

        bindToUnbindableService();
    }

    private void bindToUnbindableService() {
        String targetPackage = "com.mobisec.unbindable";
        String targetService = "com.mobisec.unbindable.UnbindableService";

        Intent i = new Intent();
        i.setClassName(targetPackage, targetService);

        boolean ok = bindService(i, conn, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "bindService returned: " + ok);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remoteMessenger != null) {
            try {
                Message unreg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                unreg.replyTo = replyMessenger;
                remoteMessenger.send(unreg);
            } catch (RemoteException ignored) {}
            unbindService(conn);
        }
    }
}
