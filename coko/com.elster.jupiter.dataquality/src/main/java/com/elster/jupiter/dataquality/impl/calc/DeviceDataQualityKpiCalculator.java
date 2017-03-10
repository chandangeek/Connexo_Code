/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.impl.DeviceDataQualityKpiImpl;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class DeviceDataQualityKpiCalculator extends AbstractDataQualityKpiCalculator {

    private final EndDeviceGroup endDeviceGroup;

    DeviceDataQualityKpiCalculator(DataQualityServiceProvider serviceProvider, DeviceDataQualityKpiImpl deviceDataQualityKpi, Logger logger) {
        super(serviceProvider, deviceDataQualityKpi, logger);
        this.endDeviceGroup = deviceDataQualityKpi.getDeviceGroup();
    }

    @Override
    QualityCodeSystem getQualityCodeSystem() {
        return QualityCodeSystem.MDC;
    }

    @Override
    DataQualityKpiSqlBuilder sqlBuilder() {
        return new DeviceDataQualityKpiSqlBuilder(this.endDeviceGroup);
    }

    @Override
    public void calculateAndStore() {
        super.calculateInTransaction();
        this.endDeviceGroup
                .getMembers(getClock().instant())
                .forEach(this::storeInTransaction);
    }

    private void storeInTransaction(EndDevice endDevice) {
        try {
            getTransactionService().run(() -> store(endDevice));
        } catch (Exception ex) {
            getTransactionService().run(() -> getLogger().log(Level.WARNING, "Failed to store data quality KPI data for device " + endDevice.getName()
                    + ". Error: " + ex.getLocalizedMessage(), ex));
        }
    }

    private void store(EndDevice endDevice) {
        if (!(endDevice instanceof Meter)) {
            return;
        }
        Meter meter = (Meter) endDevice;
        Set<Channel> channels = getEffectiveChannels(meter, super.getStart(), super.getEnd());
        super.storeForChannels(meter.getId(), DeviceDataQualityKpiImpl.kpiMemberNameSuffix(meter), channels);
    }

    private Set<Channel> getEffectiveChannels(Meter meter, Instant start, Instant end) {
        return meter.getMeterActivations(Range.openClosed(start, end))
                .stream()
                .map(MeterActivation::getChannelsContainer)
                .map(ChannelsContainer::getChannels)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
