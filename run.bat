@echo off
cd /d "%~dp0"
echo ========================================================
echo  Starting Agricultural Supply Chain Management System
echo ========================================================

rem Ensure classes are freshly compiled
if not exist bin\Main.class (
    echo Class files not found. Compiling project...
    if not exist bin mkdir bin
    javac -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
)

echo.
echo [1] Opening Agricultural Supply Chain Graphical Desktop Window (Java Swing)...
echo [2] Launching Localhost Web Server at: http://localhost:8080/
echo [3] Opening your default web browser to view the interactive dashboard...
echo.

java -cp bin Main
pause
