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
            def data = [
                    '',
                    '1',
                    '0.',
                    '.0.0',
                    'a.0.0',
                    '0.a.0',
                    '0.0.a',
                    '1.0.0-',
                    '1.0.0-a+',
                    '1.0.0+md-prerelease',
                    '1.0.0-alpha,',
                    '1.0.0-alpha+,',
                    '1.0.0.1',
                    '1.0.0-alpha-beta',
                    '1.0.0+alpha+beta'
            ]
            return data.collect { [it] as Object[] }
        }

        @Test(expected = InvalidVersionException.class)
        void shouldThrowOnInvalidInput() {
            new VersionSummary(input)
        }
    }

    @RunWith(Parameterized.class)
    static class Comparison {

        private final VersionSummary vs1
        private final VersionSummary vs2
        private final int expectedComparison

        Comparison(String v1, String v2, int expectedComparison) {
            vs1 = new VersionSummary(v1)
            vs2 = new VersionSummary(v2)
            this.expectedComparison = expectedComparison
        }

        @Parameterized.Parameters
        static Collection<Object[]> data() {
            Object[][] data = new Object[12][3]
            data[0] = ["1.2.3", "1.2.3", 0]                     // equal versions with no pre release should compare to 0
            data[1] = ["1.2.3", "1.2.3+some.meta.data", 0]      // meta data should have no effect on comparison
            data[2] = ["1.2.3-alpha", "1.2.3-alpha", 0]         // major.minor.patch-preRelease all equal should be equal
            data[3] = ["1.2.3", "1.2.3-alpha", 1]               // equal major.minor.patch where one has pre release should have non-pre-release precede
            data[4] = ["1.2.3-beta", "1.2.3-alpha", 1]          // equal major.minor.patch with unequal alpha pre release should be calculated lexically
            data[5] = ["1.2.3-1", "1.2.3-2", -1]                // equal major.minor.patch with unequal numeric pre release should be calculated numerically
            data[6] = ["1.2.3-rc.beta", "1.2.3-rc.alpha", 1]    // equal major.minor.patch with unequal alpha pre release should be calculated lexically (multiple divisions)
            data[7] = ["1.2.3-1.1", "1.2.3-1.2", -1]            // equal major.minor.patch with unequal numeric pre release should be calculated numerically (multiple divisions)
            data[8] = ["1.2.3-1.1.1", "1.2.3-1.1", 1]           // equal major.minor.patch with unequal length pre release should give precedence to longer
            data[9] = ["1.2.3", "1.2.4", -1]                    // larger patch version should get precedence
            data[10] = ["1.2.0", "1.3.0", -1]                   // larger minor version should get precedence
            data[11] = ["1.0.0", "2.0.0", -1]                   // larger major version should get precedence
            return data
        }

        @Test
        void shouldCalculateCompareToValueCorrectly() {
            assertEquals(expectedComparison, vs1 <=> vs2)
        }

        @Test
        void shouldCalculateReverseCompareToValueCorrectly() {
            assertEquals(expectedComparison * -1, vs2 <=> vs1)
        }
    }
}
