package com.mobisec.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.mobisec.serialintent.FlagContainer;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBISEC";
    private static final int REQ_SERIAL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start SerialActivity của app target
        Intent i = new Intent();
        i.setClassName(
                "com.mobisec.serialintent",
                "com.mobisec.serialintent.SerialActivity"
        );
        startActivityForResult(i, REQ_SERIAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQ_SERIAL) return;
        if (resultCode != Activity.RESULT_OK || data == null) {
            Log.e(TAG, "No result from SerialActivity");
            return;
        }

        Object o = data.getSerializableExtra("flag");
        if (!(o instanceof FlagContainer)) {
            Log.e(TAG, "flag extra is not FlagContainer: " + o);
            return;
        }

        FlagContainer fc = (FlagContainer) o;

        try {
            // gọi private getFlag() bằng reflection
            Method m = fc.getClass().getDeclaredMethod("getFlag");
            m.setAccessible(true);
            String flag = (String) m.invoke(fc);

            Log.i(TAG, "FLAG: " + flag);
        } catch (Exception e) {
            Log.e(TAG, "Error calling getFlag via reflection", e);
        }
    }
}
