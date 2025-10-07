@echo off
echo Starting OneStopUIU with Professional Design...
echo.
echo Features:
echo - Professional gradient login design
echo - Enhanced performance with background processing
echo - Smart image caching and resource management
echo - Splash screen with loading indicator
echo.

REM Set JavaFX module path if needed
set JAVAFX_PATH=--module-path "C:\Users\%USERNAME%\.m2\repository\org\openjfx" --add-modules javafx.controls,javafx.fxml

REM Run the application
mvnw.cmd clean javafx:run

pause