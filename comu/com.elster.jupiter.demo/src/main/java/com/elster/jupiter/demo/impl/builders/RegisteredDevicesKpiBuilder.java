/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import javax.inject.Inject;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class RegisteredDevicesKpiBuilder implements Builder<RegisteredDevicesKpi> {
    private final RegisteredDevicesKpiService registeredDevicesKpiService;

    private EndDeviceGroup group;
    private TemporalAmount frequency;
    private long target;

    @Inject
    public RegisteredDevicesKpiBuilder(RegisteredDevicesKpiService registeredDevicesKpiService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
    }

    @Override
    public Optional<RegisteredDevicesKpi> find() {
        return registeredDevicesKpiService.findRegisteredDevicesKpi(this.group);
    }

    @Override
    public RegisteredDevicesKpi create() {
        Log.write(this);
        RegisteredDevicesKpiService.RegisteredDevicesKpiBuilder kpiBuilder = registeredDevicesKpiService.newRegisteredDevicesKpi(group);
        kpiBuilder.frequency(frequency);
        kpiBuilder.target(target);
        RegisteredDevicesKpi kpi = kpiBuilder.save();
        return kpi;
    }

    public RegisteredDevicesKpiBuilder withGroup(EndDeviceGroup group) {
        this.group = group;
        return this;
    }

    public RegisteredDevicesKpiBuilder withFrequency(TemporalAmount frequency) {
        this.frequency = frequency;
        return this;
    }

    public RegisteredDevicesKpiBuilder withTarget(long target) {
        this.target = target;
        return this;
    }
}
