/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceDataQualityKpiBuilder;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public enum DataQualityKpiTpl implements Template<DeviceDataQualityKpi, DeviceDataQualityKpiBuilder> {

    ALL_ELECTRICITY_DEVICES(DeviceGroupTpl.ALL_ELECTRICITY_DEVICES, Duration.ofHours(1L));

    private final DeviceGroupTpl deviceGroupTpl;
    private final TemporalAmount frequency;

    DataQualityKpiTpl(DeviceGroupTpl deviceGroupTpl, TemporalAmount frequency) {
        this.deviceGroupTpl = deviceGroupTpl;
        this.frequency = frequency;
    }

    @Override
    public Class<DeviceDataQualityKpiBuilder> getBuilderClass() {
        return DeviceDataQualityKpiBuilder.class;
    }

    @Override
    public DeviceDataQualityKpiBuilder get(DeviceDataQualityKpiBuilder builder) {
        return builder.withEndDeviceGroup(Builders.from(this.deviceGroupTpl).get()).withFrequency(this.frequency);
    }
}
