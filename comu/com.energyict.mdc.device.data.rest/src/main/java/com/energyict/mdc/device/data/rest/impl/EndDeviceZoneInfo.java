package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndDeviceZoneInfo {

    public long id;
    public String zoneTypeName;
    public long zoneTypeId;
    public String zoneName;
    public long zoneId;
}
