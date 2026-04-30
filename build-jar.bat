@echo off
REM Builds TaskManagerGUI.jar — runnable JAR for the GUI demo.
REM Run this once before zipping for the professor.

setlocal

cd /d "%~dp0"

echo ==^> Cleaning bin\
if exist bin rmdir /s /q bin
mkdir bin

echo ==^> Compiling all sources (engine + GUI)
javac -d bin src\main\java\*.java src\main\java\gui\*.java
if errorlevel 1 goto :error

echo ==^> Writing JAR manifest
echo Manifest-Version: 1.0> manifest.txt
echo Main-Class: TaskManagerGUI>> manifest.txt
echo.>> manifest.txt

echo ==^> Building TaskManagerGUI.jar
jar cfm TaskManagerGUI.jar manifest.txt -C bin .
del manifest.txt

echo ==^> Done. To launch:
echo     java -jar TaskManagerGUI.jar
goto :eof

:error
echo BUILD FAILED
exit /b 1
