@echo off
echo Compiling OneStopUIU Application...

cd /d "c:\Users\mahmu\OneDrive\Desktop\OneStopUIU"

echo Cleaning previous compilation...
if exist target\classes rmdir /s /q target\classes
mkdir target\classes

echo Compiling Java files...
javac -cp "target\lib\*;." -d target\classes --module-path "target\lib" --add-modules javafx.controls,javafx.fxml -sourcepath src\main\java src\main\java\com\example\onestopuiu\*.java src\main\java\com\example\onestopuiu\controller\*.java src\main\java\com\example\onestopuiu\dao\*.java src\main\java\com\example\onestopuiu\model\*.java src\main\java\com\example\onestopuiu\util\*.java

echo Copying resources...
xcopy /s /y src\main\resources\* target\classes\

echo Compilation complete!
pause