package com.elster.jupiter.osgi.goodies;

/**
 * Copyrights EnergyICT
 * Date: 29/09/2014
 * Time: 19:44
 */
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
