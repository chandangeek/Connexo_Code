package com.elster.jupiter.system;

public interface Component {
    String getArtifactId();
    String getName();
    void setName(String name);
    String getVersion();
    BundleType getBundleType();
    Subsystem getSubsystem();
}
