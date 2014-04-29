#!/bin/sh

# JavaDoc
rm -rf apidocs/
mkdir apidocs
cp -r ../DavidWebb/target/apidocs/ .

# Coverage Report (JaCoCo)
rm -rf jacoco/
mkdir jacoco
cp -r ../DavidWebb/target/site/jacoco/ .

# github pages do not deliver .resources directory
if [ ! -d jacoco/resources ]; then
    mv jacoco/.resources jacoco/resources
fi
find jacoco/ -name "*.html" -exec sed -i 's/\.resources/resources/g' {} \;
