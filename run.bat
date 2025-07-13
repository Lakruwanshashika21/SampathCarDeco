@echo off
echo === Compiling Java source files ===
javac -cp "lib/*" -d out src/*.java

if %errorlevel% neq 0 (
    echo ❌ Compilation failed. Fix the errors above.
    pause
    exit /b
)

echo === Running the application ===
java -cp "out;lib/*" Main
pause
