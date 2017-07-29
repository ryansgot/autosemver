package com.fsryan.gradle.autosemver

import org.gradle.api.Project

class ProjectHelper {

    static boolean isRootProject(Project p) {
        return p == p.rootProject
    }

    static boolean hasSubprojects(Project p) {
        return !p.subprojects.isEmpty()
    }

    static boolean isAndroidProject(Project p) {
        return p.hasProperty('android')
    }

    static void updateProjectVersion(Project project, VersionSummary versionSummary) {
        project.version = versionSummary.toString()
        if (isAndroidProject(project)) {
            def androidExt = project.extensions.findByName('android')
            def defaultConfig = androidExt.properties.get('defaultConfig')

            // These are mainly for verification purposes
            println "Setting android defaultConfig versionName to: ${project.version}"
            println "Setting android defaultConfig versionCode to: ${versionSummary.toVersionCode()}"

            defaultConfig.versionName = project.version
            defaultConfig.versionCode = versionSummary.toVersionCode()
        }
    }
}
