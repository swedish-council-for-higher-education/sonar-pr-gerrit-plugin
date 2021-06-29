# sonar-pr-gerrit-plugin

SonarQube plugin that posts Gerrit votes depending on the Pull Request analysis result. The plugin simply vote -1 or 1 depending on if the Sonar Quality gate was passed or failed. **Note** the plugin only posts the Gerrit vote. Any issues found by Sonar is not added as comments in Gerrit, instead a link to the sonar pull request is provided in the Gerrit review message. The plugin requires a SonarQube™ plan with the Pull Request feature.

## Download binaries

Binaries are available on [GitHub](https://github.com/swedish-council-for-higher-education/sonar-pr-gerrit-plugin/packages/862593).

## Installation

See SonarQube™ documentation.

## Configuration

In your SonarQube server, see Administration -> Configuration -> General Settings and `Gerrit PR`.

In `gerrit.pr.gerrit.success.message` and `gerrit.pr.gerrit.failed.message` the following variables may be used:

* `project.key`      - The project key
* `project.name`     - The project name
* `pullrequest.key`  - The pull request id

### Example:

You could configure `gerrit.pr.gerrit.failed.message` to provide a link to the list of issues for the particular pull request.

```
Sonar review failed, see http://myurl.com/dashboard?id=${project.key}&pullRequest=${pullrequest.key}
```

## Build Env

You must provide the following properties to the PR build:

* sonar.pullrequest.base
* sonar.pullrequest.branch
* sonar.pullrequest.key

### Jenkins Gerrit trigger

If you are using [Jenkins Gerrit trigger](https://github.com/jenkinsci/gerrit-trigger-plugin) and Maven you may do something like this in the build triggered by `patchset-created`:

```groovy
sh "./mvnw
    -Dsonar.pullrequest.base=${params.GERRIT_BRANCH} \
    -Dsonar.pullrequest.branch=${params.GERRIT_REFSPEC.replaceFirst('refs/', '')} \
    -Dsonar.pullrequest.key=${params.GERRIT_CHANGE_NUMBER} \
    sonar:sonar"
```

## System requirements 

* SonarQube™ version 8.9 or later.

## Build

Java version 11 or later required.

`./mvnw clean package`

### Release new version

In GitHub create a new release, set tag with format n.n.n, and press `Publish release`.

## Revision history

`1.0.0` - First version

`1.1.0` - Removed configuration `gerrit.pr.sonar.url`