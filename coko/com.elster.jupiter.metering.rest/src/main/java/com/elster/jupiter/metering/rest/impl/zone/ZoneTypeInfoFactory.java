/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.zone;

import com.elster.jupiter.metering.zone.ZoneType;

import java.util.List;
import java.util.stream.Collectors;

public class ZoneTypeInfoFactory {

    public ZoneTypeInfo from(ZoneType zoneType) {
        ZoneTypeInfo zoneTypeInfo = new ZoneTypeInfo();
        zoneTypeInfo.id = zoneType.getId();
        zoneTypeInfo.application = zoneType.getApplication();
        zoneTypeInfo.name = zoneType.getName();
        zoneTypeInfo.version = zoneType.getVersion();
        return zoneTypeInfo;
    }

    public List<ZoneTypeInfo> from(List<ZoneType> zoneTypes) {
        return zoneTypes.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }
}
