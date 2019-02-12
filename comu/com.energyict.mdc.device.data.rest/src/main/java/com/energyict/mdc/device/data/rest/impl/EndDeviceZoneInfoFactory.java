package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.metering.zone.EndDeviceZone;

import java.util.List;
import java.util.stream.Collectors;

public class EndDeviceZoneInfoFactory {

    public EndDeviceZoneInfo from(EndDeviceZone endDeviceZone) {
        EndDeviceZoneInfo info = new EndDeviceZoneInfo();
        info.id = endDeviceZone.getId();
        info.zoneTypeName = endDeviceZone.getZone().getZoneType().getName();
        info.zoneName = endDeviceZone.getZone().getName();
        info.zoneTypeId = endDeviceZone.getZone().getZoneType().getId();
        info.zoneId = endDeviceZone.getZone().getId();
        return info;
    }

    public List<EndDeviceZoneInfo> from(List<EndDeviceZone> zones) {
        return zones.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }
}