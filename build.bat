@echo off

:: Configurations
set FOLDER=build
set VERSION=1.0

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
move .\HueSetColor\target\hue-set-color-%VERSION%.jar .\%FOLDER%\Hue-Set-Color-%VERSION%.jar