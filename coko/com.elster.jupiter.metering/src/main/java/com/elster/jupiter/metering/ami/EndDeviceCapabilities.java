/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EndDeviceCapabilities {
    private List<EndDeviceControlType> controlTypes;
    private ZoneId zoneId;
    private Map<ReadingType, Long> readingTypeOffsetMap = new HashMap<>();

    public EndDeviceCapabilities(List<ReadingType> readingTypes, List<EndDeviceControlType> controlTypes) {
        this.controlTypes = ImmutableList.copyOf(controlTypes);
        readingTypes.forEach(readingType -> readingTypeOffsetMap.put(readingType, 0L));
    }

    public EndDeviceCapabilities(List<ReadingType> readingTypes, List<EndDeviceControlType> controlTypes, ZoneId zoneId) {
        readingTypes.forEach(readingType -> readingTypeOffsetMap.put(readingType, 0L));
        this.controlTypes = ImmutableList.copyOf(controlTypes);
        this.zoneId = zoneId;
    }

    public EndDeviceCapabilities(Map<ReadingType, Long> readingTypeOffsetMap, List<EndDeviceControlType> controlTypes, ZoneId zoneId) {
        this.readingTypeOffsetMap = ImmutableMap.copyOf(readingTypeOffsetMap);
        this.controlTypes = ImmutableList.copyOf(controlTypes);
        this.zoneId = zoneId;
    }

    public List<ReadingType> getConfiguredReadingTypes() {
        return new ArrayList<>(readingTypeOffsetMap.keySet());
    }

    public Map<ReadingType, Long> getReadingTypeOffsetMap() {
        return readingTypeOffsetMap;
    }

    public List<EndDeviceControlType> getSupportedControlTypes() {
        return controlTypes;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}