::WIN BATCH SCRIPT

:: CHANGE THESE
set app_package=com.example.alan.smartvanity
set dir_app_name=abSmartVanity
set MAIN_ACTIVITY=MainActivity

set ADB="adb"
::ADB_SH="%ADB% shell" # this script assumes using `adb root`. for `adb su` 
see `Caveats`

set path_sysapp=/system/priv-app
set apk_host=.\Application\build\outputs\apk\Application-debug.apk
set apk_name=%dir_app_name%.apk
set apk_target_dir=%path_sysapp%/%dir_app_name%
set apk_target_sys=%apk_target_dir%/%apk_name%

:: Delete previous APK
del %apk_host%

:: Compile the APK: you can adapt this for production build, flavors, etc.
call gradlew assembleDebug

set ADB_SH=%ADB% shell su -c

:: Install APK: using adb su
%ADB_SH% mount -o rw,remount /system
%ADB_SH% chmod 777 /system/lib/
%ADB_SH% mkdir -p /sdcard/tmp
%ADB_SH% mkdir -p %apk_target_dir%
%ADB% push %apk_host% /sdcard/tmp/%apk_name% 
%ADB_SH% mv /sdcard/tmp/%apk_name% %apk_target_sys%
%ADB_SH% rmdir /sdcard/tmp

:: Give permissions
%ADB_SH% chmod 755 %apk_target_dir%