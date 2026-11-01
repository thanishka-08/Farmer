@echo off
echo ========================================================
echo  Compiling Agricultural Supply Chain Management System
echo ========================================================
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\test\*.java src\Main.java
if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilation successful! Class files generated in bin\
) else (
    echo.
    echo Compilation failed! Check error messages above.
)
pause
