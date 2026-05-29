@echo off
echo ========================================
echo   COMPILING RESTAURANT MANAGEMENT
echo ========================================
echo.

REM Create bin directory if not exists
if not exist "bin" mkdir bin

REM Compile all Java files
echo Compiling Java files...
javac -encoding UTF-8 -cp "lib/*;src" -d bin src/com/restaurant/*.java src/com/restaurant/model/*.java src/com/restaurant/dao/*.java src/com/restaurant/database/*.java src/com/restaurant/view/*.java src/com/restaurant/utils/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   COMPILATION SUCCESSFUL!
    echo ========================================
    echo.
    echo Run the application with: run.bat
) else (
    echo.
    echo ========================================
    echo   COMPILATION FAILED!
    echo ========================================
    echo.
    echo Please check:
    echo 1. JDK is installed and in PATH
    echo 2. lib/sqlite-jdbc-3.44.1.0.jar exists
    echo 3. lib/jbcrypt-0.4.jar exists
)

pause
