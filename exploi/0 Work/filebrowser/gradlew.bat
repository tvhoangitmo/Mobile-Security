\
    @echo off
    setlocal
    set DIR=%~dp0
    if exist "%DIR%gradle\wrapper\gradle-wrapper.jar" (
      java -jar "%DIR%gradle\wrapper\gradle-wrapper.jar" %*
      exit /b %ERRORLEVEL%
    ) else (
      echo gradle-wrapper.jar is missing. Open the project in Android Studio and run any Gradle task; Studio will regenerate wrapper.
      echo Or run with system Gradle if installed: gradle %*
      exit /b 1
    )
