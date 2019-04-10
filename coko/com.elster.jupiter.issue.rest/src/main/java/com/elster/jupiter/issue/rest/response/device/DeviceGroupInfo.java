package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceGroupInfo {

    public long id;
    public String mRID;
    public String name;
    public long version;
    public boolean dynamic;
    public String filter;
    public List<Long> devices;

    public DeviceGroupInfo(){ this.devices = new ArrayList<>();}

    public DeviceGroupInfo(EndDeviceGroup endDeviceGroup){
        if (endDeviceGroup != null) {
            this.id = endDeviceGroup.getId();
            this.mRID = endDeviceGroup.getMRID();
            this.name = endDeviceGroup.getName();
            this.dynamic = endDeviceGroup.isDynamic();
            this.version = endDeviceGroup.getVersion();
            this.devices = new ArrayList<>();
            endDeviceGroup.getMembers(Instant.now()).stream().forEach(dev -> devices.add(dev.getId()));
        }
    }
}
