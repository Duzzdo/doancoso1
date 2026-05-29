@echo off
title Restaurant Management System
echo Starting Restaurant Management System...
echo.

cd /d "%~dp0"

javaw -cp "lib\*;bin" com.restaurant.Main

if errorlevel 1 (
    echo.
    echo Error: Failed to start the application.
    echo Please make sure Java is installed and JAVA_HOME is set correctly.
    pause
)
