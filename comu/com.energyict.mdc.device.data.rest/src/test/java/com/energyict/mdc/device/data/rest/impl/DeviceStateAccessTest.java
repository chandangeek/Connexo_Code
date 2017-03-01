/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDeviceStage;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

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

    private Device mockDeviceWithState(String stateName, String stageName){
        State state = mock(State.class);
        Stage stage = mock(Stage.class);
        when(stage.getName()).thenReturn(stageName);
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(state.getName()).thenReturn(stateName);
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(device.getStage()).thenReturn(stage);
        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));
        return device;
    }

    @Test
    public void testActivateValidationForInStockState(){
        Device device = mockDeviceWithState(DefaultState.IN_STOCK.getKey(), EndDeviceStage.PRE_OPERATIONAL.name());

        Response response = target("devices/1/validationrulesets/validationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findDeviceByName("1");
        verify(device, times(1)).getStage();
    }

    @Test
    public void testActivateValidationForDecommissionedState(){
        Device device = mockDeviceWithState(DefaultState.DECOMMISSIONED.getKey(), EndDeviceStage.POST_OPERATIONAL.name());

        Response response = target("devices/1/validationrulesets/validationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findDeviceByName("1");
        verify(device, times(1)).getStage();
    }


    @Test
    public void testActivateValidationForCustomState(){
        Device device = mockDeviceWithState("Custom state", EndDeviceStage.OPERATIONAL.name());

        Response response = target("devices/1/validationrulesets/validationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findDeviceByName("1");
        verify(device, times(1)).getStage();
    }

    @Test
    public void testActivateEstimationForInStockState(){
        Device device = mockDeviceWithState(DefaultState.IN_STOCK.getKey(), EndDeviceStage.PRE_OPERATIONAL.name());

        Response response = target("devices/1/estimationrulesets/esimationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findDeviceByName("1");
        verify(device, times(1)).getStage();
    }

    @Test
    public void testActivateEstimationForDecommissionedState(){
        Device device = mockDeviceWithState(DefaultState.DECOMMISSIONED.getKey(), EndDeviceStage.POST_OPERATIONAL.name());

        Response response = target("devices/1/estimationrulesets/esimationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findDeviceByName("1");
        verify(device, times(1)).getStage();
    }

    @Test
    public void testActivateEstimationForCustomState(){
        Device device = mockDeviceWithState("Custom state", EndDeviceStage.OPERATIONAL.name());

        Response response = target("devices/1/estimationrulesets/esimationstatus").request().put(Entity.json("some entity"));
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findDeviceByName("1");
        verify(device, times(1)).getStage();
    }

    @Test
    public void testCommunicationPlaningForInStockState(){
        Device device = mockDeviceWithState(DefaultState.IN_STOCK.getKey(), EndDeviceStage.PRE_OPERATIONAL.name());
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.<ComTaskExecution>emptyList());
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());

        Response response = target("devices/1/schedules").request().get(Response.class);
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, atLeastOnce()).findDeviceByName("1");
    }

    @Test
    public void testCommunicationPlaningForDecommissionedState(){
        Device device = mockDeviceWithState(DefaultState.DECOMMISSIONED.getKey(), EndDeviceStage.POST_OPERATIONAL.name());

        Response response = target("devices/1/schedules").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceService, times(1)).findDeviceByName("1");
        verify(device, times(1)).getStage();
    }
}
