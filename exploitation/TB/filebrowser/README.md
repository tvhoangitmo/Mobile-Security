# FileBrowser exploit (content-query inside target uid)

This build fixes two issues seen in previous attempts:

1) The provider is **not exported**, so querying it from the exploit app fails with SecurityException.
2) Copying the SQLite DB and opening it locally can fail (schema / WAL / race), so avoid it.

## Final approach
- Use `PluginActivity` to obtain a `PendingIntent` to the target's `QueryActivity`.
- Abuse `QueryActivity` command injection (`sh -c "ls " + arg`) to execute commands **as the target UID**.
- Stage 1: copy `shared_prefs/keys.xml` to `/sdcard/keys.xml` and parse the random key.
- Stage 2: run `/system/bin/content query` on `content://com.mobisec.provider.Log/log` (still as target UID),
  dump rows to `/sdcard/fb_dump.txt`, extract the `arg` field for the `genflag` entry into `/sdcard/fb_arg.txt`.
- Decrypt `fb_arg.txt` locally using AES key = MD5(key).

Build `app-debug.apk` and submit.
