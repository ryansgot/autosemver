package com.fsryan.gradle.autosemver

import java.util.regex.Pattern

class VersionSummary {

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
        final String v = value
        if (!validVersionRegex.matcher(value).matches()) {
            throw new InvalidVersionException(value)
        }

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
}

class InvalidVersionException extends RuntimeException {
    InvalidVersionException(String value) {
        super("$value is an invalid semantic version string")
    }
}