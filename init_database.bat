@echo off
echo ========================================
echo H2 Database Initialization Script
echo ========================================
echo.

REM Xoa database cu (neu co)
echo [1/3] Cleaning old database...
if exist "%USERPROFILE%\media_processor_db.mv.db" (
    del "%USERPROFILE%\media_processor_db.mv.db"
    echo Old database deleted.
) else (
    echo No old database found.
)

REM Khoi tao database moi
echo.
echo [2/3] Creating new database and tables...
java -cp src\main\webapp\WEB-INF\lib\h2-2.2.224.jar org.h2.tools.RunScript -url "jdbc:h2:~/media_processor_db" -user sa -script H2_DATABASE_INIT.sql

REM Kiem tra ket qua
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [3/3] SUCCESS! Database initialized.
    echo.
    echo ========================================
    echo Next steps:
    echo 1. Rebuild your project in Eclipse
    echo 2. Run on Tomcat server
    echo 3. Login with: admin/admin123
    echo ========================================
) else (
    echo.
    echo [ERROR] Failed to initialize database!
    echo Please check if h2-2.2.224.jar exists in src\main\webapp\WEB-INF\lib\
)

echo.
pause
