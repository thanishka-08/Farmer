@echo off
cd /d "%~dp0"
title Agricultural Supply Chain Management System
echo ====================================================================
echo   Starting Agricultural Supply Chain Management System (Java Swing)
echo ====================================================================
echo.

if not exist bin mkdir bin
if not exist bin\Main.class (
    echo [INFO] Compiling Java classes for Java 17/21 compatibility...
    javac --release 17 -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
    if errorlevel 1 (
        javac -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
    )
)

echo [1] Launching Graphical Desktop Application (Swing GUI)...
echo [2] Starting Localhost Web Server on http://localhost:8080/
echo [3] Opening browser dashboard...
echo.

java -cp bin Main

if errorlevel 1 (
    echo.
    echo [NOTICE] If a class version mismatch occurred, recompiling freshly...
    javac --release 17 -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
    echo Retrying application launch...
    java -cp bin Main
)

echo.
pause
