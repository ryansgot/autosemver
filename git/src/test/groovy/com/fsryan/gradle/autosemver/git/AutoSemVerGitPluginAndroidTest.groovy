package com.fsryan.gradle.autosemver.git

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.fsryan.gradle.autosemver.git.SourceControlApiClassDefBuilder.DEFAULT_SOURCE_CONTROL_API_CLASS_NAME
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AutoSemVerGitPluginAndroidTest extends Specification {

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder() {
        @Override
        protected void after() {}
    }

    File versionLockFile
    File buildFile
    File localPropertiesFile
    List<File> pluginClasspath

    def setup() {
        versionLockFile = testProjectDir.newFile('version.lock')
        buildFile = testProjectDir.newFile('build.gradle')
        localPropertiesFile = testProjectDir.newFile('local.properties')

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def "something"() {
        given:
        versionLockFile << '1.2.3-alpha+meta'
        localPropertiesFile << "sdk.dir=${System.getenv('HOME')}${File.separator}Android${File.separator}Sdk"
        buildFile << new File(getClass().getClassLoader().getResource('basic_android_build.gradle').toURI()).text

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dependencies')
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        result.output.contains('Setting android defaultConfig versionName to: 1.2.3-alpha+meta')
        result.output.contains('Setting android defaultConfig versionCode to: 1002003')
        result.task(":dependencies").outcome == SUCCESS
    }
}