@echo off

:: Configurations
set FOLDER=build
set VERSION=1.0.1

:: Clear output folder
echo Current directory: %CD%
rmdir /q /s %FOLDER%
mkdir %FOLDER%

:: Debug info
echo Folder set to '%FOLDER%'
echo Version set to '%VERSION%'

:: Build maven
call mvn clean package -DskipTests
move .\HueMaster\target\hue-master-%VERSION%.jar .\%FOLDER%\Hue-Master-%VERSION%.jar
move .\HueToggle\target\hue-toggle-%VERSION%.jar .\%FOLDER%\Hue-Toggle-%VERSION%.jar
move .\HueSetState\target\hue-set-state-%VERSION%.jar .\%FOLDER%\Hue-Set-State-%VERSION%.jar
move .\HueSetEffect\target\hue-set-effect-%VERSION%.jar .\%FOLDER%\Hue-Set-Effect-%VERSION%.jar
move .\HueSetAlert\target\hue-set-alert-%VERSION%.jar .\%FOLDER%\Hue-Set-Alert-%VERSION%.jar
move .\HueSetScene\target\hue-set-scene-%VERSION%.jar .\%FOLDER%\Hue-Set-Scene-%VERSION%.jar
call mvn clean

echo Done