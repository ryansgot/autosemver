package com.fsryan.gradle.autosemver.git

import com.fsryan.gradle.autosemver.AutoSemVerExt

import static com.fsryan.gradle.autosemver.git.StringUtil.indent

class AutoSemVerExtDefBuilder {

    List<String> branchConfigDefs = new ArrayList<>()

    static AutoSemVerExtDefBuilder ext() {
        return new AutoSemVerExtDefBuilder()
    }

    AutoSemVerExtDefBuilder addBranchConfig(BranchConfigDefBuilder builder) {
        if (builder != null) {
            branchConfigDefs.add(builder.build(2))
        }
        return this
    }

    String build() {
        StringBuilder buf = new StringBuilder(indent(1)).append(AutoSemVerExt.NAME).append(" {\n")
        for (String branchConfigDef : branchConfigDefs) {
            buf.append(branchConfigDef).append("\n")
        }
        return buf.append(indent(1)).append("}").append("\n").toString()
    }
}
