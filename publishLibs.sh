#!/bin/bash

./gradlew clean :niddler-lib-android:assembleRelease :niddler-lib-android-noop:assembleRelease :niddler-lib-java-noop:assemble :niddler-lib-java:assemble --no-daemon
./gradlew :niddler-lib-base:bintrayUpload :niddler-lib-base-noop:bintrayUpload :niddler-lib-java:bintrayUpload :niddler-lib-java-noop:bintrayUpload :niddler-lib-android:bintrayUpload :niddler-lib-android-noop:bintrayUpload --no-daemon
