#!/bin/bash

# synchronize time of emulator (otherwise problems with SSL are likely)
adb -e shell date -s `date +"%Y%m%d.%H%M%S"`

# run the tests
adb shell am instrument -w \
  -e class com.goebl.david.tests.DavidWebbAndroidTests \
  com.goebl.david/android.test.InstrumentationTestRunner
