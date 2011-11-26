#!/bin/bash
cp ../build/MyBot.jar ../archive/$1.jar
pushd ../build
mkdir $1
pushd $1
cp ../../src/*.java .
zip $1 *.java
cp $1.zip ../../archive/
popd
popd

