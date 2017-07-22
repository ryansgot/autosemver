package com.fsryan.gradle.autosemver

import org.gradle.api.Project

class ProjectHelper {

    static boolean isRootProject(Project p) {
        return p == p.rootProject
    }

    static boolean hasSubprojects(Project p) {
        return !p.subprojects.isEmpty()
    }
}
