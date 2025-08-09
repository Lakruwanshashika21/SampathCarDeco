@echo off
echo [🧹] Cleaning previous build...
rmdir /s /q out >nul 2>&1
mkdir out

echo [🛠️] Compiling all Java files...
javac -encoding UTF-8 -cp "lib/*" -d out src\*.java

if %errorlevel% neq 0 (
    echo [❌] Compilation failed. Check above errors.
    pause
    exit /b
)

echo [✅] Compilation successful.
pause
