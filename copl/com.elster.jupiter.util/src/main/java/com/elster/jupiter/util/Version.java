/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Version implements Comparable<Version> {
    public static final Version EMPTY = new Version(new String[0]);
    public static final String DEFAULT_SEPARATOR = ".";
    public static final String DEFAULT_SEPARATOR_PATTERN = "\\.";

    private Object[] version;

    private Version(Object[] parts) {
        version = parts;
    }

    public static Version fromParts(int... parts) {
        Object[] version = new Object[parts.length];
        System.arraycopy(parts, 0, version, 0, parts.length);
        return new Version(version);
    }

    public static Version fromParts(String... parts) {
        return new Version(Arrays.stream(parts)
                .map(Version::tryParseInteger)
                .toArray());
    }

    public static Version fromString(String version, String separator) {
        return fromParts(version.split(separator));
    }

    public static Version fromString(String version) {
        return fromString(version, DEFAULT_SEPARATOR_PATTERN);
    }

    private static Object tryParseInteger(String arg) {
        try {
            return Integer.valueOf(arg);
        } catch (NumberFormatException e) {
            return arg;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof Version
                && Arrays.equals(version, ((Version) obj).version);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(version);
    }

    @Override
    public int compareTo(Version other) {
        int minSize = Math.min(version.length, other.version.length);
        for (int i = 0; i < minSize; ++i) {
            Object local = version[i];
            Object remote = other.version[i];
            if (local instanceof Integer) {
                if (remote instanceof Integer) {
                    int currentResult = ((Integer) local).compareTo((Integer) remote);
                    if (currentResult != 0) {
                        return currentResult;
                    }
                } else {
                    return 1; // string qualifier has lower priority
                }
            } else {
                if (remote instanceof Integer) {
                    return -1; // string qualifier has lower priority
                } else {
                    int currentResult = ((String) local).compareTo((String) remote);
                    if (currentResult != 0) {
                        return currentResult;
                    }
                }
            }
        }
        return version.length > minSize ? 1 : // one more subversion
                other.version.length > minSize ? -1 : // one more subversion
                        0; // fully equal
    }

    public String toString(String separator) {
        return Arrays.stream(version)
                .map(Object::toString)
                .collect(Collectors.joining(separator));
    }

    @Override
    public String toString() {
        return toString(DEFAULT_SEPARATOR);
    }
}
