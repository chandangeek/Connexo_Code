/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.ImmutableList;

import java.time.ZoneId;
import java.util.List;

public final class EndDeviceCapabilities {
    private List<ReadingType> readingTypes;
    private List<EndDeviceControlType> controlTypes;
    private ZoneId zoneId;

    public EndDeviceCapabilities(List<ReadingType> readingTypes, List<EndDeviceControlType> controlTypes) {
        this.readingTypes = ImmutableList.copyOf(readingTypes);
        this.controlTypes = ImmutableList.copyOf(controlTypes);
    }

    public EndDeviceCapabilities(List<ReadingType> readingTypes, List<EndDeviceControlType> controlTypes, ZoneId zoneId) {
        this.readingTypes = ImmutableList.copyOf(readingTypes);
        this.controlTypes = ImmutableList.copyOf(controlTypes);
        this.zoneId = zoneId;
    }

    public List<ReadingType> getConfiguredReadingTypes() {
        return readingTypes;
    }

    public List<EndDeviceControlType> getSupportedControlTypes() {
        return controlTypes;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}