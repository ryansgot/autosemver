# autosemver
How many times have you seen commits wherein the only change was to your project's version? autosemver is a gradle plugin providing a configuration-based means of automatically updating your project's version based upon your project's branching strategy and following the [semantic versioning standard](http://semver.org). It works best when the ```bumpVersion``` task is performed by your project's CI system rather than you via command line.

## Quickstart
1. In your project's root build.gradle file:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // Currently, only git is supported because that's all I've written, but it should be pretty easy to extend
        classpath 'com.fsryan.gradle.autosemver:autosemver-git:0.0.1'
    }
}
```
2. In your any versioned project's build.gradle (you can also embed this in the allprojects closure of your root project if you want the same config for all subprojects)
```groovy
// At the top
plugins {
    id 'autosemver-git'
}

// Alternatively, you can use the apply syntax (but you should probably apply prior to applying the android plugin if on an android project):
// apply plugin: 'autosemver-git'

/* ... */

autosemver {
    versionFilename = 'version.lock' // version.lock is the default--you do not need to configure this unless the version filename is something else
    branchConfigs {

        // The names of the following properties must correspond exactly to
        // branch names. They allow you to configure how the version gets
        // updated on a branch-by-branch basis.

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
            taskDependencies = ['test']
        }

        // Then, when the build is verified to be working, you would merge to
        // alpha from integration wold presumably also publish to your alpha
        // customers. Therefore, this configuration bumps the minor version
        // and changes the preRelease to -alpha and removes any meta data
        alpha {
            skipCiCommitMessageSuffix = '[skip ci]'
            pullRemote = 'origin'
            pushRemote = 'origin'
            versionIncrement = 'minor'
            preRelease = "alpha"
            taskDependencies = ['test']
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
            taskDependencies = ['test']
        }

        // Then, when your beta customers have signed off, merging to master
        // from beta would presumably also publish to whatever stable channel
        // holds your stable versions. Therefore, this configuration preserves
        // the major.minor.patch version and removes any preRelease or
        // meta data
        master {
            skipCiCommitMessageSuffix = '[skip ci]'
            pullRemote = 'origin'
            pushRemote = 'origin'
            taskDependencies = ['test']
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

## Why create autosemver?
I never wanted to manually bump the version of my libraries/apps again. There are two main problems doing this manually:
1. Inevitably, a developer will forget to bump the version prior to publishing and accidentally overwrite some previously published version.
2. Increasing the version of one low-level library will have a cascading effect wherein you have to not only change the version dependency in the consumers of the libraries, but you have to remember to bump their versions as well.
3. It takes time and brainpower that would be better spent developing.
4. Projects that publish to different groups will have a more complicated strategy for branching and versioning. By automatically updating the version based upon the current branch, you don't need to remember the strategy for versioning--just for branching.

## Why not use some existing plugin?
Others do exist, and if they fit your needs, then feel free to use them. I tried a few, and found some promising ones. The one I most liked was [gradle-release-plugin](https://github.com/netzwerg/gradle-release-plugin), but I found that it did not fit the workflow that I wanted regarding branching. Otherwise, it is a well-done, simple, and useful plugin.

## What tasks get added?
You should run the following:
```bash
./gradlew tasks
```
If you have applied the ```autosemver-git``` plugin, then you should see the following tasks:
```
Continuous Integration tasks
----------------------------
bumpVersion - Push all version bump commits to remote
bumpVersionLocally - Bump the version on the local machine
```
### bumpVersionLocally
There is probably not much reason to run this yourself. It just commits the changes to each versioned project's version.lock file on the local machine. Because this is a task dependency for ```bumpVersion```, running the ```bumpVersion``` task will ensure that ```bumpVersionLocally``` gets run and is successful first.
### bumpVersion
When run, will:
1. Check whether the current branch has been configured in the autosemver extension. If not skip all of the following steps.
2. Reads the current version from the project's version.lock file
3. Increments the version and writes to the project's version.lock file
4. Commits the changes to the version.lock file locally
5. Pushes to the remote configured in the ```pushRemote``` property of the autosemver.buildConfig.branchName property on the current branch.

## How does autosemver prevent infini-build?
Since autosemver pushes to the current branch, and since your CI environment is monitoring the current branch, you could see how you may end up with an unending cycle of builds. However, Your CI system should be able to filter commits with some string like ```'[skip ci]```. If it can't do that, then we may need to extend this plugin to account for that.