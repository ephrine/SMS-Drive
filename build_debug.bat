:: This batch file gradle builds all release apk for production
ECHO OFF

set message====================== Welcome to SMS Drive Build ===================  
set message0= Product of Ephrine Apps (C) 2020 
set message1= Developed by Devesh Chaudhari (C) 2020
set message2= Building Debug APK .....

echo %message%
echo %message0%
echo %message1%
echo %message2%
gradlew tasks assembleGalaxy --stacktrace && gradlew tasks assembleMaster --stacktrace


set messageend====================== SMS Drive Debug APK has Generated ===================  
echo %messageend%

PAUSE