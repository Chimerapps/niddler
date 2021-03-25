#!/bin/bash
./gradlew clean --no-daemon
./gradlew \
:niddler:publishReleasePublicationToSonatypeRepository \
:niddler-base:publishReleasePublicationToSonatypeRepository \
:niddler-java:publishReleasePublicationToSonatypeRepository \
:niddler-java-noop:publishReleasePublicationToSonatypeRepository \
:niddler-noop:publishReleasePublicationToSonatypeRepository \
:niddler-noop-base:publishReleasePublicationToSonatypeRepository \
:niddler-urlconnection:publishReleasePublicationToSonatypeRepository \
--no-daemon
