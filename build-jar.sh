#!/usr/bin/env bash
# Builds TaskManagerGUI.jar — a self-contained runnable JAR for the GUI demo.
# Run this once before zipping for the professor.

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "==> Cleaning bin/"
rm -rf bin
mkdir -p bin

echo "==> Compiling all sources (engine + GUI)"
javac -d bin src/main/java/*.java src/main/java/gui/*.java

echo "==> Writing JAR manifest"
echo "Manifest-Version: 1.0" > manifest.txt
echo "Main-Class: TaskManagerGUI" >> manifest.txt
echo "" >> manifest.txt

echo "==> Building TaskManagerGUI.jar"
jar cfm TaskManagerGUI.jar manifest.txt -C bin .
rm manifest.txt

echo "==> Done. To launch:"
echo "    java -jar TaskManagerGUI.jar"
