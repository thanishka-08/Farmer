@echo off
cd /d "%~dp0"
echo ========================================================
echo  Compiling Agricultural Supply Chain Management System
echo ========================================================
if not exist bin mkdir bin
javac --release 17 -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
if errorlevel 1 (
    echo [FALLBACK] Compiling with standard javac...
    javac -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
)
if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilation successful! Class files generated in bin\ (Java 17/21 compatible)
) else (
    echo.
    echo Compilation failed! Check error messages above.
)
pause
