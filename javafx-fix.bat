@echo off
echo ================================================
echo OneStopUIU - JavaFX Configuration Fix
echo ================================================
echo.

echo Checking JavaFX configuration...
echo.

echo STEP 1: Checking Java version
java -version
echo.

echo STEP 2: Checking Maven dependencies
if exist "pom.xml" (
    echo Found pom.xml - Maven project detected
    echo JavaFX dependencies should be automatically managed
) else (
    echo ERROR: pom.xml not found!
)
echo.

echo STEP 3: Alternative run methods
echo.
echo METHOD 1 - IntelliJ IDEA (Recommended):
echo 1. In IntelliJ, go to Run -> Edit Configurations
echo 2. Select "OneStopUIU - Simple" configuration (if available)
echo 3. Or create a new Application configuration with:
echo    - Main class: com.example.onestopuiu.OneStopUIUApplication
echo    - VM options: --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED
echo    - Use classpath of module: OneStopUIU
echo.

echo METHOD 2 - Maven (if JAVA_HOME is set):
echo Run: mvnw clean javafx:run
echo.

echo METHOD 3 - Direct Java execution:
echo This requires proper classpath setup
echo.

echo TROUBLESHOOTING TIPS:
echo.
echo 1. Make sure you're using the correct Java version (17 or higher)
echo 2. Ensure IntelliJ is using the correct JDK in Project Structure
echo 3. Try invalidating caches: File -> Invalidate Caches and Restart
echo 4. Reimport Maven project: Right-click pom.xml -> Maven -> Reload project
echo 5. Check that JavaFX modules are in the classpath, not module path
echo.

echo ================================================
echo Press any key to continue...
pause >nul