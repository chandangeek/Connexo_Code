/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import static com.elster.jupiter.dataquality.DataQualityKpiService.DeviceDataQualityKpiFinder;
import static com.elster.jupiter.dataquality.rest.impl.DataQualityKpiInfo.DeviceDataQualityKpiInfo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceKpiResourceTest extends DataQualityApplicationJerseyTest {

    private static final Long ID = 13L;
    private static final Long VERSION = 14L;
    private static final Long GROUP_ID = 16L;
    private static final String GROUP_NAME = "G";
    private static final TemporalAmount FREQUENCY = Period.ofDays(1);
    private static final Instant LATEST_CALCULATION = Instant.now();

    @Test
    public void getDeviceKpis() {
        DeviceDataQualityKpiFinder finder = mock(DeviceDataQualityKpiFinder.class);
        when(dataQualityKpiService.deviceDataQualityKpiFinder()).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        EndDeviceGroup endDeviceGroup = mockDeviceGroup(GROUP_ID, GROUP_NAME);
        DeviceDataQualityKpi kpi = mockDeviceDataQualityKpi(ID, VERSION, FREQUENCY, endDeviceGroup, LATEST_CALCULATION);
        when(finder.stream()).thenReturn(Stream.of(kpi));

        // Business method
        String response = target("/deviceKpis").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List<?>>get("$.deviceKpis")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.deviceKpis[0].id")).isEqualTo(ID.intValue());
        assertThat(jsonModel.<Number>get("$.deviceKpis[0].version")).isEqualTo(VERSION.intValue());
        assertThat(jsonModel.<Number>get("$.deviceKpis[0].deviceGroup.id")).isEqualTo(GROUP_ID.intValue());
        assertThat(jsonModel.<String>get("$.deviceKpis[0].deviceGroup.name")).isEqualTo(GROUP_NAME);
        assertThat(jsonModel.<Number>get("$.deviceKpis[0].latestCalculationDate")).isEqualTo(LATEST_CALCULATION.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.deviceKpis[0].frequency.every.count")).isEqualTo(Long.valueOf(FREQUENCY.get(ChronoUnit.DAYS)).intValue());
        assertThat(jsonModel.<String>get("$.deviceKpis[0].frequency.every.timeUnit")).isEqualTo("days");
    }

    @Test
    public void getDeviceKpiById() {
        EndDeviceGroup endDeviceGroup = mockDeviceGroup(GROUP_ID, GROUP_NAME);
        DataQualityKpi kpi = mockDeviceDataQualityKpi(ID, VERSION, FREQUENCY, endDeviceGroup, LATEST_CALCULATION);
        doReturn(Optional.of(kpi)).when(dataQualityKpiService).findDeviceDataQualityKpi(ID);

        // Business method
        String response = target("/deviceKpis/" + ID).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(ID.intValue());
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(VERSION.intValue());
        assertThat(jsonModel.<Number>get("$.deviceGroup.id")).isEqualTo(GROUP_ID.intValue());
        assertThat(jsonModel.<String>get("$.deviceGroup.name")).isEqualTo(GROUP_NAME);
        assertThat(jsonModel.<Number>get("$.latestCalculationDate")).isEqualTo(LATEST_CALCULATION.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.frequency.every.count")).isEqualTo(Long.valueOf(FREQUENCY.get(ChronoUnit.DAYS)).intValue());
        assertThat(jsonModel.<String>get("$.frequency.every.timeUnit")).isEqualTo("days");
    }

    @Test
    public void getDeviceKpiByIdNotFound() {
        doReturn(Optional.empty()).when(dataQualityKpiService).findDeviceDataQualityKpi(ID);

        // Business method
        Response response = target("/deviceKpis/" + ID).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void createDeviceKpi() {
        EndDeviceGroup endDeviceGroup = mockDeviceGroup(GROUP_ID, GROUP_NAME);
        DeviceDataQualityKpi deviceDataQualityKpi = mockDeviceDataQualityKpi(ID, VERSION, FREQUENCY, endDeviceGroup, null);
        when(dataQualityKpiService.newDataQualityKpi(endDeviceGroup, FREQUENCY)).thenReturn(deviceDataQualityKpi);

        DeviceDataQualityKpiInfo info = new DeviceDataQualityKpiInfo();
        info.deviceGroup = new LongIdWithNameInfo(GROUP_ID, GROUP_NAME);
        info.frequency = new TemporalExpressionInfo();
        info.frequency.every = new TimeDurationInfo();
        info.frequency.every.count = 1;
        info.frequency.every.timeUnit = "days";

        // Business method
        Response response = target("/deviceKpis").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(dataQualityKpiService).newDataQualityKpi(endDeviceGroup, FREQUENCY);
    }

    @Test
    public void deleteDeviceKpi() {
        DeviceDataQualityKpi dataQualityKpi = mock(DeviceDataQualityKpi.class);
        doReturn(Optional.of(dataQualityKpi)).when(dataQualityKpiService).findAndLockDataQualityKpiByIdAndVersion(ID, VERSION);
        DeviceDataQualityKpiInfo info = new DeviceDataQualityKpiInfo();
        info.id = ID;
        info.version = VERSION;

        // Business method
        Response response = target("/deviceKpis/" + ID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(dataQualityKpi).makeObsolete();
    }

    @Test
    public void deleteDeviceKpiConcurrencyCheck() {
        DeviceDataQualityKpi kpi = mockDeviceDataQualityKpi(ID, VERSION, FREQUENCY, mockDeviceGroup(GROUP_ID, GROUP_NAME), null);
        doReturn(Optional.of(kpi)).when(dataQualityKpiService).findDataQualityKpi(ID);
        when(dataQualityKpiService.findAndLockDataQualityKpiByIdAndVersion(ID, VERSION)).thenReturn(Optional.empty());
        DeviceDataQualityKpiInfo info = new DeviceDataQualityKpiInfo();
        info.id = ID;
        info.version = VERSION;

        // Business method
        Response response = target("/deviceKpis/" + ID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}
