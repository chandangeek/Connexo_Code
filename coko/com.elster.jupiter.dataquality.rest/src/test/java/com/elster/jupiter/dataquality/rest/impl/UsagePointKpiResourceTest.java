/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static com.elster.jupiter.dataquality.DataQualityKpiService.UsagePointDataQualityKpiFinder;
import static com.elster.jupiter.dataquality.rest.impl.DataQualityKpiInfo.UsagePointDataQualityKpiInfo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointKpiResourceTest extends DataQualityApplicationJerseyTest {

    private static final Long ID = 13L;
    private static final Long VERSION = 14L;
    private static final Long GROUP_ID = 16L;
    private static final String GROUP_NAME = "G";
    private static final Long METROLOGY_PURPOSE_ID = 17L;
    private static final String METROLOGY_PURPOSE_NAME = "Billing";
    private static final TemporalAmount FREQUENCY = Period.ofDays(1);
    private static final Instant LATEST_CALCULATION = Instant.now();

    @Test
    public void getUsagePointKpis() {
        UsagePointDataQualityKpiFinder finder = mock(UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        UsagePointGroup usagePointGroup = mockUsagePointGroup(GROUP_ID, GROUP_NAME);
        MetrologyPurpose metrologyPurpose = mockMetrologyPurpose(METROLOGY_PURPOSE_ID, METROLOGY_PURPOSE_NAME);
        UsagePointDataQualityKpi kpi = mockUsagePointDataQualityKpi(ID, VERSION, FREQUENCY, usagePointGroup, metrologyPurpose, LATEST_CALCULATION);
        when(finder.find()).thenReturn(Collections.singletonList(kpi));

        // Business method
        String response = target("/usagePointKpis").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.usagePointKpis[0].id")).isEqualTo(ID.intValue());
        assertThat(jsonModel.<Number>get("$.usagePointKpis[0].version")).isEqualTo(VERSION.intValue());
        assertThat(jsonModel.<Number>get("$.usagePointKpis[0].usagePointGroup.id")).isEqualTo(GROUP_ID.intValue());
        assertThat(jsonModel.<String>get("$.usagePointKpis[0].usagePointGroup.name")).isEqualTo(GROUP_NAME);
        assertThat(jsonModel.<Number>get("$.usagePointKpis[0].metrologyPurpose.id")).isEqualTo(METROLOGY_PURPOSE_ID.intValue());
        assertThat(jsonModel.<String>get("$.usagePointKpis[0].metrologyPurpose.name")).isEqualTo(METROLOGY_PURPOSE_NAME);
        assertThat(jsonModel.<Number>get("$.usagePointKpis[0].latestCalculationDate")).isEqualTo(LATEST_CALCULATION.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.usagePointKpis[0].frequency.every.count")).isEqualTo(Long.valueOf(FREQUENCY.get(ChronoUnit.DAYS)).intValue());
        assertThat(jsonModel.<String>get("$.usagePointKpis[0].frequency.every.timeUnit")).isEqualTo("days");
    }

    @Test
    public void getUsagePointKpisPaginationAndSorting() {
        UsagePointDataQualityKpiFinder finder = mock(UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);

        UsagePointGroup group_A = mockUsagePointGroup(1, "GA");
        UsagePointGroup group_B = mockUsagePointGroup(2, "GB");

        MetrologyPurpose purpose_A = mockMetrologyPurpose(1, "PA");
        MetrologyPurpose purpose_B = mockMetrologyPurpose(2, "PB");
        MetrologyPurpose purpose_C = mockMetrologyPurpose(3, "PC");

        UsagePointDataQualityKpi kpi_AA = mockUsagePointDataQualityKpi(1, VERSION, FREQUENCY, group_A, purpose_A, null);
        UsagePointDataQualityKpi kpi_AB = mockUsagePointDataQualityKpi(2, VERSION, FREQUENCY, group_A, purpose_B, null);
        UsagePointDataQualityKpi kpi_AC = mockUsagePointDataQualityKpi(3, VERSION, FREQUENCY, group_A, purpose_C, null);
        UsagePointDataQualityKpi kpi_BA = mockUsagePointDataQualityKpi(4, VERSION, FREQUENCY, group_B, purpose_A, null);
        UsagePointDataQualityKpi kpi_BB = mockUsagePointDataQualityKpi(5, VERSION, FREQUENCY, group_B, purpose_B, null);
        UsagePointDataQualityKpi kpi_BC = mockUsagePointDataQualityKpi(6, VERSION, FREQUENCY, group_B, purpose_C, null);

        when(finder.find()).thenReturn(Arrays.asList(kpi_BC, kpi_BB, kpi_BA, kpi_AC, kpi_AB, kpi_AA));

        // Business method
        String response = target("/usagePointKpis").queryParam("start", 1).queryParam("limit", 3).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(5);
        assertThat(jsonModel.<List<Number>>get("$.usagePointKpis[*].id")).containsExactly(
                Long.valueOf(kpi_AB.getId()).intValue(),
                Long.valueOf(kpi_AC.getId()).intValue(),
                Long.valueOf(kpi_BA.getId()).intValue()
        );
    }

    @Test
    public void getUsagePointKpiById() {
        UsagePointGroup usagePointGroup = mockUsagePointGroup(GROUP_ID, GROUP_NAME);
        MetrologyPurpose metrologyPurpose = mockMetrologyPurpose(METROLOGY_PURPOSE_ID, METROLOGY_PURPOSE_NAME);
        DataQualityKpi kpi = mockUsagePointDataQualityKpi(ID, VERSION, FREQUENCY, usagePointGroup, metrologyPurpose, LATEST_CALCULATION);
        doReturn(Optional.of(kpi)).when(dataQualityKpiService).findUsagePointDataQualityKpi(ID);

        // Business method
        String response = target("/usagePointKpis/" + ID).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(ID.intValue());
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(VERSION.intValue());
        assertThat(jsonModel.<Number>get("$.usagePointGroup.id")).isEqualTo(GROUP_ID.intValue());
        assertThat(jsonModel.<String>get("$.usagePointGroup.name")).isEqualTo(GROUP_NAME);
        assertThat(jsonModel.<Number>get("$.metrologyPurpose.id")).isEqualTo(METROLOGY_PURPOSE_ID.intValue());
        assertThat(jsonModel.<String>get("$.metrologyPurpose.name")).isEqualTo(METROLOGY_PURPOSE_NAME);
        assertThat(jsonModel.<Number>get("$.latestCalculationDate")).isEqualTo(LATEST_CALCULATION.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.frequency.every.count")).isEqualTo(Long.valueOf(FREQUENCY.get(ChronoUnit.DAYS)).intValue());
        assertThat(jsonModel.<String>get("$.frequency.every.timeUnit")).isEqualTo("days");
    }

    @Test
    public void getUsagePointKpiByIdNotFound() {
        doReturn(Optional.empty()).when(dataQualityKpiService).findUsagePointDataQualityKpi(ID);

        // Business method
        Response response = target("/usagePointKpis/" + ID).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteUsagePointKpi() {
        UsagePointDataQualityKpi dataQualityKpi = mock(UsagePointDataQualityKpi.class);
        doReturn(Optional.of(dataQualityKpi)).when(dataQualityKpiService).findAndLockDataQualityKpiByIdAndVersion(ID, VERSION);
        UsagePointDataQualityKpiInfo info = new UsagePointDataQualityKpiInfo();
        info.id = ID;
        info.version = VERSION;

        // Business method
        Response response = target("/usagePointKpis/" + ID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(dataQualityKpi).makeObsolete();
    }

    @Test
    public void deleteUsagePointKpiConcurrencyCheck() {
        UsagePointDataQualityKpi kpi = mockUsagePointDataQualityKpi(ID, VERSION, FREQUENCY,
                mockUsagePointGroup(GROUP_ID, GROUP_NAME), mockMetrologyPurpose(METROLOGY_PURPOSE_ID, METROLOGY_PURPOSE_NAME), null);
        doReturn(Optional.of(kpi)).when(dataQualityKpiService).findDataQualityKpi(ID);
        when(dataQualityKpiService.findAndLockDataQualityKpiByIdAndVersion(ID, VERSION)).thenReturn(Optional.empty());
        UsagePointDataQualityKpiInfo info = new UsagePointDataQualityKpiInfo();
        info.id = ID;
        info.version = VERSION;

        // Business method
        Response response = target("/usagePointKpis/" + ID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void createUsagePointKpi() {
        UsagePointGroup usagePointGroup = mockUsagePointGroup(GROUP_ID, GROUP_NAME);
        MetrologyPurpose billing = mockMetrologyPurpose(1, "Billing");
        MetrologyPurpose information = mockMetrologyPurpose(2, "Information");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(ID, VERSION, FREQUENCY, usagePointGroup, billing, null);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(ID, VERSION, FREQUENCY, usagePointGroup, information, null);
        when(dataQualityKpiService.newDataQualityKpi(usagePointGroup, billing, FREQUENCY)).thenReturn(kpi_1);
        when(dataQualityKpiService.newDataQualityKpi(usagePointGroup, information, FREQUENCY)).thenReturn(kpi_2);

        UsagePointDataQualityKpiInfo info = new UsagePointDataQualityKpiInfo();
        info.usagePointGroup = new LongIdWithNameInfo(GROUP_ID, GROUP_NAME);
        info.purposes = new LongIdWithNameInfo[]{new LongIdWithNameInfo(billing), new LongIdWithNameInfo(information)};
        info.frequency = new TemporalExpressionInfo();
        info.frequency.every = new TimeDurationInfo();
        info.frequency.every.count = 1;
        info.frequency.every.timeUnit = "days";

        // Business method
        Response response = target("/usagePointKpis").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(dataQualityKpiService).newDataQualityKpi(usagePointGroup, billing, FREQUENCY);
        verify(dataQualityKpiService).newDataQualityKpi(usagePointGroup, information, FREQUENCY);
    }
}
