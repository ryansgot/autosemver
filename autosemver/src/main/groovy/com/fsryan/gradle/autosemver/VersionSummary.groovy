package com.fsryan.gradle.autosemver

import java.util.regex.Pattern

import static com.fsryan.gradle.autosemver.CharCounter.countOf

class VersionSummary implements Comparable<VersionSummary> {

    private static final Pattern validVersionRegex = Pattern.compile("^((\\d+)\\.(\\d+)\\.(\\d+))" // version string
            + "(?:-([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?" // prerelease suffix (optional)
            + "(?:\\+([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?" // build suffix (optional)
            + '$')

    final int major = 0
    final int minor = 0
    final int patch = 0
    final String preRelease
    final String metaData

    VersionSummary(String value) {
        if (!validVersionRegex.matcher(value).matches() || countOf('-').inString(value) > 1 || countOf('+').inString(value) > 1) {
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
    }

    @Override
    String toString() {
        return major + "." + minor + "." + patch  + (isPreRelease() ? "-" + preRelease : "") + (hasMetaData() ? "+" + metaData : "")
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

    private int comparePreRelease(String otherPreRelease) {
    }
}

class InvalidVersionException extends RuntimeException {
    InvalidVersionException(String value) {
        super("$value is an invalid semantic version string")
    }
}

class CharCounter {

    private final char c

    CharCounter(char c) {
        this.c = c
    }

    static CharCounter countOf(char c) {
        return new CharCounter(c)
    }

    static CharCounter countOf(String c) {
        if (c.length() != 1) {
            throw new IllegalArgumentException("can only find count of single characters")
        }
        return countOf(c.charAt(0))
    }

    int inString(String str) {
        return str.replaceAll("[^" + c + "]", "").length()
    }
}