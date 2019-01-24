/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.zone;

import com.elster.jupiter.metering.zone.Zone;

import java.util.List;
import java.util.stream.Collectors;

public class ZoneInfoFactory {

    public ZoneInfo from(Zone zone) {
        ZoneInfo zoneInfo = new ZoneInfo();
        zoneInfo.id = zone.getId();
        zoneInfo.application = zone.getApplication();
        zoneInfo.name = zone.getName();
        zoneInfo.version = zone.getVersion();
        zoneInfo.zoneTypeId = zone.getZoneType().getId();
        zoneInfo.zoneTypeName = zone.getZoneType().getName();
        return zoneInfo;
    }

    public List<ZoneInfo> from(List<Zone> zones) {
        return zones.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }
}
