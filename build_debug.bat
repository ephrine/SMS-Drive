:: This batch file gradle builds all release apk for production
ECHO OFF

set message======================Welcome to SMS Drive Build===================  
echo %message%

gradlew tasks assembleGalaxy --stacktrace && gradlew tasks assembleMaster --stacktrace


set messageend======================Welcome to SMS Drive Debug APK has Generated===================  
echo %messageend%

PAUSE