@echo off
echo ========================================
echo RESET DATABASE WITH UTF-8 ENCODING
echo ========================================
echo.

echo [1/3] STOP Tomcat server first!
echo       Press any key AFTER stopping Tomcat...
echo.
pause

echo [2/3] Deleting old database...
set DB_PATH=%USERPROFILE%\media_processor_db.mv.db
if exist "%DB_PATH%" (
    del /F /Q "%DB_PATH%"
    echo       OK: Deleted %DB_PATH%
) else (
    echo       WARNING: Database file not found
)

set DB_TRACE=%USERPROFILE%\media_processor_db.trace.db
if exist "%DB_TRACE%" (
    del /F /Q "%DB_TRACE%"
    echo       OK: Deleted %DB_TRACE%
)

echo.
echo [3/3] Restart Tomcat to create new database with UTF-8...
echo       H2 v2.x automatically uses UTF-8 encoding
echo.

echo ========================================
echo COMPLETED!
echo ========================================
echo.
echo NEXT STEPS:
echo 1. Start Tomcat server
echo 2. Access the web application
echo 3. Upload audio/video file to test
echo 4. Vietnamese text should display correctly!
echo.
pause
