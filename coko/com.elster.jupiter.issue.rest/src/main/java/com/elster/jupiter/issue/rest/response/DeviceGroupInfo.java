package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

public class DeviceGroupInfo {
    public long id;
    public String name;

    public DeviceGroupInfo() {
    }

    public DeviceGroupInfo(EndDeviceGroup group) {
        id = group.getId();
        name = group.getName();
    }
}
