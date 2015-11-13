package com.elster.jupiter.system.beans;

import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.Subsystem;

public class ComponentImpl implements Component {
    private String artifactId;
    private String name;
    private String version;
    private BundleType bundleType;
    private Subsystem subsystem;


    public ComponentImpl(String artifactId, String name, String version, BundleType bundleType, Subsystem subsystem) {
        this.artifactId = artifactId;
        this.name = name;
        this.version = version;
        this.bundleType = bundleType;
        this.subsystem = subsystem;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public BundleType getBundleType() {
        return this.bundleType;
    }

    @Override
    public Subsystem getSubsystem() {
        return this.subsystem;
    }

    @Override
    public String getArtifactId() {
        return this.artifactId;
    }
}
