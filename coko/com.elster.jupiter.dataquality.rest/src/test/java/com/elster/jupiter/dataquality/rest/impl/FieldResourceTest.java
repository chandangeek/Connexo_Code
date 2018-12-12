/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import com.jayway.jsonpath.JsonModel;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldResourceTest extends DataQualityApplicationJerseyTest {

    @Test
    public void getAvailableDeviceGroups() {
        EndDeviceGroup group_1 = mockDeviceGroup(1, "G1");
        EndDeviceGroup group_2 = mockDeviceGroup(2, "G2");
        EndDeviceGroup group_3 = mockDeviceGroup(3, "G3");
        Query<EndDeviceGroup> endDeviceGroupQuery = mock(Query.class);
        when(meteringGroupsService.getEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        when(endDeviceGroupQuery.select(any(), any())).thenReturn(Arrays.asList(group_1, group_2, group_3));
        DataQualityKpiService.DeviceDataQualityKpiFinder finder = mock(DataQualityKpiService.DeviceDataQualityKpiFinder.class);
        when(dataQualityKpiService.deviceDataQualityKpiFinder()).thenReturn(finder);
        DeviceDataQualityKpi kpi = mockDeviceDataQualityKpi(1, 1, Duration.ofDays(1), group_2, null);
        when(finder.stream()).thenReturn(Stream.of(kpi));

        // Business method
        String response = target("/fields/deviceGroups").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List<?>>get("$.deviceGroups[*].id")).containsExactly(Long.valueOf(group_1.getId()).intValue(), Long.valueOf(group_3.getId()).intValue());
        assertThat(jsonModel.<List<?>>get("$.deviceGroups[*].name")).containsExactly(group_1.getName(), group_3.getName());
    }

    @Test
    public void getAvailableUsagePointGroups() {
        MetrologyPurpose billing = mockMetrologyPurpose(1, "Billing");
        MetrologyPurpose information = mockMetrologyPurpose(2, "Information");
        when(metrologyConfigurationService.getMetrologyPurposes()).thenReturn(Arrays.asList(information, billing));

        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        UsagePointGroup group_2 = mockUsagePointGroup(2, "G2");
        UsagePointGroup group_3 = mockUsagePointGroup(3, "G3");
        Query<UsagePointGroup> usagePointGroupQuery = mock(Query.class);
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        when(usagePointGroupQuery.select(any())).thenReturn(Arrays.asList(group_3, group_2, group_1));

        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(1, 1, Duration.ofDays(1), group_1, information, null);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(2, 1, Duration.ofDays(1), group_1, billing, null);
        UsagePointDataQualityKpi kpi_3 = mockUsagePointDataQualityKpi(3, 1, Duration.ofDays(1), group_3, billing, null);
        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2, kpi_3));

        // Business method
        String response = target("/fields/usagePointGroups").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<Number>get("$.usagePointGroups[0].id")).isEqualTo(Long.valueOf(group_2.getId()).intValue());
        assertThat(jsonModel.<Number>get("$.usagePointGroups[0].name")).isEqualTo(group_2.getName());
        assertThat(jsonModel.<List<?>>get("$.usagePointGroups[0].purposes[*].id"))
                .containsExactly(Long.valueOf(billing.getId()).intValue(), Long.valueOf(information.getId()).intValue());
        assertThat(jsonModel.<List<?>>get("$.usagePointGroups[0].purposes[*].name")).containsExactly(billing.getName(), information.getName());

        assertThat(jsonModel.<Number>get("$.usagePointGroups[1].id")).isEqualTo(Long.valueOf(group_3.getId()).intValue());
        assertThat(jsonModel.<Number>get("$.usagePointGroups[1].name")).isEqualTo(group_3.getName());
        assertThat(jsonModel.<List<?>>get("$.usagePointGroups[1].purposes[*].id")).containsExactly(Long.valueOf(information.getId()).intValue());
        assertThat(jsonModel.<List<?>>get("$.usagePointGroups[1].purposes[*].name")).containsExactly(information.getName());
    }
}
