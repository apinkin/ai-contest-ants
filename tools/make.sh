#!/bin/bash
pushd ../build
rm *.jar *.class
javac ../src/*.java
mv ../src/*.class .
jar cvfm MyBot.jar Manifest.txt *.class
rm *.class
popd

