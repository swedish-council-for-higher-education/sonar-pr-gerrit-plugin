# sonar-pr-gerrit-plugin

This is **WIP!**

A Sonar plugin that performs a Gerrit vote on a Pull Request analysis. The plugin simply vote -1 or 1 depending on if the Sonar Quality gate was passed or failed. **Note** that the plugin only performs the Gerrit vote. Any issues found by Sonar is not added as comments in Gerrit, instead a link to the sonar pull request is provided in the Gerrit comment. The plugin requires a SonarQube™ plan with the Pull Request feature. 


## Installation

See SonarQube™ documentation.

## Configuration

In your SonarQube server, see Administration -> Configuration -> General Settings and `Gerrit PR`.

## System requirements 

* SonarQube™ version 8.8

## Release new version

Updated version in `pom.xml`.

`mvn release:prepare`
`mvn release:perform`


## ToDo

* Upgrade minimal version to SonarQube™ version 8.9 LTS
