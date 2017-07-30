package com.fsryan.gradle.autosemver.git

import com.fsryan.gradle.autosemver.SourceControlApi
import org.gradle.api.Project

class GitApi implements SourceControlApi {

    Project project

    @Override
    String currentBranch() {
        return "git rev-parse --abbrev-ref HEAD".execute().text.trim()
    }

    @Override
    boolean checkoutNewBranch(String branchName) {
        return commandSuccessful("git checkout -b $branchName")
    }

    @Override
    boolean commit(String fileName, String message) {
        git('add', fileName)
        git('commit', '-m', "'$message'")
        return true
    }

    @Override
    boolean push(String remote, String branch) {
        throwIfNullOrEmpty(remote, "Cannot pushStatus to null/empty remote")
        throwIfNullOrEmpty(branch, "Cannot pushStatus to null/empty branch")
        git('push', remote, branch)
        return true
    }

    private String git(Object... arguments) {
        def output = new ByteArrayOutputStream()
        project.exec {
            executable 'git'
            args arguments
            standardOutput output
            ignoreExitValue = true
        }.assertNormalExitValue()
        return output.toString().trim()
    }

    private static void throwIfNullOrEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(message)
        }
    }
}
