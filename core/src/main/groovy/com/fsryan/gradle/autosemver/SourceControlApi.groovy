package com.fsryan.gradle.autosemver

interface SourceControlApi {

    /**
     * @return the name of the current branch
     */
    String currentBranch()

    /**
     * @param branchName the name of the new branch to check out
     * @return true if the checkout succeeded
     */
    boolean checkoutNewBranch(String branchName)

    /**
     * @param message the commit message
     * @return true if the commit succeeded
     */
    boolean commit(String fileName, String message)

    /**
     * @param remote the remote to push to
     * @param branchName the name of the branch to push to
     * @return true if the push succeeded
     */
    boolean push(String remote, String branchName)
}