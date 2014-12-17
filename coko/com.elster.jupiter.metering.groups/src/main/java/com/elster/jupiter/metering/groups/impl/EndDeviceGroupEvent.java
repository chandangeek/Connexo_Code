package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

public class EndDeviceGroupEvent {

    private long id;
    private EndDeviceGroup endDeviceGroup;

    public EndDeviceGroupEvent(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
        this.id = endDeviceGroup.getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup;
    }

    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
    }
}
