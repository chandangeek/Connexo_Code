package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.DeviceLifeCycleConfigApplicationJerseyTest;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
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
        List<AuthorizedAction> authorizedActions = mockDefaultActions();
        List<State> states = mockDefaultStates();
        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        when(finiteStateMachine.getStates()).thenReturn(states);
        when(dlc.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(dlc.getAuthorizedActions()).thenReturn(authorizedActions);
        when(finder.from(Matchers.any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.stream()).thenReturn(Collections.singletonList(dlc).stream());
        when(deviceLifeCycleConfigurationService.findAllDeviceLifeCycles()).thenReturn(finder);

        String stringResponse = target("/devicelifecycles").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).hasSize(1);
        assertThat(model.<Number>get("$.deviceLifeCycles[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycles[0].name")).isEqualTo("Standard");
        assertThat(model.<Number>get("$.deviceLifeCycles[0].statesCount")).isEqualTo(3);
        assertThat(model.<Number>get("$.deviceLifeCycles[0].actionsCount")).isEqualTo(2);
        assertThat(model.<List>get("$.deviceLifeCycles[0].deviceTypes")).hasSize(1);
        assertThat(model.<Number>get("$.deviceLifeCycles[0].deviceTypes[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycles[0].deviceTypes[0].name")).isEqualTo("Device Type");
    }

    @Test
    public void testEmptyDeviceLifeCycleList(){
        Finder<DeviceLifeCycle> finder = mock(Finder.class);
        when(finder.from(Matchers.any(JsonQueryParameters.class))).thenReturn(finder);
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
        List<AuthorizedAction> authorizedActions = mockDefaultActions();
        List<State> states = mockDefaultStates();
        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        when(finiteStateMachine.getStates()).thenReturn(states);
        when(dlc.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(dlc.getAuthorizedActions()).thenReturn(authorizedActions);
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
        List<AuthorizedAction> authorizedActions = mockDefaultActions();
        List<State> states = mockDefaultStates();
        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        when(finiteStateMachine.getStates()).thenReturn(states);
        when(lifeCycle.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(lifeCycle.getAuthorizedActions()).thenReturn(authorizedActions);
        when(deviceLifeCycleConfigurationService.newDefaultDeviceLifeCycle(Matchers.anyString())).thenReturn(lifeCycle);

        DeviceLifeCycleInfo newLifeCycle = new DeviceLifeCycleInfo();
        newLifeCycle.name = "New life cycle";
        Response response = target("/devicelifecycles").request().post(Entity.json(newLifeCycle));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCloneDeviceLifeCycle(){
        DeviceLifeCycle lifeCycle = mockSimpleDeviceLifeCycle(1L, "Cloned life cycle");
        List<AuthorizedAction> authorizedActions = mockDefaultActions();
        List<State> states = mockDefaultStates();
        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        when(finiteStateMachine.getStates()).thenReturn(states);
        when(lifeCycle.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(lifeCycle.getAuthorizedActions()).thenReturn(authorizedActions);
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
