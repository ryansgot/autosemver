package com.fsryan.gradle.autosemver

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class VersionBumpTask extends DefaultTask {

    public static final String NAME = "bumpVersion"

    String branchName
    File versionFile
    SourceControlApi sourceControlApi
    BranchConfig branchConfig

    VersionBumpTask() {
        description = "Bump the version"
        group = "Continuous Integration"
    }

    @TaskAction
    void bump() {
        final VersionSummary versionSummary = new VersionSummary(versionFile.text.trim())
        update(versionSummary)
        versionFile.write(versionSummary.toString())
        final String commitMessage = "bump version to ${versionSummary} ${branchConfig.skipCiCommitMessageSuffix}"
        if (!sourceControlApi.commit(versionFile.absolutePath, commitMessage)) {
            throw new IllegalStateException("failed to commit with message: $commitMessage on branch: $branchName")
        }
    }

    private void update(VersionSummary versionSummary) {
        if (branchConfig.incrementing()) {
            increment(versionSummary, branchConfig.versionIncrement)
        }
        if (branchConfig.isPreRelease()) {
            versionSummary.preRelease = branchConfig.preRelease
        }
        if (branchConfig.hasMetaData()) {
            versionSummary.metaData = branchConfig.metaData
        }
    }

    private static void increment(VersionSummary versionSummary, String type) {
        switch (type) {
            case "major":
                versionSummary.major++
                break
            case "minor":
                versionSummary.minor++
                break
            case "patch":
                versionSummary.patch++
        }
    }
}
