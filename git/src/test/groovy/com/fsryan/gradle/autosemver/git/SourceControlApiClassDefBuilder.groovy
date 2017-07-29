package com.fsryan.gradle.autosemver.git

import static com.fsryan.gradle.autosemver.git.StringUtil.indent

class SourceControlApiClassDefBuilder {

    static final String DEFAULT_SOURCE_CONTROL_API_CLASS_NAME = "MockSourceControlApi"
    static final String DEFAULT_CURRENT_BRANCH = "currentBranch"

    String className
    String currentBranch
    boolean checkoutNewBranchStatus
    boolean commitStatus
    boolean pushStatus

    Exception currentBranchException
    Exception checkoutNewBranchException
    Exception commitException
    Exception pushException

    static SourceControlApiClassDefBuilder forFailure() {
        return forFailure(DEFAULT_SOURCE_CONTROL_API_CLASS_NAME, DEFAULT_CURRENT_BRANCH)
    }

    static SourceControlApiClassDefBuilder forFailure(String className, String currentBranch) {
        return new SourceControlApiClassDefBuilder()
                .className(className)
                .currentBranch(currentBranch)
    }

    static SourceControlApiClassDefBuilder successfulSCAPI() {
        return successfulSCAPI(DEFAULT_SOURCE_CONTROL_API_CLASS_NAME, DEFAULT_CURRENT_BRANCH)
    }

    static SourceControlApiClassDefBuilder successfulSCAPI(String className, String currentBranch) {
        return new SourceControlApiClassDefBuilder()
                .className(className)
                .pushStatus(true)
                .commitStatus(true)
                .checkoutNewBranchStatus(true)
                .currentBranch(currentBranch)
    }

    SourceControlApiClassDefBuilder className(String className) {
        this.className = className
        return this
    }

    SourceControlApiClassDefBuilder currentBranch(String currentBranch) {
        this.currentBranch = currentBranch
        return this
    }

    SourceControlApiClassDefBuilder checkoutNewBranchStatus(boolean checkoutNewBranchStatus) {
        this.checkoutNewBranchStatus = checkoutNewBranchStatus
        return this
    }

    SourceControlApiClassDefBuilder commitStatus(boolean commitStatus) {
        this.commitStatus = commitStatus
        return this
    }

    SourceControlApiClassDefBuilder pushStatus(boolean pushStatus) {
        this.pushStatus = pushStatus
        return this
    }

    SourceControlApiClassDefBuilder currentBranchException(Exception currentBranchException) {
        this.currentBranchException = currentBranchException
        return this
    }

    SourceControlApiClassDefBuilder checkoutNewBranchException(Exception checkoutNewBranchException) {
        this.checkoutNewBranchException = checkoutNewBranchException
        return this
    }

    SourceControlApiClassDefBuilder commitException(Exception commitException) {
        this.commitException = commitException
        return this
    }

    SourceControlApiClassDefBuilder pushException(Exception pushException) {
        this.pushException = pushException
        return this
    }

    String build() {
        return """
${indent(1)}class ${className ?: DEFAULT_SOURCE_CONTROL_API_CLASS_NAME} extends ${GitApi.class.name} {
${indent(2)}String currentBranch() {
${indent(3)}${returnOrThrowLine(currentBranch, currentBranchException)}
${indent(2)}}
${indent(2)}boolean checkoutNewBranch(String branchName) {
${indent(3)}${returnOrThrowLine(checkoutNewBranchStatus, checkoutNewBranchException)}
${indent(2)}}
${indent(2)}boolean commit(String filename, String message) {
${indent(3)}${returnOrThrowLine(commitStatus, commitException)}
${indent(2)}}
${indent(2)}boolean push(String remote, String branchName) {
${indent(3)}${returnOrThrowLine(pushStatus, pushException)}
${indent(2)}}
${indent(1)}}
"""
    }

    private static String returnOrThrowLine(boolean status, Exception e) {
        if (e != null) {
            return "throw new ${e.getClass().name}('${e.message}')"
        }
        return "return $status"
    }

    private static String returnOrThrowLine(String string, Exception e) {
        if (e != null) {
            return "throw new ${e.getClass().name}('${e.message}')"
        }
        return "return '$string'"
    }
}
