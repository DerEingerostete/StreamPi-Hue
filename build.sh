#!/bin/bash

# Configurations
folder="build"
version="1.0.1"
outputName="StreamPi-Hue-$version"

# Moves a build to the target destination
function moveBuild {
  mv "./$1/target/$2-$version.jar" "$folder/$3.jar"
}

function downloadDependency {
  echo "Downloading dependency $2"
  curl --silent --remote-name --output-dir $folder https://repo1.maven.org/maven2/$1/$2/$3/$2-$3.jar
}

# Install dependencies
echo "Installing packages"
sudo apt-get -qq install -y zip tar curl

# Clear output folder
echo "Current directory: $PWD"
if [ -d "$folder" ]; then rm -rf $folder; fi
mkdir $folder

# Debug info
echo "Folder set to $folder"
echo "Version set to $version"

# Downloading dependencies
echo "Downloading dependencies"
downloadDependency "io/github/zeroone3010" "yetanotherhueapi" "2.7.0"
downloadDependency "com/fasterxml/jackson/core" "jackson-databind" "2.14.2"
downloadDependency "org/jetbrains" "annotations" "24.0.1"

# Build maven
echo Building maven packages
mvn -B -q package -DskipTests --file pom.xml

# Moving files
echo Moving files
moveBuild "HueMaster" "hue-master" "Hue-Master"
moveBuild "HueToggle" "hue-toggle" "Hue-Toggle"
moveBuild "HueSetState" "hue-set-state" "Hue-Set-State"
moveBuild "HueSetEffect" "hue-set-effect" "Hue-Set-Effect"
moveBuild "HueSetAlert" "hue-set-alert" "Hue-Set-Alert"
moveBuild "HueSetScene" "hue-set-scene" "Hue-Set-Scene"

# Cleanup
echo "Run cleanup"
mvn -q clean

# Create archives
find $folder -printf "%P\n" -type f -o -type l -o -type d | tar -czf $outputName.tar.gz --no-recursion -C $folder -T -
zip -r -qq -j $outputName.zip $folder
mv $outputName.tar.gz build
mv $outputName.zip build

echo Done