@echo off
echo ========================================
echo Starting H2 Database Web Console
echo ========================================
echo.
echo H2 Console will open in your browser at:
echo http://localhost:8082
echo.
echo Connection settings:
echo   JDBC URL: jdbc:h2:~/media_processor_db
echo   User Name: sa
echo   Password: (leave empty)
echo.
echo Press Ctrl+C to stop the console server
echo ========================================
echo.

java -cp src\main\webapp\WEB-INF\lib\h2-2.2.224.jar org.h2.tools.Server -web -webPort 8082 -browser

pause
