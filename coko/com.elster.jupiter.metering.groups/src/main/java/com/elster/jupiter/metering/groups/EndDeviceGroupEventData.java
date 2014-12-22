package com.elster.jupiter.metering.groups;

public class EndDeviceGroupEventData {

    private long id;
    private EndDeviceGroup endDeviceGroup;

    public EndDeviceGroupEventData(EndDeviceGroup endDeviceGroup) {
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
