@echo off
cls

echo Compiling Java files...
javac -cp "lib/*" -d bin src\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b
)

echo Running the program...
java -cp "bin;lib/*" MainAppFrame

pause
