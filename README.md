<h1 align="center">
  <br/>
  <a href="https://github.com/DerEingerostete/StreamPi-Hue"><img src="https://files.dereingerostete.dev/GitHubAssets/StreamPi-Hue-Light.png" alt="StreamPi Hue" width="600"></a>
</h1>

___

<p align="center">
  <a href="#features">Features</a> •
  <a href="#actions">Actions</a> •
  <a href="#dependencies">Dependencies</a> •
  <a href="#download--install">Download & Install</a> •
  <a href="#license">License</a>
</p>

## A StreamPi Plugin
StreamPi Hue is a small plugin that allows the control of your Philips Hue lights via StreamPi. 

## Features
- Toggle Lights or Groups on / off
- Set the State of Lights or Groups (Color, Brightness, Hue, Saturation, Transition Time)
- Set alerts for Lights or Groups
- Set effects for Lights or Groups
- Selected Scenes

## Actions
StreamPi Hue contains the following actions:
 - **Toggle Light:** Toggles a light / room on or off
 - **Set State:** Set the state of a light or room
 - **Set Scene:** Sets the active scene of a light or room
 - **Set Effect:** Sets the effect of a light or room
 - **Set Alert:** Sets the alert of a light or room

## Dependencies
StreamPi Hue requires three dependencies:
1. [Yet Another Hue API](https://github.com/ZeroOne3010/yetanotherhueapi):
   - **Version:** 2.7.0
   - **License:** MIT License
   - **Usage:** Main dependency for the core functionality
   - [Download Jar](https://repo1.maven.org/maven2/io/github/zeroone3010/yetanotherhueapi/2.7.0/yetanotherhueapi-2.7.0.jar)
2. [Jackson Databind](https://github.com/FasterXML/jackson-databind):
   - **Version:** 2.14.2
   - **License:** Apache License 2.0
   - **Usage:** Provides functionality to 'Yet Another Hue API'
   - **Downloads:**
     - [Download Databind Jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.14.2/jackson-databind-2.14.2.jar)
     - [Download Core Jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.14.2/jackson-core-2.14.2.jar)
     - [Download Annotations Jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.14.2/jackson-annotations-2.14.2.jar)
3. [JetBrains Java Annotations](https://github.com/JetBrains/java-annotations):
    - **Version:** 24.0.1
    - **License:** Apache License 2.0
    - **Usage:** Speeds up development by proving helpful annotations
    - [Download Jar](https://repo1.maven.org/maven2/org/jetbrains/annotations/24.0.1/annotations-24.0.1.jar)

## Download & Install
To add the plugin to your StreamPi application, simply download the [latest build](https://github.com/DerEingerostete/StreamPi-Hue/releases/latest) from the [release tab](https://github.com/DerEingerostete/StreamPi-Hue/releases), extract the archive, and paste the jar files into the plugin directory located in your user directory at ``$HOME/Stream-Pi/Server/Plugins``.

## License
Distributed under the GPL-3.0 License. See [`LICENSE`](/LICENSE) for more information.
