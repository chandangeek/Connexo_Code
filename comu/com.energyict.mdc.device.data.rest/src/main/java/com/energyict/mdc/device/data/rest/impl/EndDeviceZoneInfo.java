package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndDeviceZoneInfo {

    public long id;
    public String zoneTypeName;
    public long zoneTypeId;
    public String zoneName;
    public long zoneId;

    public EndDeviceZoneInfo() {
    }

    public EndDeviceZoneInfo(String zoneTypeName, String zoneName, long id, long zoneTypeId, long zoneId) {
        this.zoneTypeName = zoneTypeName;
        this.zoneName = zoneName;
        this.id = id;
        this.zoneTypeId = zoneTypeId;
        this.zoneId = zoneId;
    }
}
