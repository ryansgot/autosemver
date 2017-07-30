package com.fsryan.gradle.autosemver.git

class StringUtil {

    static String repeat(String str, int times) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < times; i++) {
            buf.append(str)
        }
        return buf.toString()
    }

    static String indent(int times) {
        return repeat("    ", times)
    }

    static String surround(String str, String with) {
        return with + str + with
    }
}
