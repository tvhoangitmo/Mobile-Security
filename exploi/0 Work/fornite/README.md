# Fortnite exploit (Java 21 compatible project)

This project targets the authorized Mobisec homework emulator.
It is compatible with **Java 21** by using:
- Gradle 8.7 wrapper
- Android Gradle Plugin 8.5.2
- AndroidX AppCompat

## Build (from Android Studio or terminal)
1) Open the folder in Android Studio, let it sync.
2) Generate the payload dex:
   ./gradlew :payload:makeDex
   -> creates app/src/main/assets/fortnite.dex

3) Build the exploit APK:
   ./gradlew :app:assembleDebug
   -> app/build/outputs/apk/debug/app-debug.apk

## Run
- Install/upload app-debug.apk as your submission.
- On launch, the exploit app starts the target and overwrites:
  /sdcard/fortnite.dex and /sdcard/sign.dat
- Target loads and runs com.mobisec.fortnitepayload.Payload.run()
- Payload reads com.mobisec.fortnite.MainActivity.flag and starts FlagActivity with the flag.

## Notes
- Emulator is Android 9 (API 28) so scoped storage does not apply.
- WRITE_EXTERNAL_STORAGE is declared; the Mobisec runner usually auto-grants permissions on install.
