@echo off
echo ========================================================
echo  Starting Agricultural Supply Chain Management System
echo ========================================================
if not exist bin\Main.class (
    echo Class files not found. Compiling first...
    if not exist bin mkdir bin
    javac -encoding UTF-8 -d bin src\model\*.java src\service\*.java src\storage\*.java src\ui\*.java src\Main.java
)
java -cp bin Main
pause
