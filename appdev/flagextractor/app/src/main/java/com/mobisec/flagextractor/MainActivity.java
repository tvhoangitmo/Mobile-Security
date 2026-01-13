package com.mobisec.flagextractor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mobisec.flagextractor.FlagContainer; // Import from same package

import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SERIAL = 1;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultText = findViewById(R.id.resultText);
        Log.e("MOBISEC", "MyApp started");

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "com.mobisec.serialintent",
                "com.mobisec.serialintent.SerialActivity"
        ));
        try {
            Log.e("MOBISEC", "Starting SerialActivity for result");
            startActivityForResult(intent, REQUEST_CODE_SERIAL);
        } catch (Exception e) {
            Log.e("MOBISEC", "Error starting activity: " + e.toString());
            resultText.setText("Error: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("MOBISEC", "onActivityResult: req=" + requestCode + " res=" + resultCode);
        if (requestCode == REQUEST_CODE_SERIAL) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    FlagContainer fc = (FlagContainer) data.getSerializableExtra("flag");
                    if (fc == null) {
                        Log.e("MOBISEC", "No flag extra in intent");
                        return;
                    }
                    Log.e("MOBISEC", "Got fc, parts length: " + fc.parts.length);

                    // Reconstruct flag
                    ArrayList<Integer> perm = fc.perm;
                    String[] parts = fc.parts;
                    int n = parts.length;
                    StringBuilder b64 = new StringBuilder();
                    for (int i = 0; i < n; i++) {
                        b64.append(parts[perm.get(i)]);
                    }
                    byte[] flagBytes = Base64.decode(b64.toString(), Base64.DEFAULT);
                    String flag = new String(flagBytes, Charset.defaultCharset());

                    Log.e("MOBISEC", "FLAG: " + flag);
                    resultText.setText("Flag: " + flag);

                } catch (Exception e) {
                    Log.e("MOBISEC", "Error extracting: " + e.toString());
                    resultText.setText("Error extracting: " + e.toString());
                }
            } else {
                Log.e("MOBISEC", "Result not OK or no data");
            }
        }
    }
}