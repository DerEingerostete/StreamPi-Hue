#!/bin/bash

# Configurations
folder="build"
version="1.0.1"
outputName="StreamPi-Hue-$version"

# Install dependencies
echo "Installing dependencies"
sudo apt -qq install -y zip, tar

# Clear output folder
echo "Current directory: $PWD"
rm -r $folder
mkdir $folder

# Debug info
echo "Folder set to $folder"
echo "Version set to $version"

# Build maven
echo Building maven packages
mvn -B package -DskipTests --file pom.xml

# Moving files
echo Moving files
mv ./HueMaster/target/hue-master-$version.jar ./$folder/Hue-Master-$version.jar
mv ./HueToggle/target/hue-toggle-$version.jar ./$folder/Hue-Toggle-$version.jar
mv ./HueSetState/target/hue-set-state-$version.jar ./$folder/Hue-Set-State-$version.jar
mv ./HueSetEffect/target/hue-set-effect-$version.jar ./$folder/Hue-Set-Effect-$version.jar
mv ./HueSetAlert/target/hue-set-alert-$version.jar ./$folder/Hue-Set-Alert-$version.jar
mv ./HueSetScene/target/hue-set-scene-$version.jar ./$folder/Hue-Set-Scene-$version.jar

# Cleanup
echo "Run cleanup"
mvn clean

# Create directory
find $folder -printf "%P\n" -type f -o -type l -o -type d | tar -czf $outputName.tar.gz --no-recursion -C $folder -T -
zip -r -qq -j $outputName.zip $folder

echo Done