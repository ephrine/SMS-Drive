:: This batch file gradle builds all release apk for production
ECHO OFF

gradlew tasks assembleGalaxy --stacktrace && gradlew tasks assembleMaster --stacktrace

PAUSE