/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import javax.inject.Inject;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class DeviceDataQualityKpiBuilder implements Builder<DeviceDataQualityKpi> {

    private final DataQualityKpiService dataQualityKpiService;

    private EndDeviceGroup endDeviceGroup;
    private TemporalAmount frequency;

    @Inject
    public DeviceDataQualityKpiBuilder(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    public DeviceDataQualityKpiBuilder withEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
        return this;
    }

    public DeviceDataQualityKpiBuilder withFrequency(TemporalAmount frequency) {
        this.frequency = frequency;
        return this;
    }

    @Override
    public Optional<DeviceDataQualityKpi> find() {
        return this.dataQualityKpiService.deviceDataQualityKpiFinder()
                .forGroup(this.endDeviceGroup)
                .find().stream().findFirst();
    }

    @Override
    public DeviceDataQualityKpi create() {
        return this.dataQualityKpiService.newDataQualityKpi(this.endDeviceGroup, this.frequency);
    }
}
