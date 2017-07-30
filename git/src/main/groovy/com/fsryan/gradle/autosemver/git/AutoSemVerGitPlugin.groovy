package com.fsryan.gradle.autosemver.git

import com.fsryan.gradle.autosemver.AutoSemVerPlugin
import org.gradle.api.Project

class AutoSemVerGitPlugin extends AutoSemVerPlugin<GitApi> {

    private GitApi gitApi = new GitApi()

    @Override
    void apply(Project project) {
        gitApi.project = project
        super.apply(project)
    }

    @Override
    GitApi sourceControlApi() {
        return gitApi
    }
}
