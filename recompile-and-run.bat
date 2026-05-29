@echo off
echo ========================================
echo RECOMPILE PROJECT - FIX EMAIL ISSUE
echo ========================================

echo.
echo [1/3] Cleaning old class files...
if exist "bin\com\restaurant\utils\EmailUtil.class" (
    del /F /Q "bin\com\restaurant\utils\EmailUtil.class"
    echo - Deleted EmailUtil.class
)

echo.
echo [2/3] Recompiling EmailUtil.java...
javac -encoding UTF-8 -cp "lib/*;src" -d bin src/com/restaurant/utils/EmailUtil.java
if %ERRORLEVEL% EQU 0 (
    echo - Compiled successfully!
) else (
    echo - Compilation failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Testing email sending...
echo.
java -cp "lib/*;bin" com.restaurant.Main

pause
