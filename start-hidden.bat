@echo off
cd /d "%~dp0"
start /b javaw -cp "lib\*;bin" com.restaurant.Main
exit
