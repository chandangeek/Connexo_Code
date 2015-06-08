package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.DeviceLifeCycleConfigApplicationJerseyTest;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.DefaultLifeCycleTranslationKey;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceLifeCycleActionResourceTest extends DeviceLifeCycleConfigApplicationJerseyTest {

    @Test
    public void testDeviceLifeCycleActionJsonModel(){
        List<AuthorizedAction> actions = mockDefaultActions();
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getAuthorizedActions()).thenReturn(actions);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/actions").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List<?>>get("$.deviceLifeCycleActions")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycleActions")).hasSize(2);
        assertThat(model.<Number>get("$.deviceLifeCycleActions[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceLifeCycleActions[0].name")).isEqualTo(com.energyict.mdc.device.lifecycle.config.impl.DefaultLifeCycleTranslationKey.TRANSITION_START_COMMISSIONING.getDefaultFormat());
        assertThat(model.<Object>get("$.deviceLifeCycleActions[0].fromState")).isNotNull();
        assertThat(model.<Number>get("$.deviceLifeCycleActions[0].fromState.id")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceLifeCycleActions[0].fromState.name")).isEqualTo("In stock");
        assertThat(model.<Object>get("$.deviceLifeCycleActions[0].toState")).isNotNull();
        assertThat(model.<Number>get("$.deviceLifeCycleActions[0].toState.id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycleActions[0].toState.name")).isEqualTo("Commissioning");
        assertThat(model.<List<?>>get("$.deviceLifeCycleActions[0].privileges")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycleActions[0].privileges")).hasSize(1);
        assertThat(model.<String>get("$.deviceLifeCycleActions[0].privileges[0].privilege")).isEqualTo("ONE");
        assertThat(model.<String >get("$.deviceLifeCycleActions[0].privileges[0].name")).isEqualTo(DefaultLifeCycleTranslationKey.PRIVILEGE_LEVEL_1.getDefaultFormat());
        assertThat(model.<String >get("$.deviceLifeCycleActions[0].triggeredBy.symbol")).isEqualTo("#eventType");
        assertThat(model.<String >get("$.deviceLifeCycleActions[0].triggeredBy.name")).isNotEmpty();
    }

    @Test
    public void testEmptyDeviceLifeCycleActionsList(){
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getAuthorizedActions()).thenReturn(Collections.emptyList());
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/actions").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>>get("$.deviceLifeCycleActions")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycleActions")).isEmpty();
    }

    @Test
    public void testGetDeviceLifeCycleActionById(){
        List<AuthorizedAction> actions = mockDefaultActions();
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getAuthorizedActions()).thenReturn(actions);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/actions/1").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo(com.energyict.mdc.device.lifecycle.config.impl.DefaultLifeCycleTranslationKey.TRANSITION_DECOMMISSION.getDefaultFormat());
    }

    @Test
    public void testGetUnexistedDeviceLifeCycleAction(){
        List<AuthorizedAction> actions = mockDefaultActions();
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getAuthorizedActions()).thenReturn(actions);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        Response response = target("/devicelifecycles/1/actions/200").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
