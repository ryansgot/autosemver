package com.fsryan.gradle.autosemver

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.UnknownDomainObjectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.fsryan.gradle.autosemver.ProjectHelper.hasSubprojects
import static com.fsryan.gradle.autosemver.ProjectHelper.isRootProject

abstract class AutoSemVerPlugin<T extends SourceControlApi> implements Plugin<Project> {

    private static final Logger log = LoggerFactory.getLogger(AutoSemVerPlugin.class)

    @Override
    void apply(Project project) {
        final NamedDomainObjectContainer<BranchConfig> branchConfigs = project.container(BranchConfig)
        def ext = project.extensions.create(AutoSemVerExt.NAME, AutoSemVerExt, branchConfigs)

        project.afterEvaluate {

            final String currentBranch = sourceControlApi().currentBranch()
            if (currentBranch == null || currentBranch.isEmpty()) {
                createNoOpTasks(project, null)
                return
            }

            BranchConfig branchConfig
            try {
                branchConfig = ext.branchConfigOf(currentBranch)
            } catch (UnknownDomainObjectException udoe) {
                createNoOpTasks(project, currentBranch)
                return
            }

            // the subprojects are the ones that will be versioned
            if (isRootProject(project) && hasSubprojects(project)) {
                project.evaluationDependsOnChildren()
                project.tasks.create('pushVersionBumpCommits', {
                    it.description = 'Push all version bump commits to remote'
                    it.group = 'Continuous Integration'
                    it.dependsOn(project.subprojects.bumpVersion)
                    it.doLast { a ->
                        sourceControlApi().push(branchConfig.pushRemote, currentBranch)
                    }
                })
                return
            }

            VersionBumpTask vbt = project.tasks.create(VersionBumpTask.NAME, VersionBumpTask.class)
            vbt.branchName = currentBranch
            vbt.sourceControlApi = sourceControlApi()
            vbt.versionFile = ext.getVersionFile(project)
            vbt.branchConfig = overrideRootProjectValues(project, branchConfig)
            vbt.dependsOn(branchConfig.taskDependencies ?: [])

            if (isRootProject(project)) {
                project.tasks.create('pushVersionBumpCommits', {
                    it.dependsOn('bumpVersion')
                    it.doLast { a ->
                        sourceControlApi().push(branchConfig.pushRemote, currentBranch)
                    }
                })
            }
        }
    }

    abstract T sourceControlApi()

    static BranchConfig overrideRootProjectValues(Project project, BranchConfig branchConfig) {
        if (isRootProject(project)) {
            return branchConfig
        }

        AutoSemVerExt rootExt = project.rootProject.extensions.findByName('autosemver')
        if (rootExt == null) {
            log.warning("Cannot find autosemver extension in root project")
            return branchConfig
        }

        BranchConfig rootBranchConfig
        try {
            rootBranchConfig = rootExt.branchConfigOf(branchConfig.name)
        } catch (UnknownDomainObjectException udoe) {
            log.warning("Cannot find branchConfig ${branchConfig.name} in root project")
            return branchConfig
        }

        println "${project.name} branchConfig ${branchConfig.name} prior to filling in root: $branchConfig"

        BranchConfig ret = new BranchConfig(branchConfig.name)
        ret.taskDependencies = branchConfig.taskDependencies ?: rootBranchConfig.taskDependencies
        ret.pushRemote = branchConfig.pushRemote ?: rootBranchConfig.pushRemote
        ret.pullRemote = branchConfig.pullRemote ?: rootBranchConfig.pullRemote
        ret.preRelease = branchConfig.preRelease ?: rootBranchConfig.preRelease
        ret.metaData = branchConfig.metaData ?: rootBranchConfig.metaData
        ret.skipCiCommitMessageSuffix = branchConfig.skipCiCommitMessageSuffix ?: rootBranchConfig.skipCiCommitMessageSuffix
        ret.versionIncrement = branchConfig.versionIncrement ?: rootBranchConfig.versionIncrement

        println "${project.name} branchConfig ${ret.name} after filling in root: $ret"

        return ret
    }

    static void createNoOpTasks(Project project, String currentBranch) {
        if (isRootProject(project) && hasSubprojects(project)) {
            project.evaluationDependsOnChildren()
            project.tasks.create('pushVersionBumpCommits', {
                it.dependsOn(project.subprojects.bumpVersion)
                it.doLast { a ->
                    println "No configuration for current branch: $currentBranch; pushVersionBumpCommits will do nothing"
                }
            })
            return
        }

        project.tasks.create('bumpVersion', {
            it.doLast {
                println "No configuration for current branch: $currentBranch; bumpVersion will do nothing"
            }
        })

        if (isRootProject(project)) {
            project.tasks.create('pushVersionBumpCommits', {
                it.dependsOn 'bumpVersion'
                it.doLast { a ->
                    println "No configuration for current branch: $currentBranch; pushVersionBumpCommits will do nothing"
                }
            })
        }
    }
}

