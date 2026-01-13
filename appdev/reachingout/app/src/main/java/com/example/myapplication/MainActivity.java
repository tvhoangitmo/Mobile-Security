package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOBISEC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(() -> {
            try {
                // 1) GET /flag để lấy đề bài + các hidden input
                URL url = new URL("http://10.0.2.2:31337/flag");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String html = sb.toString();
                Log.i(TAG, "Server page: " + html);

                // 2) Parse val1, oper, val2 từ HTML
                String val1 = extractValue(html, "val1");
                String oper = extractValue(html, "oper");
                String val2 = extractValue(html, "val2");

                int a = Integer.parseInt(val1);
                int b = Integer.parseInt(val2);
                int result;

                switch (oper) {
                    case "+": result = a + b; break;
                    case "-": result = a - b; break;
                    case "*": result = a * b; break;
                    case "/": result = a / b; break; // yên tâm là chia nguyên
                    default:
                        Log.e(TAG, "Unknown operator: " + oper);
                        return;
                }

                String answer = String.valueOf(result);
                Log.i(TAG, "Computed answer: " + a + " " + oper + " " + b + " = " + answer);

                // 3) POST tới /flag với form data
                URL postUrl = new URL("http://10.0.2.2:31337/flag");
                HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
                postConn.setRequestMethod("POST");
                postConn.setDoOutput(true);
                postConn.setConnectTimeout(5000);
                postConn.setReadTimeout(5000);
                postConn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                String body =
                        "answer=" + URLEncoder.encode(answer, "UTF-8") +
                                "&val1=" + URLEncoder.encode(val1, "UTF-8") +
                                "&oper=" + URLEncoder.encode(oper, "UTF-8") +
                                "&val2=" + URLEncoder.encode(val2, "UTF-8");

                OutputStream os = postConn.getOutputStream();
                os.write(body.getBytes());
                os.flush();
                os.close();

                BufferedReader r2 = new BufferedReader(
                        new InputStreamReader(postConn.getInputStream())
                );
                StringBuilder sb2 = new StringBuilder();
                while ((line = r2.readLine()) != null) {
                    sb2.append(line);
                }
                r2.close();

                String flagResp = sb2.toString();
                Log.i(TAG, "FLAG RESPONSE: " + flagResp);

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
            }
        }).start();
    }

    // hàm phụ để lấy value="..." cho id=...
    private String extractValue(String html, String id) {
        // Ví dụ: <input id="val1" name="val1" ... value="3">
        String marker = "id=\"" + id + "\"";
        int idx = html.indexOf(marker);
        if (idx < 0) return "";
        int vIdx = html.indexOf("value=\"", idx);
        if (vIdx < 0) return "";
        vIdx += "value=\"".length();
        int end = html.indexOf("\"", vIdx);
        if (end < 0) return "";
        return html.substring(vIdx, end);
    }
}
