@echo off
echo ========================================
echo   RESET DATABASE - RESTAURANT MANAGEMENT
echo ========================================
echo.

echo Deleting all database files...

REM Delete database in resources folder
if exist "resources\database\restaurant.db" (
    del /F /Q "resources\database\restaurant.db"
    echo Deleted: resources\database\restaurant.db
)

REM Delete database in bin folder
if exist "bin\resources\database\restaurant.db" (
    del /F /Q "bin\resources\database\restaurant.db"
    echo Deleted: bin\resources\database\restaurant.db
)

REM Delete database in root folder
if exist "restaurant.db" (
    del /F /Q "restaurant.db"
    echo Deleted: restaurant.db
)

REM Delete database in bin root
if exist "bin\restaurant.db" (
    del /F /Q "bin\restaurant.db"
    echo Deleted: bin\restaurant.db
)

echo.
echo ========================================
echo   DATABASE RESET COMPLETE!
echo ========================================
echo.
echo Next time you run the application, a new database
echo will be created with the CORRECT admin password hash.
echo.
echo Admin credentials:
echo   Username: admin
echo   Password: admin123
echo.
echo IMPORTANT: The password hash has been fixed!
echo Old hash was WRONG, new hash is CORRECT.
echo.
pause
