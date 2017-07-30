package com.fsryan.gradle.autosemver.git

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AutoSemVerGitPluginTest extends Specification {

    static String PLUGIN_PART = """
            plugins {
                id 'autosemver-git'
            }
        """

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    File versionLockFile
    File buildFile
    List<File> pluginClasspath

    def setup() {
        versionLockFile = testProjectDir.newFile('version.lock')
        buildFile = testProjectDir.newFile('build.gradle')

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def "bumpVersion dependent upon bumpVersionLocally"() {
        given:
        versionLockFile << '1.4.5-preRelease+metaData'
        buildFile << PLUGIN_PART

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('bumpVersion')
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        result.output.contains('bumpVersion will do nothing')
        result.output.contains('bumpVersionLocally will do nothing')
        result.task(":bumpVersion").outcome == SUCCESS
        result.task(":bumpVersionLocally").outcome == SUCCESS
    }
}