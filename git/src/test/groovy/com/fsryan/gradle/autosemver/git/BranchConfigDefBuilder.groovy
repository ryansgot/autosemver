package com.fsryan.gradle.autosemver.git

import static com.fsryan.gradle.autosemver.git.StringUtil.indent
import static com.fsryan.gradle.autosemver.git.StringUtil.surround

class BranchConfigDefBuilder {

    String name
    String skipCiCommitMessageSuffix
    List<String> taskDependencies = new ArrayList<>()
    String pushRemote
    String pullRemote
    String versionIncrement
    String preRelease
    String metaData

    BranchConfigDefBuilder name(String name) {
        this.name = name
        return this
    }
    BranchConfigDefBuilder skipCiCommitMessageSuffix(String skipCiCommitMessageSuffix) {
        this.skipCiCommitMessageSuffix = skipCiCommitMessageSuffix
        return this
    }
    BranchConfigDefBuilder taskDependencies(String taskDependencies) {
        this.taskDependencies = taskDependencies
        return this
    }
    BranchConfigDefBuilder pushRemote(String pushRemote) {
        this.pushRemote = pushRemote
        return this
    }
    BranchConfigDefBuilder pullRemote(String pullRemote) {
        this.pullRemote = pullRemote
        return this
    }
    BranchConfigDefBuilder versionIncrement(String versionIncrement) {
        this.versionIncrement = versionIncrement
        return this
    }
    BranchConfigDefBuilder preRelease(String preRelease) {
        this.preRelease = preRelease
        return this
    }
    BranchConfigDefBuilder metaData(String metaData) {
        this.metaData = metaData
        return this
    }

    String build() {
        return build(2)
    }

    String build(int indents) {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("Cannot build when name null or empty")
        }
        StringBuilder buf = new StringBuilder(indent(indents)).append(name).append(" {\n")
        appendIfNoNull(buf, indents + 1, "skipCiCommitMessageSuffix", surround(skipCiCommitMessageSuffix, "'"))
        appendIfNoNull(buf, indents + 1, "pushRemote", surround(pushRemote, "'"))
        appendIfNoNull(buf, indents + 1, "pullRemote", surround(pullRemote, "'"))
        appendIfNoNull(buf, indents + 1, "versionIncrement", surround(versionIncrement, "'"))
        appendIfNoNull(buf, indents + 1, "preRelease", surround(preRelease, "'"))
        appendIfNoNull(buf, indents + 1, "metaData", surround(metaData, "'"))
        if (!taskDependencies.isEmpty()) {
            buf.append(indent(indents + 1)).append("taskDependencies = ").append(taskDependencies.toString())
        }
        buf.append(indent(indents)).append("}")
    }

    private static void appendIfNoNull(StringBuilder buf, int indents, String name, String value) {
        if (value == null) {
            return
        }
        buf.append(indent(indents)).append(name).append(" = ").append(value).append("\n")
    }
}
