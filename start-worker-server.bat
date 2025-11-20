@echo off
echo ========================================
echo   WORKER TCP SERVER LAUNCHER
echo ========================================
echo.

REM Set classpath
set CLASSPATH=build\classes;src\main\webapp\WEB-INF\lib\*

echo Starting Worker TCP Server...
echo Press Ctrl+C to stop
echo.

java -cp "%CLASSPATH%" Service.WorkerTCPServer

pause
