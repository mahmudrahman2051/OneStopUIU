@echo off
color 0A
echo.
echo  =========================================
echo          OneStopUIU - Presentation Demo
echo  =========================================
echo.
echo  University: United International University
echo  Project: OneStopUIU Management System  
echo  Date: %date%
echo.
echo  =========================================
echo.

rem Set JAVA_HOME to Java 23
set JAVA_HOME=C:\Program Files\Java\jdk-23

rem Change to project directory
cd /d "C:\Users\mahmu\OneDrive\Desktop\OneStopUIU"

echo [1/3] Checking Java version...
java -version
echo.

echo [2/3] Compiling project...
call mvnw.cmd clean compile -q

rem Check if compilation was successful
if %ERRORLEVEL% neq 0 (
    color 0C
    echo.
    echo  ========================================
    echo      COMPILATION FAILED!
    echo  ========================================
    echo  Please check for errors above.
    pause
    exit /b 1
)

color 0B
echo.
echo  ========================================
echo      COMPILATION SUCCESSFUL!
echo  ========================================
echo.
echo  Database: onestopuiu (MySQL)
echo  Server: localhost:3306
echo  Application: JavaFX with FXML
echo.
echo  Starting OneStopUIU Application...
echo  ========================================
echo.

rem Run the JavaFX application
call mvnw.cmd javafx:run

echo.
echo  ========================================
echo      PRESENTATION DEMO COMPLETED
echo  ========================================
pause