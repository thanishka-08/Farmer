@echo off
cd /d "%~dp0"
echo ========================================================
echo  Opening phpMyAdmin Database Viewer (XAMPP MySQL)
echo ========================================================

rem Check if Apache is listening on port 80
netstat -ano | findstr ":80 " | findstr "LISTENING" >nul
if %ERRORLEVEL% neq 0 (
    echo [INFO] Apache port 80 is not currently running.
    if exist "C:\xampp\xampp-control.exe" (
        echo [INFO] Launching XAMPP Control Panel so you can start Apache and MySQL...
        start "" "C:\xampp\xampp-control.exe"
    )
)

echo.
echo Opening phpMyAdmin in your default browser...
start http://localhost/phpmyadmin/index.php?route=/database/structure^&db=agri_supply_chain
echo.
echo Note: If your browser says "localhost refused to connect", open the
echo XAMPP Control Panel window and click the [Start] button next to Apache and MySQL.
echo.
pause
