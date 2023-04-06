#!/bin/bash

# Moves a build to the target destination
function moveBuild {
  mv "$1/target/$2-$version.jar" "$folder/$3.jar"
}

function downloadDependency {
  echo "Downloading dependency $2"
  curl --silent --remote-name --output-dir "$folder" "https://repo1.maven.org/maven2/$1/$2/$3/$2-$3.jar"
}

function getProjectVersion {
  grep -m 1 "<version>" pom.xml | sed -E 's/.*>(.+)<.*/\1/'
}

# Configurations
folder="build"
version=$(getProjectVersion)
outputName="StreamPi-Hue-$version"

# Debug info
echo "Building plugin"
echo "Version: $version"
echo

# Install dependencies
echo "Installing packages"
sudo apt-get -qq install -y zip tar curl

# Clear output folder
if [ -d "$folder" ]; then rm -rf $folder; fi
mkdir $folder

# Downloading dependencies
echo "Downloading dependencies"
downloadDependency "io/github/zeroone3010" "yetanotherhueapi" "2.7.0"
downloadDependency "com/fasterxml/jackson/core" "jackson-databind" "2.14.2"
downloadDependency "org/jetbrains" "annotations" "24.0.1"
echo

# Build maven
echo "Building maven packages"
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
echo

# Create archives
echo "Creating archives"
find $folder -printf "%P\n" -type f -o -type l -o -type d | tar -czf "$outputName.tar.gz" --no-recursion -C "$folder" -T -
zip -r -qq -j "$outputName.zip" $folder

# Generating Hash
echo "Generating sha256 checksums"
sha256sum -b "$outputName.zip" "$outputName.tar.gz" > "$folder/sha256.txt"

# Moving files to output folder
mv "$outputName.tar.gz" $folder
mv "$outputName.zip" $folder

echo Done