/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DataValidationKpiBuilder;
import com.elster.jupiter.validation.kpi.DataValidationKpi;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public enum DataValidationKpiTpl implements Template<DataValidationKpi, DataValidationKpiBuilder> {
    ALL_ELECTRICITY_DEVICES(DeviceGroupTpl.ALL_ELECTRICITY_DEVICES, Duration.ofHours(1L));

    private final DeviceGroupTpl deviceGroupTpl;
    private final TemporalAmount frequency;

    DataValidationKpiTpl(DeviceGroupTpl deviceGroupTpl, TemporalAmount frequency) {
        this.deviceGroupTpl = deviceGroupTpl;
        this.frequency = frequency;
    }

    @Override
    public Class<DataValidationKpiBuilder> getBuilderClass() {
        return DataValidationKpiBuilder.class;
    }

    @Override
    public DataValidationKpiBuilder get(DataValidationKpiBuilder builder) {
        return builder.withEndDeviceGroup(Builders.from(this.deviceGroupTpl).get()).withFrequency(this.frequency);
    }
}
