/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceGroupInfo {

    public long id;
    public String name;
    public List<Long> devices;

    public DeviceGroupInfo(EndDeviceGroup endDeviceGroup){
        if (endDeviceGroup != null) {
            this.id = endDeviceGroup.getId();
            this.name = endDeviceGroup.getName();
            this.devices = new ArrayList<>();
            endDeviceGroup.getMembers(Instant.now()).stream().forEach(dev -> devices.add(dev.getId()));
        }
    }
}
