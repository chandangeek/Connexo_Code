/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import javax.ws.rs.core.Application;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataQualityApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    DataQualityKpiService dataQualityKpiService;
    @Mock
    MeteringGroupsService meteringGroupsService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;

    @Override
    protected Application getApplication() {
        DataQualityApplication application = new DataQualityApplication();
        application.setNlsService(nlsService);
        application.setDataQualityKpiService(dataQualityKpiService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        return application;
    }

    EndDeviceGroup mockDeviceGroup(long id, String name) {
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(id);
        when(endDeviceGroup.getName()).thenReturn(name);
        when(meteringGroupsService.findEndDeviceGroup(id)).thenReturn(Optional.of(endDeviceGroup));
        return endDeviceGroup;
    }

    UsagePointGroup mockUsagePointGroup(long id, String name) {
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(id);
        when(usagePointGroup.getName()).thenReturn(name);
        when(meteringGroupsService.findUsagePointGroup(id)).thenReturn(Optional.of(usagePointGroup));
        return usagePointGroup;
    }

    MetrologyPurpose mockMetrologyPurpose(long id, String name) {
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyPurpose.getId()).thenReturn(id);
        when(metrologyPurpose.getName()).thenReturn(name);
        when(metrologyConfigurationService.findMetrologyPurpose(id)).thenReturn(Optional.of(metrologyPurpose));
        return metrologyPurpose;
    }

    DeviceDataQualityKpi mockDeviceDataQualityKpi(long id, long version, TemporalAmount frequency, EndDeviceGroup endDeviceGroup, Instant latestCalculation) {
        DeviceDataQualityKpi deviceDataQualityKpi = mock(DeviceDataQualityKpi.class);
        when(deviceDataQualityKpi.getId()).thenReturn(id);
        when(deviceDataQualityKpi.getVersion()).thenReturn(version);
        when(deviceDataQualityKpi.getFrequency()).thenReturn(frequency);
        when(deviceDataQualityKpi.getDeviceGroup()).thenReturn(endDeviceGroup);
        when(deviceDataQualityKpi.getLatestCalculation()).thenReturn(Optional.ofNullable(latestCalculation));
        return deviceDataQualityKpi;
    }

    UsagePointDataQualityKpi mockUsagePointDataQualityKpi(long id, long version, TemporalAmount frequency, UsagePointGroup usagePointGroup, MetrologyPurpose purpose, Instant latestCalculation) {
        UsagePointDataQualityKpi usagePointDataQualityKpi = mock(UsagePointDataQualityKpi.class);
        when(usagePointDataQualityKpi.getId()).thenReturn(id);
        when(usagePointDataQualityKpi.getVersion()).thenReturn(version);
        when(usagePointDataQualityKpi.getFrequency()).thenReturn(frequency);
        when(usagePointDataQualityKpi.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(usagePointDataQualityKpi.getMetrologyPurpose()).thenReturn(purpose);
        when(usagePointDataQualityKpi.getLatestCalculation()).thenReturn(Optional.ofNullable(latestCalculation));
        return usagePointDataQualityKpi;
    }
}
