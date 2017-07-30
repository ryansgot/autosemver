package com.fsryan.gradle.autosemver

import java.util.regex.Pattern

import static com.fsryan.gradle.autosemver.VersionSummary.CharCounter.countOf

class VersionSummary implements Comparable<VersionSummary> {

    private static final Pattern validVersionRegex = Pattern.compile("^((\\d+)\\.(\\d+)\\.(\\d+))" // version string
            + "(?:-([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?" // prerelease suffix (optional)
            + "(?:\\+([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?" // build suffix (optional)
            + '$')

    int major = 0
    int minor = 0
    int patch = 0
    String preRelease
    String metaData

    VersionSummary(String value) {
        if (!validVersionRegex.matcher(value).matches() || countOf('-' as char).inString(value) > 1 || countOf('+' as char).inString(value) > 1) {
            throw new InvalidVersionException(value)
        }

        final String v = value

        // major version
        int dotIdx = v.indexOf('.')
        major = Integer.parseInt(v.substring(0, dotIdx))
        v = v.substring(dotIdx + 1)

        // minor version
        dotIdx = v.indexOf('.')
        minor = Integer.parseInt(v.substring(0, dotIdx))
        v = v.substring(dotIdx + 1)

        // patch version
        int dashIdx = v.indexOf('-')
        int plusIdx = v.indexOf('+')
        if (dashIdx > 0 && plusIdx > 0 && plusIdx < dashIdx) {
            throw new InvalidVersionException(value)
        }

        String currentPart = dashIdx > 0 ? v.substring(0, dashIdx) : plusIdx > 0 ? v.substring(0, plusIdx) : v
        patch = Integer.parseInt(currentPart)

        // pre release version
        if (dashIdx > 0) {
            v = v.substring(dashIdx + 1)
            plusIdx = v.indexOf('+')
            preRelease = plusIdx > 0 ? v.substring(0, plusIdx) : v
        }

        // meta data
        if (plusIdx > 0) {
            metaData = v.substring(plusIdx + 1)
        }

        if (major > 999 || minor > 999 || patch > 999) {
            throw new VersionPartTooHighException(toString())
        }
    }

    @Override
    String toString() {
        return major + "." + minor + "." + patch  + (isPreRelease() ? "-" + preRelease : "") + (hasMetaData() ? "+" + metaData : "")
    }

    int toVersionCode() {
        return 1_000_000 * major + 1_000 * minor + patch
    }

    void increment(String versionIncrement) {
        if ("patch".equalsIgnoreCase(versionIncrement)) {
            patch++
        } else if ("minor".equalsIgnoreCase(versionIncrement)) {
            minor++
        } else if ("major".equalsIgnoreCase(versionIncrement)) {
            major++
        } else {
            throw new IllegalArgumentException("Version part not found: $versionIncrement")
        }

        if (major > 999 || minor > 999 || patch > 999) {
            throw new VersionPartTooHighException(toString())
        }
    }

    boolean isStable() {
        return major > 0
    }

    boolean isPreRelease() {
        return preRelease != null
    }

    boolean hasMetaData() {
        return metaData != null
    }

    @Override
    int compareTo(VersionSummary other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare with null")
        }
        if (major != other.major) {
            return major - other.major
        }
        if (minor != other.minor) {
            return minor - other.minor
        }
        if (patch != other.patch) {
            return patch - other.patch
        }
        if (isPreRelease() ^ other.isPreRelease()) {
            return isPreRelease() ? -1 : 1
        }
        if (!isPreRelease()) {
            return 0
        }

        // matching major.minor.patch versions and both are prereleases
        // numerically/lexically compare by prerelease divsion
        // finally, compare by number of divisions
        String[] splitPreRelease = preRelease.split("\\.")
        String[] otherSplitPreRelease = other.preRelease.split("\\.")
        final int minLength = Math.min(splitPreRelease.length, otherSplitPreRelease.length)
        for (int i = 0; i < minLength; i++) {
            try {
                int val = Integer.parseInt(splitPreRelease[i])
                int otherVal = Integer.parseInt(otherSplitPreRelease[i])
                if (val != otherVal) {
                    return val - otherVal
                }
            } catch (NumberFormatException nfe) {
                int stringComparison = splitPreRelease[i].compareTo(otherSplitPreRelease[i])
                if (stringComparison != 0) {
                    return stringComparison
                }
            }
        }
        return splitPreRelease.length - otherSplitPreRelease.length
    }

    /**
     * <p>
     *   Utility class for counting occurrences of a character in a a string.
     *   Will not work for characters that must be escaped in regex
     * </p>
     */
    private static class CharCounter {

        private final char c

        CharCounter(char c) {
            this.c = c
        }

        static CharCounter countOf(char c) {
            return new CharCounter(c)
        }

        int inString(String str) {
            return str.replaceAll("[^" + c + "]", "").length()
        }
    }
}

class InvalidVersionException extends RuntimeException {
    InvalidVersionException(String value) {
        super("$value is an invalid semantic version string")
    }
}

class VersionPartTooHighException extends RuntimeException {
    VersionPartTooHighException(String value) {
        super("$value contains a part that is too large")
    }
}