@echo off
cd /d H:\MapleGame

if not exist out mkdir out
if not exist save mkdir save

echo Compiling...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo Compile FAILED. Check errors above.
    pause
    exit /b 1
)

echo Done. Starting game...
java -cp out maplestory.Main
pause
