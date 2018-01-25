/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.RegisteredDevicesKpiBuilder;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public enum RegisteredDevicesKpiTpl implements Template<RegisteredDevicesKpi, RegisteredDevicesKpiBuilder> {

    BEACON_DEVICES(DeviceGroupTpl.BEACON_DEVICES, Duration.ofMinutes(15L), 95L);

    private final DeviceGroupTpl deviceGroupTpl;
    private final TemporalAmount frequency;
    private final long target;

    RegisteredDevicesKpiTpl(DeviceGroupTpl deviceGroupTpl, TemporalAmount frequency, long target) {
        this.deviceGroupTpl = deviceGroupTpl;
        this.frequency = frequency;
        this.target = target;
    }

    @Override
    public Class<RegisteredDevicesKpiBuilder> getBuilderClass() {
        return RegisteredDevicesKpiBuilder.class;
    }

    @Override
    public RegisteredDevicesKpiBuilder get(RegisteredDevicesKpiBuilder builder) {
        return builder.withGroup(Builders.from(this.deviceGroupTpl).get()).withFrequency(this.frequency).withTarget(this.target);
    }
}
