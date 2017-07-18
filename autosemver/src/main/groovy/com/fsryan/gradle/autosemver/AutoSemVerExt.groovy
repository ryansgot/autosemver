package com.fsryan.gradle.autosemver

import org.gradle.api.Project

class AutoSemVerExt {

    private static final String DEFAULT_VERSION_FILENAME = 'version.lock'
    private static final String DEFAULT_TASK_DEPENDENCIES = Collections.singletonList('build')
    private static final boolean DEFAULT

    String versionFilename = DEFAULT_VERSION_FILENAME
    List<String> taskDependencies = DEFAULT_TASK_DEPENDENCIES

}
