package com.fsryan.gradle.autosemver

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.fsryan.gradle.autosemver.ProjectHelper.updateProjectVersion

class LocalVersionBumpTask extends DefaultTask {

    public static final String NAME = "bumpVersionLocally"

    String branchName
    File versionFile
    SourceControlApi sourceControlApi
    BranchConfig branchConfig

    LocalVersionBumpTask() {
        description = "Bump the version on the local machine"
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
        versionSummary.preRelease = branchConfig.isPreRelease() ? branchConfig.preRelease : null
        versionSummary.metaData = branchConfig.hasMetaData() ? branchConfig.metaData : null
    }
}
