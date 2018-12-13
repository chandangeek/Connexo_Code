package com.energyict.mdc.device.lifecycle.config.rest.info;

public class TransitionEndPointConfigurationInfo {

    public long id;
    public String name;
    public long version;

    public TransitionEndPointConfigurationInfo() {
    }

    public TransitionEndPointConfigurationInfo(long id, String name, long version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }
}