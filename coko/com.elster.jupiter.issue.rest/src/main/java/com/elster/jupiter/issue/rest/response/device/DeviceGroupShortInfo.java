package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

public class DeviceGroupShortInfo {
    public long id;
    public String name;

    public DeviceGroupShortInfo(EndDeviceGroup devgroup) {
        if (devgroup != null) {
            this.id = devgroup.getId();
            this.name = devgroup.getName();
        }
    }
}