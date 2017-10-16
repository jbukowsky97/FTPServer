#! /bin/bash

echo "compiling server..."
cd server/
javac -d build/ src/*.java
cd ../
echo "compiling client..."
cd client/
javac -d build/ src/*.java
cd ../
echo "done"
