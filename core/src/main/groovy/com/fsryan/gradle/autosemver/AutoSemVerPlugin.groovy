package com.fsryan.gradle.autosemver

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.UnknownDomainObjectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.fsryan.gradle.autosemver.ProjectHelper.hasSubprojects
import static com.fsryan.gradle.autosemver.ProjectHelper.isRootProject
import static com.fsryan.gradle.autosemver.ProjectHelper.updateProjectVersion

abstract class AutoSemVerPlugin<T extends SourceControlApi> implements Plugin<Project> {

    private static final Logger log = LoggerFactory.getLogger(AutoSemVerPlugin.class)

    static final String VERSION_BUMP_TASK_NAME = "bumpVersion"

    @Override
    void apply(Project project) {
        final NamedDomainObjectContainer<BranchConfig> branchConfigs = project.container(BranchConfig)
        def ext = project.extensions.create(AutoSemVerExt.NAME, AutoSemVerExt, branchConfigs)

        if (!dependOnSubprojects(project)) {
            updateProjectVersion(project, new VersionSummary(ext.getVersionFile(project).text))
        }

        project.afterEvaluate {
            final String currentBranch = sourceControlApi().currentBranch()
            BranchConfig branchConfig = null
            try {
                branchConfig = ext.branchConfigOf(currentBranch)
            } catch (UnknownDomainObjectException udoe) {
                log.warn("No branch config found for branch: $currentBranch")
            }

            if (dependOnSubprojects(project)) {
                configureRootProjectWithSubprojects(project, branchConfig)
            } else {
                configureVersionedProject(project, ext, branchConfig)
            }
        }
    }

    abstract T sourceControlApi()

    void configureRootProjectWithSubprojects(Project project, BranchConfig branchConfig) {
        project.evaluationDependsOnChildren()
        if (branchConfig == null) {
            createNoOpTasks(project, null)
            return
        }

        project.tasks.create(VERSION_BUMP_TASK_NAME, {
            it.description = 'Push all version bump commits to remote'
            it.group = 'Continuous Integration'
            it.dependsOn(project.subprojects.bumpVersionLocally)
            it.doLast { a ->
                sourceControlApi().push(branchConfig.pushRemote, branchConfig.name)
            }
        })
    }

    void configureVersionedProject(Project project, AutoSemVerExt ext, BranchConfig branchConfig) {
        updateProjectVersion(project, new VersionSummary(ext.getVersionFile(project).text))

        if (branchConfig == null) {
            createNoOpTasks(project, null)
            return
        }

        LocalVersionBumpTask vbt = project.tasks.create(LocalVersionBumpTask.NAME, LocalVersionBumpTask.class)
        vbt.branchName = branchConfig.name
        vbt.sourceControlApi = sourceControlApi()
        vbt.versionFile = ext.getVersionFile(project)
        vbt.branchConfig = overrideRootProjectValues(project, branchConfig)
        vbt.dependsOn(vbt.branchConfig.taskDependencies ?: [])

        if (isRootProject(project)) {
            project.tasks.create(VERSION_BUMP_TASK_NAME, {
                it.description = 'Push all version bump commits to remote'
                it.group = 'Continuous Integration'
                it.dependsOn(LocalVersionBumpTask.NAME)
                it.doLast { a ->
                    sourceControlApi().push(branchConfig.pushRemote, branchConfig.name)
                }
            })
        }
    }

    static boolean dependOnSubprojects(Project project) {
        return isRootProject(project) && hasSubprojects(project)
    }

    static BranchConfig overrideRootProjectValues(Project project, BranchConfig branchConfig) {
        if (isRootProject(project)) {
            return branchConfig
        }

        AutoSemVerExt rootExt = project.rootProject.extensions.findByName('autosemver')
        if (rootExt == null) {
            log.warn("Cannot find autosemver extension in root project")
            return branchConfig
        }

        BranchConfig rootBranchConfig
        try {
            rootBranchConfig = rootExt.branchConfigOf(branchConfig.name)
        } catch (UnknownDomainObjectException udoe) {
            log.warn("Cannot find branchConfig ${branchConfig.name} in root project")
            return branchConfig
        }

        BranchConfig ret = new BranchConfig(branchConfig.name)
        ret.taskDependencies = branchConfig.taskDependencies ?: rootBranchConfig.taskDependencies
        ret.pushRemote = branchConfig.pushRemote ?: rootBranchConfig.pushRemote
        ret.pullRemote = branchConfig.pullRemote ?: rootBranchConfig.pullRemote
        ret.preRelease = branchConfig.preRelease ?: rootBranchConfig.preRelease
        ret.metaData = branchConfig.metaData ?: rootBranchConfig.metaData
        ret.skipCiCommitMessageSuffix = branchConfig.skipCiCommitMessageSuffix ?: rootBranchConfig.skipCiCommitMessageSuffix
        ret.versionIncrement = branchConfig.versionIncrement ?: rootBranchConfig.versionIncrement

        return ret
    }

    static void createNoOpTasks(Project project, String currentBranch) {
        if (dependOnSubprojects(project)) {
            project.tasks.create(VERSION_BUMP_TASK_NAME, {
                it.description = "Does nothing because there is no configuration for branch: $currentBranch"
                it.group = "Continuous Integration"
                it.dependsOn(project.subprojects.bumpVersionLocally)
                it.doLast { a ->
                    println "No configuration for current branch: $currentBranch; $VERSION_BUMP_TASK_NAME will do nothing"
                }
            })
            return
        }

        project.tasks.create(LocalVersionBumpTask.NAME, {
            it.description = "Does nothing because there is no configuration for branch: $currentBranch"
            it.group = "Continuous Integration"
            it.doLast {
                println "No configuration for current branch: $currentBranch; ${LocalVersionBumpTask.NAME} will do nothing"
            }
        })

        if (isRootProject(project)) {
            project.tasks.create(VERSION_BUMP_TASK_NAME, {
                it.description = "Does nothing because there is no configuration for branch: $currentBranch"
                it.group = "Continuous Integration"
                it.dependsOn(LocalVersionBumpTask.NAME)
                it.doLast { a ->
                    println "No configuration for current branch: $currentBranch; $VERSION_BUMP_TASK_NAME will do nothing"
                }
            })
        }
    }
}

