/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import aQute.bnd.annotation.ProviderType;

import java.time.temporal.TemporalAmount;
import java.util.Optional;

@ProviderType
public interface DataQualityKpiService {

    String COMPONENTNAME = "DQK";

    DeviceDataQualityKpi newDataQualityKpi(EndDeviceGroup endDeviceGroup, TemporalAmount calculationFrequency);

    UsagePointDataQualityKpi newDataQualityKpi(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, TemporalAmount calculationFrequency);

    DeviceDataQualityKpiFinder deviceDataQualityKpiFinder();

    UsagePointDataQualityKpiFinder usagePointDataQualityKpiFinder();

    Optional<? extends DataQualityKpi> findDataQualityKpi(long id);

    Optional<DeviceDataQualityKpi> findDeviceDataQualityKpi(long id);

    Optional<UsagePointDataQualityKpi> findUsagePointDataQualityKpi(long id);

    Optional<? extends DataQualityKpi> findAndLockDataQualityKpiByIdAndVersion(long id, long version);

    @ProviderType
    interface DeviceDataQualityKpiFinder extends Finder<DeviceDataQualityKpi> {

        DeviceDataQualityKpiFinder forGroup(EndDeviceGroup endDeviceGroup);

    }

    @ProviderType
    interface UsagePointDataQualityKpiFinder extends Finder<UsagePointDataQualityKpi> {

        UsagePointDataQualityKpiFinder forGroup(UsagePointGroup usagePointGroup);

        UsagePointDataQualityKpiFinder forPurpose(MetrologyPurpose metrologyPurpose);

    }
}