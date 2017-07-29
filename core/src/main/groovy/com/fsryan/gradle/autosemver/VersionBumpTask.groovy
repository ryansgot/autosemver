package com.fsryan.gradle.autosemver

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.fsryan.gradle.autosemver.ProjectHelper.updateProjectVersion

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
    void overWriteWithUpdatedVersionAndCommit() {
        final VersionSummary versionSummary = new VersionSummary(versionFile.text.trim())
        update(versionSummary)
        versionFile.write(versionSummary.toString())
        final String commitMessage = "bump version to ${versionSummary} ${branchConfig.skipCiCommitMessageSuffix}"
        if (!sourceControlApi.commit(versionFile.absolutePath, commitMessage)) {
            throw new IllegalStateException("failed to commit with message: $commitMessage on branch: $branchName")
        }

        updateProjectVersion(project, versionSummary)
    }

    private void update(VersionSummary versionSummary) {
        if (branchConfig.incrementing()) {
            versionSummary.increment(branchConfig.versionIncrement)
        }
        // TODO: right now, this only allows for overwriting preRelease and metaData--perhaps you could create a more rich set of features
        if (branchConfig.isPreRelease()) {
            versionSummary.preRelease = branchConfig.preRelease
        }
        if (branchConfig.hasMetaData()) {
            versionSummary.metaData = branchConfig.metaData
        }
    }
}
