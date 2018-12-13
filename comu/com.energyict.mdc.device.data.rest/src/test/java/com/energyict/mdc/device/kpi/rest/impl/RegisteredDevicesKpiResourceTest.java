/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.kpi.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.rest.impl.DeviceDataRestApplicationJerseyTest;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisteredDevicesKpiResourceTest extends DeviceDataRestApplicationJerseyTest {


    @Test
    public void getRegisteredDevicesKpis() throws Exception {
        Finder<RegisteredDevicesKpi> finder = mock(Finder.class);
        RegisteredDevicesKpi kpi = createRegisteredDevicesKpi();
        when(registeredDevicesKpiService.registeredDevicesKpiFinder()).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi));
        Response response = target("/registereddevkpis").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("kpis[0].id")).isEqualTo(1);
        assertThat(model.<Integer>get("kpis[0].version")).isEqualTo(2);
        assertThat(model.<String>get("kpis[0].deviceGroup.name")).isEqualTo("group");
        assertThat(model.<Integer>get("kpis[0].deviceGroup.id")).isEqualTo(2);
        assertThat(model.<Integer>get("kpis[0].target")).isEqualTo(95);
        assertThat(model.<Integer>get("kpis[0].frequency.every.count")).isEqualTo(5);
        assertThat(model.<String>get("kpis[0].frequency.every.timeUnit")).isEqualTo("hours");
    }

    @Test
    public void getRegisteredDevicesKpi() throws Exception {
        RegisteredDevicesKpi kpi = createRegisteredDevicesKpi();
        when(registeredDevicesKpiService.findRegisteredDevicesKpi(1)).thenReturn(Optional.of(kpi));
        Response response = target("/registereddevkpis/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("id")).isEqualTo(1);
    }

    @Test
    public void getRegisteredDevicesKpisForGraphs() throws Exception {
        RegisteredDevicesKpi kpi = createRegisteredDevicesKpi();
        when(registeredDevicesKpiService.findAllRegisteredDevicesKpis()).thenReturn(Collections.singletonList(kpi));
        Response response = target("/registereddevkpis").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("[0].id")).isEqualTo(1);
        assertThat(model.<String>get("[0].name")).isEqualTo("group");
    }

    private RegisteredDevicesKpi createRegisteredDevicesKpi() {
        RegisteredDevicesKpi kpi = mock(RegisteredDevicesKpi.class);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(2L);
        when(endDeviceGroup.getName()).thenReturn("group");
        when(kpi.getId()).thenReturn(1L);
        when(kpi.getVersion()).thenReturn(2L);
        when(kpi.getDeviceGroup()).thenReturn(endDeviceGroup);
        when(kpi.getFrequency()).thenReturn(Duration.of(5, ChronoUnit.HOURS));
        when(kpi.getTarget()).thenReturn(95L);
        when(kpi.getLatestCalculation()).thenReturn(Optional.empty());
        return kpi;
    }
}
