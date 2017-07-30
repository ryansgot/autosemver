# autosemver
A gradle plugin that helps you automatically version your project in accordance with your project's branching strategy. This plugin works best in concert with your chosen CI system.

## Quickstart
1. In your project's root build.gradle file:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // Currently, only git is supported
        classpath 'com.fsryan.gradle.autosemver:autosemver-git:0.0.1'
    }
}
```
2. In your any versioned project's build.gradle (you can also embed this in the allprojects closure of your root project if you want the same config for all subprojects)
```groovy
autosemver {
    branchConfigs {

        // In this model, bugfixes/features would be developed on bugfix/feature
        // branches that would not be monitored by autosemver. When they are
        // merged, they would get merged into the integration branch (presumably
        // a successful build on this branch would cause some sort of publishing
        // to a place that your QA/development team could use).
        integration {
            skipCiCommitMessageSuffix = '[skip ci]'
            pullRemote = 'origin'
            pushRemote = 'origin'
            versionIncrement = "patch"
            preRelease = 'SNAPSHOT' // adds -SNAPSHOT to the version
            metaData = Long.toString(new Date().getTime())  // add the +timestamp as meta data
        }

        // Then, when the build is verified to be working, you would merge to
        // alpha from integration wold presumably also publish to your alpha
        // customers. Therefore, this configuration bumps the minor version
        // and changes the preRelease to -alpha and removes any meta data
        alpha {
            skipCiCommitMessageSuffix = '[skip ci]'
            pullRemote = 'origin'
            pushRemote = 'origin'
            versionIncrement = 'minor'  // make sure to build AFTER pushing version bump commit
            preRelease = "alpha"
            // not setting meta data removes previous meta data
        }

        // Then, when your alpha customers have signed off, merging to beta
        // from alpha would presumably also publish to your beta customers.
        // Therefore, this configuration preserves the major.minor.patch
        // version and changes the -alpha to -beta and removes any meta data
        beta {
            skipCiCommitMessageSuffix = '[skip ci]'
            pullRemote = 'origin'
            pushRemote = 'origin'
            preRelease = "beta" // updates the preRelease to -beta
            // not setting versionIncrement preserves previous version
            // not setting meta data removes previous meta data
        }

        // Then, when your beta customers have signed off, merging to master
        // from alpha would presumably also publish to whatever stable channel
        // holds your stable versions. Therefore, this configuration preserves
        // the major.minor.patch version and removes any preRelease or
        // meta data
        master {
            pullRemote = 'origin'
            pushRemote = 'origin'
            // not setting versionIncrement preserves previous major.minor.patch version
            // not setting preRelease removes previous preRelease
            // not setting meta data removes previous meta data
        }
    }
}
```
3. In each subproject, create a file called version.lock, and populate it with your project's current version.
4. Remove any ```project.version = ``` code from any versioned project's build.gradle (if an android project, also remove anything that sets the ```android.defautConfig.versionName``` or ```android.defaultConfig.versionCode``` as this will be handled for you by autosemver.
5. On the command line:
```bash
./gradlew bumpVersion
```
Given the above config, if your current branch is integration, alpha, beta, or master, then the appropriate incrementing of the version will happen.

## How does this prevent endless building?
Your CI system should be able to filter commits with some string like ```'[skip ci]```. If it can't do that, then we may need to extend this plugin to account for that.