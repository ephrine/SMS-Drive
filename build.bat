:: This batch file gradle builds all release apk for production
ECHO OFF
set message====================== Welcome to SMS Drive ===================
set message0= Product of Ephrine Apps (C) 2020 
set message1= Developed by Devesh Chaudhari (C) 2020
set message2= Building Release APK .....

echo %message%
echo %message0%
echo %message1%
echo %message2%
for /F "tokens=2" %%i in ('date /t') do set mydate=%%i
set mytime=%time%
echo started at %mydate%:%mytime%

gradlew tasks clean --stacktrace && gradlew tasks build --stacktrace && gradlew tasks assembleRelease --stacktrace

set messageend======================SMS Drive Debug APK has Generated===================  
echo %messageend%
for /F "tokens=2" %%i in ('date /t') do set mydate=%%i
set mytime=%time%
echo Finished at %mydate%:%mytime%

PAUSE