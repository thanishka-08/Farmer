@echo off
cd /d "%~dp0"
echo ========================================================
echo  Agricultural Supply Chain System - Terminal Mode
echo ========================================================
if not exist bin mkdir bin
javac --release 17 -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
if errorlevel 1 (
    javac -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
)
if %ERRORLEVEL% equ 0 (
    echo.
    java -cp bin Main --terminal
) else (
    echo.
    echo Compilation failed! Check error messages above.
)
echo.
pause
