/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.utils;

import com.elster.jupiter.system.ComponentStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OsgiUtils {
    private static final Pattern ONLY_NUMBERS = Pattern.compile("[0-9]+");
    private static final Pattern OSGI_VERSION_PATTERN = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+(\\.[0-9A-Za-z_-]+)?");
    private static final Pattern DATED_SNAPSHOT = Pattern.compile("([0-9])(\\.([0-9]))?(\\.([0-9]))?\\-([0-9]{8}\\.[0-9]{6}\\-[0-9]*)");
    private static final Pattern DOTS_IN_QUALIFIER = Pattern.compile("([0-9])(\\.[0-9])?\\.([0-9A-Za-z_-]+)\\.([0-9A-Za-z_-]+)");
    private static final Pattern NEED_TO_FILL_ZEROS = Pattern.compile("([0-9])(\\.([0-9]))?(\\.([0-9A-Za-z_-]+))?");

    /**
     * Just copied from http://svn.apache.org/repos/asf/maven/shared/trunk/maven-osgi/src/main/java/org/apache/maven/shared/osgi/DefaultMaven2OsgiConverter.java
     * We use this class to convert maven bundle version to osgi version format.
     * Example: maven version = 1.0-SNAPSHOT -> osgi version: 1.0.0.SNAPSHOT
     */
    public static String getOsgiVersion(String mavenVersion) {
        String osgiVersion;
        Matcher m;
        m = OSGI_VERSION_PATTERN.matcher(mavenVersion);
        if (m.matches()) {
            return mavenVersion;
        }
        osgiVersion = mavenVersion;
        m = DATED_SNAPSHOT.matcher(osgiVersion);
        if (m.matches()) {
            String major = m.group(1);
            String minor = (m.group(3) != null) ? m.group(3) : "0";
            String service = (m.group(5) != null) ? m.group(5) : "0";
            String qualifier = m.group(6).replaceAll("-", "_").replaceAll("\\.", "_");
            osgiVersion = major + "." + minor + "." + service + "." + qualifier;
        }
        osgiVersion = osgiVersion.replaceFirst("-", "\\.");
        osgiVersion = osgiVersion.replaceAll("-", "_");
        m = OSGI_VERSION_PATTERN.matcher(osgiVersion);
        if (m.matches()) {
            return osgiVersion;
        }
        m = DOTS_IN_QUALIFIER.matcher(osgiVersion);
        if (m.matches()) {
            String s1 = m.group(1);
            String s2 = m.group(2);
            String s3 = m.group(3);
            String s4 = m.group(4);
            Matcher qualifierMatcher = ONLY_NUMBERS.matcher(s3);
            if (!qualifierMatcher.matches()) {
                osgiVersion = s1 + s2 + "." + s3 + "_" + s4;
            }
        }
        m = NEED_TO_FILL_ZEROS.matcher(osgiVersion);
        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(3);
            String service = null;
            String qualifier = m.group(5);
            if (qualifier == null) {
                osgiVersion = getVersion(major, minor, service, qualifier);
            } else {
                Matcher qualifierMatcher = ONLY_NUMBERS.matcher(qualifier);
                if (qualifierMatcher.matches()) {
                    if (minor == null) {
                        minor = qualifier;
                    } else {
                        service = qualifier;
                    }
                    osgiVersion = getVersion(major, minor, service, null);
                } else {
                    osgiVersion = getVersion(major, minor, service, qualifier);
                }
            }
        }

        m = OSGI_VERSION_PATTERN.matcher(osgiVersion);
        if (!m.matches()) {
            String major = "0";
            String minor = "0";
            String service = "0";
            String qualifier = osgiVersion.replaceAll("\\.", "_");
            osgiVersion = major + "." + minor + "." + service + "." + qualifier;
        }

        return osgiVersion;
    }

    private static String getVersion(String major, String minor, String service, String qualifier) {
        StringBuffer sb = new StringBuffer();
        sb.append(major != null ? major : "0");
        sb.append('.');
        sb.append(minor != null ? minor : "0");
        sb.append('.');
        sb.append(service != null ? service : "0");
        if (qualifier != null) {
            sb.append('.');
            sb.append(qualifier);
        }
        return sb.toString();
    }

    public static String getOsgiBundleName(String mavenGroupId, String mavenArtifactId) {
        return mavenGroupId + "." + mavenArtifactId;
    }

    public static ComponentStatus bundleStateToComponentStatus(int state) {
        switch (state) {
            case 1:
                return ComponentStatus.UNINSTALLED;
            case 2:
                return ComponentStatus.INSTALLED;
            case 4:
                return ComponentStatus.RESOLVED;
            case 8:
                return ComponentStatus.STARTING;
            case 16:
                return ComponentStatus.STOPPING;
            case 32:
                return ComponentStatus.ACTIVE;
            default:
                return null;
        }
    }
}
