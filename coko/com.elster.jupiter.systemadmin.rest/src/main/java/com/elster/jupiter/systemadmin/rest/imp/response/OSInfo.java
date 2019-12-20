/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.systemadmin.rest.imp.response;

import java.util.Locale;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 11/6/2019 (10:57)
 */
public class OSInfo {
    public enum OS {
        Windows,
        Unix,
        Posix_Unix,
        mac,
        Other;
        private String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
    private static OS os = OS.Other;
    static {
        try {
            String osName = System.getProperty("os.name");
            if (osName != null) {
                osName = osName.toLowerCase(Locale.ENGLISH);
                if (osName.contains("windows")) {
                    os = OS.Windows;
                } else if (osName.contains("linux")
                        || osName.contains("mpe/ix")
                        || osName.contains("freebsd")
                        || osName.contains("irix")
                        || osName.contains("digital unix")
                        || osName.contains("unix")) {
                    os = OS.Unix;
                } else if (osName.contains("mac os")) {
                    os = OS.mac;
                } else if (osName.contains("sun os")
                        || osName.contains("sunos")
                        || osName.contains("solaris")) {
                    os = OS.Posix_Unix;
                } else if (osName.contains("hp-ux")
                        || osName.contains("aix")) {
                    os = OS.Posix_Unix;
                } else {
                    os = OS.Other;
                }
            }
        } catch (Exception ex) {
            os = OS.Other;
        } finally {
            os.setVersion(System.getProperty("os.version"));
        }
    }
    public static OS getOs() {
        return os;
    }
}
