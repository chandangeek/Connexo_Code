/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import javax.inject.Inject;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class DataValidationKpiBuilder implements Builder<DataValidationKpi> {
    private final DataValidationKpiService dataValidationKpiService;

    private EndDeviceGroup endDeviceGroup;
    private TemporalAmount frequency;

    @Inject
    public DataValidationKpiBuilder(DataValidationKpiService dataValidationKpiService) {
        this.dataValidationKpiService = dataValidationKpiService;
    }

    public DataValidationKpiBuilder withEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
        return this;
    }

    public DataValidationKpiBuilder withFrequency(TemporalAmount frequency) {
        this.frequency = frequency;
        return this;
    }

    @Override
    public Optional<DataValidationKpi> find() {
        return this.dataValidationKpiService.findDataValidationKpi(this.endDeviceGroup);
    }

    @Override
    public DataValidationKpi create() {
        return this.dataValidationKpiService.newDataValidationKpi(this.endDeviceGroup).frequency(this.frequency).build();
    }
}
