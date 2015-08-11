package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceStateAccessTest extends DeviceDataRestApplicationJerseyTest{


    @Override
    protected boolean disableDeviceConstraintsBasedOnDeviceState() {
        return false;
    }

    private Device mockDeviceWithState(String stateName){
        State state = mock(State.class);
        when(state.getName()).thenReturn(stateName);
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        return device;
    }

    @Test
    public void testActivateValidationForInStockState(){
        Device device = mockDeviceWithState(DefaultState.IN_STOCK.getKey());

        Response response = target("devices/1/validationrulesets/validationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findByUniqueMrid("1");
        verify(device, times(1)).getState();
    }

    @Test
    public void testActivateValidationForDecommissionedState(){
        Device device = mockDeviceWithState(DefaultState.DECOMMISSIONED.getKey());

        Response response = target("devices/1/validationrulesets/validationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findByUniqueMrid("1");
        verify(device, times(1)).getState();
    }


    @Test
    public void testActivateValidationForCustomState(){
        Device device = mockDeviceWithState("Custom state");

        Response response = target("devices/1/validationrulesets/validationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findByUniqueMrid("1");
        verify(device, times(1)).getState();
    }

    @Test
    public void testActivateEstimationForInStockState(){
        Device device = mockDeviceWithState(DefaultState.IN_STOCK.getKey());

        Response response = target("devices/1/estimationrulesets/esimationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findByUniqueMrid("1");
        verify(device, times(1)).getState();
    }

    @Test
    public void testActivateEstimationForDecommissionedState(){
        Device device = mockDeviceWithState(DefaultState.DECOMMISSIONED.getKey());

        Response response = target("devices/1/estimationrulesets/esimationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findByUniqueMrid("1");
        verify(device, times(1)).getState();
    }

    @Test
    public void testActivateEstimationForCustomState(){
        Device device = mockDeviceWithState("Custom state");

        Response response = target("devices/1/estimationrulesets/esimationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findByUniqueMrid("1");
        verify(device, times(1)).getState();
    }

    @Test
    public void testCommunicationPlaningForInStockState(){
        Device device = mockDeviceWithState(DefaultState.IN_STOCK.getKey());
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.<ComTaskExecution>emptyList());
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());

        Response response = target("devices/1/schedules").request().get(Response.class);
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, atLeastOnce()).findByUniqueMrid("1");
    }

    @Test
    public void testCommunicationPlaningForDecommissionedState(){
        Device device = mockDeviceWithState(DefaultState.DECOMMISSIONED.getKey());

        Response response = target("devices/1/schedules").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findByUniqueMrid("1");
        verify(device, times(1)).getState();
    }
}
