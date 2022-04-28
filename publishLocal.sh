#!/bin/bash
./gradlew clean --no-daemon
./gradlew \
:niddler:publishReleasePublicationToMavenLocal \
:niddler-base:publishReleasePublicationToMavenLocal \
:niddler-java:publishReleasePublicationToMavenLocal \
:niddler-java-noop:publishReleasePublicationToMavenLocal \
:niddler-noop:publishReleasePublicationToMavenLocal \
:niddler-noop-base:publishReleasePublicationToMavenLocal \
:niddler-urlconnection:publishReleasePublicationToMavenLocal \
--no-daemon