/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

public class BundleVersion {
    public String bundleName;
    public String bundleVersion;
    public String commitHash;
    public String buildTime;

    public BundleVersion(String bundleName, String bundleVersion, String commitHash, String buildTime) {
        this.bundleName = bundleName;
        this.bundleVersion = bundleVersion;
        this.commitHash = commitHash;
        this.buildTime = buildTime;
    }

    public BundleVersion() {
    }
}
