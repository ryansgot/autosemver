package com.fsryan.gradle.autosemver

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class AutoSemVerExt {

    public static final String NAME = "autosemver"

    /**
     * <p>
     *   The name of the version file
     * </p>
     */
    String versionFilename = 'version.lock'

    private NamedDomainObjectContainer<BranchConfig> branchConfigs

    AutoSemVerExt(NamedDomainObjectContainer<BranchConfig> branchConfigs) {
        this.branchConfigs = branchConfigs
        branchConfigs.each { bc ->
            bc.skipCiCommitMessageSuffix = bc.skipCiCommitMessageSuffix ?: '[skip ci]'
            bc.taskDependencies = bc.taskDependencies ?: Collections.emptyList()
            bc.pullRemote = bc.pullRemote ?: 'origin'
            bc.pushRemote = bc.pushRemote ?: 'origin'
        }
    }

    def branchConfigs(final Closure c) {
        branchConfigs.configure(c)
    }

    def forEachBranchConfig(final Closure c) {
        branchConfigs.each(c)
    }

    BranchConfig branchConfigOf(String branch) {
        return branchConfigs.getByName(branch)
    }

    File getVersionFile(Project project) {
        return new File(project.projectDir.absolutePath + File.separator + versionFilename)
    }
}
