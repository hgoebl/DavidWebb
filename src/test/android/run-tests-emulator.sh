#!/bin/bash

# build .apk
/bin/bash ./gradlew.sh assembleDebug

# synchronize time of emulator (otherwise problems with SSL are likely)
adb -e shell date -s `date +"%Y%m%d.%H%M%S"`

adb -e install -r build/outputs/apk/android-debug.apk

# run the tests
adb shell am instrument -w \
  -e class com.goebl.david.tests.DavidWebbAndroidTests \
  com.goebl.david/android.test.InstrumentationTestRunner
