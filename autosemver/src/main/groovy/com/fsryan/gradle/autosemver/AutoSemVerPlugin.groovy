package com.fsryan.gradle.autosemver

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task

class AutoSemVerPlugin implements Plugin<Project> {

    private static Map<String, String> versionBumpTasks = [
            "majorVersionBump" : "Increments the major version",
            "minorVersionBump" : "Increments the minor version",
            "patchVersionBump" : "Increments the patch version"
    ]

    void apply(Project project) {

        project.afterEvaluate {


        }

    }
}

