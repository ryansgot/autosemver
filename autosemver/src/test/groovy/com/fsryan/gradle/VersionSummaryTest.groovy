package com.fsryan.gradle

import com.fsryan.gradle.autosemver.InvalidVersionException
import com.fsryan.gradle.autosemver.VersionSummary
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static org.junit.Assert.assertEquals

abstract class VersionSummaryTest {

    @RunWith(Parameterized.class)
    static class SuccessCases {

        private final String input
        private final String expectedVersion
        private final int expectedMajor
        private final int expectedMinor
        private final int expectedPatch
        private final String expectedPreRelease
        private final String expectedMetaData
        private final boolean expectedStable
        private final boolean expectedIsPreRelease
        private final boolean expectedHasMetaData

        private VersionSummary versionSummaryUnderTest

        SuccessCases(String input,
                     String expectedVersion,
                     int expectedMajor,
                     int expectedMinor,
                     int expectedPatch,
                     String expectedPreRelease,
                     String expectedMetaData,
                     boolean expectedStable,
                     boolean expectedIsPreRelease,
                     boolean expectedHasMetaData) {
            this.input = input
            this.expectedVersion = expectedVersion
            this.expectedMajor = expectedMajor
            this.expectedMinor = expectedMinor
            this.expectedPatch = expectedPatch
            this.expectedPreRelease = expectedPreRelease
            this.expectedMetaData = expectedMetaData
            this.expectedStable = expectedStable
            this.expectedIsPreRelease = expectedIsPreRelease
            this.expectedHasMetaData = expectedHasMetaData
        }

        @Parameterized.Parameters
        static Collection<Object[]> data() {
            Object[][] data = new Object[7][10]
            data[0] = ["0.0.0", "0.0.0", 0, 0, 0, null, null, false, false, false]                                                          // all zeros correctly parsed
            data[1] = ["1.1.1", "1.1.1", 1, 1, 1, null, null, true, false, false]                                                           // all ones correctly parsed
            data[2] = ["01.01.01", "1.1.1", 1, 1, 1, null, null, true, false, false]                                                        // leading zeros removed from major, minor, and patch versions
            data[3] = ["999.999.999", "999.999.999", 999, 999, 999, null, null, true, false, false]                                         // as much as 999 for any major/minor/patch version
            data[4] = ["1.2.3-alpha", "1.2.3-alpha", 1, 2, 3, "alpha", null, true, true, false]                                             // should correctly parse pre release version when no meta data
            data[5] = ["1.0.0+20130313144700", "1.0.0+20130313144700", 1, 0, 0, null, "20130313144700", true, false, true]                  // should correctly parse meta data when no pre release version
            data[6] = ["1.0.0-beta+exp.sha.5114f85", "1.0.0-beta+exp.sha.5114f85", 1, 0, 0, "beta", "exp.sha.5114f85", true, true, true]    // should correctly parse pre release version and meta data when both present
            return data
        }

        @Before
        void initVersionSummary() {
            versionSummaryUnderTest = new VersionSummary(input)
        }

        @Test
        void shouldOutputCorrectVersionString() {
            assertEquals(expectedVersion, versionSummaryUnderTest.toString())
        }

        @Test
        void shouldOutputCorrectMajorVersion() {
            assertEquals(expectedMajor, versionSummaryUnderTest.getMajor())
        }

        @Test
        void shouldOutputCorrectMinorVersion() {
            assertEquals(expectedMinor, versionSummaryUnderTest.getMinor())
        }

        @Test
        void shouldOutputCorrectPatchVersion() {
            assertEquals(expectedPatch, versionSummaryUnderTest.getPatch())
        }

        @Test
        void shouldOutputCorectPreReelaseVersion() {
            assertEquals(expectedPreRelease, versionSummaryUnderTest.getPreRelease())
        }

        @Test
        void shouldOutputCorrectMetaData() {
            assertEquals(expectedMetaData, versionSummaryUnderTest.getMetaData())
        }

        @Test
        void shouldOutputCorrectStableValue() {
            assertEquals(expectedStable, versionSummaryUnderTest.isStable())
        }

        @Test
        void shouldOutputCorrectIsPreReleaseValue() {
            assertEquals(expectedIsPreRelease, versionSummaryUnderTest.isPreRelease())
        }

        @Test
        void shouldOutputCorrectHasMetaDataValue() {
            assertEquals(expectedHasMetaData, versionSummaryUnderTest.hasMetaData())
        }
    }

    @RunWith(Parameterized.class)
    static class ErrorCases {

        private final String input

        ErrorCases(String input) {
            this.input = input
        }

        @Parameterized.Parameters
        static Collection<Object[]> data() {
            def data = ['', '1', '0.', '.0.0', 'a.0.0', '0.a.0', '0.0.a', '1.0.0-', '1.0.0-a+', '1.0.0+md-prerelease', '1.0.0-alpha,', '1.0.0-alpha+,']
            return data.collect { [it] as Object[] }
        }

        @Test(expected = InvalidVersionException.class)
        void shouldThrowOnInvalidInput() {
            new VersionSummary(input)
        }
    }
}
