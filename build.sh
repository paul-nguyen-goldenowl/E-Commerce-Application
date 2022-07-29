git log  --pretty=oneline | tail -n 10 >> log.txt
./gradlew clean
./gradlew assembleRelease

ls -l app/release/app-release.apk
ls -l app/build/outputs/apk/release/app-release.apk
./gradlew bundleRelease
firebase appdistribution:distribute app/release/app-release.apk  \
--app 1:174656673319:android:8d3f5c6f67515b9c46e002  \
--release-notes-file ./log.txt --groups go