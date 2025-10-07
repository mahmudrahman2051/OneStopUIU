@echo off
echo Starting OneStopUIU Application...
echo.

rem Set JAVA_HOME to Java 23
set JAVA_HOME=C:\Program Files\Java\jdk-23

rem Change to project directory
cd /d "C:\Users\mahmu\OneDrive\Desktop\OneStopUIU"

rem Clean and compile the project
echo Compiling project...
call mvnw.cmd clean compile

rem Check if compilation was successful
if %ERRORLEVEL% neq 0 (
    echo.
    echo Compilation failed! Please check for errors above.
    pause
    exit /b 1
)

echo.
echo Starting application...
echo Database: onestopuiu
echo Server: localhost:3306
echo.

rem Run the JavaFX application
call mvnw.cmd javafx:run

pause