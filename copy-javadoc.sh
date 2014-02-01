#!/bin/sh

# JavaDoc
rm -rf apidocs/
mkdir apidocs
cp -r ../DavidWebb/target/apidocs/ .

# Coverage Report (JaCoCo)
rm -rf jacoco/
mkdir jacoco
cp -r ../DavidWebb/target/site/jacoco/ .
