#!/bin/bash
pushd ../build
mkdir $1
pushd $1
cp ../../src/*.java .
zip $1 *.java
popd
popd

