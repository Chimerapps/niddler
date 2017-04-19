#!/bin/bash

./gradlew :niddler-lib:clean :niddler-lib:assembleRelease :niddler-lib:publish :niddler-lib:bintrayUpload
./gradlew :niddler-lib-noop:clean :niddler-lib-noop:assembleRelease :niddler-lib-noop:publish :niddler-lib-noop:bintrayUpload
