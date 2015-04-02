package com.energyict.mdc.device.lifecycle.config.rest;

import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.response.DeviceLifeCycleInfo;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceLifeCycleResourceTest extends DeviceLifeCycleConfigApplicationJerseyTest {

    @Test
    public void testDeviceLifeCycleJsonModel(){
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        Finder<DeviceLifeCycle> finder = mock(Finder.class);
        when(finder.from(Matchers.any(QueryParameters.class))).thenReturn(finder);
        when(finder.stream()).thenReturn(Collections.singletonList(dlc).stream());
        when(deviceLifeCycleConfigurationService.findAllDeviceLifeCycles()).thenReturn(finder);

        String stringResponse = target("/devicelifecycles").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).hasSize(1);
        assertThat(model.<Number>get("$.deviceLifeCycles[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycles[0].name")).isEqualTo("Standard");
    }

    @Test
    public void testEmptyDeviceLifeCycleList(){
        Finder<DeviceLifeCycle> finder = mock(Finder.class);
        when(finder.from(Matchers.any(QueryParameters.class))).thenReturn(finder);
        when(finder.stream()).thenReturn(Collections.<DeviceLifeCycle>emptyList().stream());
        when(deviceLifeCycleConfigurationService.findAllDeviceLifeCycles()).thenReturn(finder);

        String stringResponse = target("/devicelifecycles").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).isEmpty();
    }

    @Test
    public void testGetDeviceLifeCycleById(){
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("Standard");
    }

    @Test
    public void testGetUnexistedDeviceLifeCycle(){
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.empty());

        Response response = target("/devicelifecycles/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testAddNewDeviceLifeCycle(){
        DeviceLifeCycle lifeCycle = mockSimpleDeviceLifeCycle(1L, "New life cycle");
        when(deviceLifeCycleConfigurationService.newDefaultDeviceLifeCycle(Matchers.anyString())).thenReturn(lifeCycle);

        DeviceLifeCycleInfo newLifeCycle = new DeviceLifeCycleInfo();
        newLifeCycle.name = "New life cycle";
        Response response = target("/devicelifecycles").request().post(Entity.json(newLifeCycle));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCloneDeviceLifeCycle(){
        DeviceLifeCycle lifeCycle = mockSimpleDeviceLifeCycle(1L, "Cloned life cycle");
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(1L)).thenReturn(Optional.of(lifeCycle));
        when(deviceLifeCycleConfigurationService.cloneDeviceLifeCycle(eq(lifeCycle), Matchers.anyString())).thenReturn(lifeCycle);

        DeviceLifeCycleInfo newLifeCycle = new DeviceLifeCycleInfo();
        newLifeCycle.id = 1L;
        newLifeCycle.name = "Cloned life cycle";
        Response response = target("/devicelifecycles/1/clone").request().post(Entity.json(newLifeCycle));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCloneUnexistDeviceLifeCycle(){
        DeviceLifeCycle lifeCycle = mockSimpleDeviceLifeCycle(1L, "Cloned life cycle");
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(1L)).thenReturn(Optional.of(lifeCycle));
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(125)).thenReturn(Optional.empty());
        when(deviceLifeCycleConfigurationService.cloneDeviceLifeCycle(eq(lifeCycle), Matchers.anyString())).thenReturn(lifeCycle);

        DeviceLifeCycleInfo newLifeCycle = new DeviceLifeCycleInfo();
        newLifeCycle.name = "Cloned life cycle";
        Response response = target("/devicelifecycles/125/clone").request().post(Entity.json(newLifeCycle));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
