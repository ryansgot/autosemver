package com.fsryan.gradle.autosemver

class BranchConfig {
    /**
     * <p>
     *   The name of the branch
     * </p>
     */
    String name
    /**
     * <p>
     *   This gets added to each commit pushed to the branch in order to tell
     *   the CI service to not create a build
     * </p>
     */
    String skipCiCommitMessageSuffix
    /**
     * <p>
     *   The names of all task dependencies of the version bump task on this
     *   branch
     * </p>
     */
    List<String> taskDependencies
    /**
     * <p>
     *   The name of the remote to push to
     * </p>
     */
    String pushRemote
    /**
     * <p>
     *   The name of the remote to pull from
     * </p>
     */
    String pullRemote
    /**
     * <p>
     *   The part of the version to increment on the version string. Right now,
     *   this can only be:
     *   <ul>
     *     <li>major</li>
     *     <li>minor</li>
     *     <li>patch</li>
     *   </ul>
     * </p>
     */
    String versionIncrement
    /**
     * <p>
     *   The pre release literal to update on the version string
     * </p>
     */
    String preRelease
    /**
     * <p>
     *   The meta data to update on the version string
     * </p>
     */
    String metaData

    BranchConfig(final String name) {
        this.name = name
    }

    @Override
    String toString() {
        StringBuilder buf = new StringBuilder(getClass().getSimpleName()).append('{')
        getClass().getDeclaredFields().each { f ->
            f.setAccessible(true)
            buf.append(f.name).append('=').append(f.get(this)).append(", ")
        }
        return buf.delete(buf.length() - 1, buf.length()).append('}').toString()
    }

    boolean incrementing() {
        return versionIncrement.equalsIgnoreCase("major") || versionIncrement.equalsIgnoreCase("minor") || versionIncrement.equalsIgnoreCase("patch")
    }

    boolean isPreRelease() {
        return preRelease != null
    }

    boolean hasMetaData() {
        return metaData != null
    }
}
