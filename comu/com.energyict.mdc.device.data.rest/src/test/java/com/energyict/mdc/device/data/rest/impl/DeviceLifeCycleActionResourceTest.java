package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.DefaultLifeCycleTranslationKey;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.EnableValidation;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceLifeCycleActionResourceTest extends DeviceDataRestApplicationJerseyTest {
    private static final String MAIN_DEVICE_MRID = "device1";

    @Mock
    Device device;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(deviceService.findByUniqueMrid(MAIN_DEVICE_MRID)).thenReturn(Optional.of(device));
        when(device.getmRID()).thenReturn(MAIN_DEVICE_MRID);
        when(device.getId()).thenReturn(1L);
        when(device.getVersion()).thenReturn(1L);
    }

    protected List<PropertySpec> mockLastCheckedPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key());
        when(propertySpec.getValueFactory()).thenReturn(new InstantFactory());
        when(propertySpec.isRequired()).thenReturn(true);
        return Arrays.asList(propertySpec);
    }

    private ExecutableAction mockExecutableAction(Device device, AuthorizedAction authorizedAction){
        ExecutableAction action = mock(ExecutableAction.class);
        when(action.getDevice()).thenReturn(device);
        when(action.getAction()).thenReturn(authorizedAction);
        return action;
    }

    @Test
    public void testGetAllAvailableTransitions(){
        AuthorizedTransitionAction action1 = mock(AuthorizedTransitionAction.class);
        when(action1.getId()).thenReturn(1L);
        when(action1.getName()).thenReturn("Z-Transition name 1");
        AuthorizedTransitionAction action2 = mock(AuthorizedTransitionAction.class);
        when(action2.getId()).thenReturn(2L);
        when(action2.getName()).thenReturn("A-Transition name 2");
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action1), mockExecutableAction(device, action2));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);

        String response = target("/devices/"+MAIN_DEVICE_MRID+"/transitions").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number> get("$.total")).isEqualTo(2);
        assertThat(model.<List<?>> get("$.transitions")).isNotEmpty();
        assertThat(model.<Number> get("$.transitions[0].id")).isEqualTo(2);
        assertThat(model.<String> get("$.transitions[0].name")).isEqualTo("A-Transition name 2");
        assertThat(model.<Number> get("$.transitions[1].id")).isEqualTo(1);
        assertThat(model.<String> get("$.transitions[1].name")).isEqualTo("Z-Transition name 1");
    }

    @Test
    public void testGetPropertiesForAction(){
        AuthorizedTransitionAction action1 = mock(AuthorizedTransitionAction.class);
        when(action1.getId()).thenReturn(1L);
        when(action1.getName()).thenReturn("Transition name 1");
        when(action1.getActions()).thenReturn(new HashSet<MicroAction>(Arrays.asList(MicroAction.values())));
        when(action1.getChecks()).thenReturn(new HashSet<MicroCheck>(Arrays.asList(MicroCheck.values())));
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action1));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);
        List<PropertySpec> propertySpecs = mockLastCheckedPropertySpec();
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.ENABLE_VALIDATION)).thenReturn(propertySpecs);

        String response = target("/devices/"+MAIN_DEVICE_MRID+"/transitions/1").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("Transition name 1");
        assertThat(model.<Number>get("$.deviceVersion")).isEqualTo(1);
        assertThat(model.<List>get("$.properties")).isNotEmpty();
        assertThat(model.<String>get("$.properties[0].key")).isNotEmpty();
    }

    @Test
    public void testExecuteBadVersion(){
        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.id = 1L;
        info.deviceVersion = 5000L;
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 5000L)).thenReturn(Optional.empty());

        Response response = target("/devices/" + MAIN_DEVICE_MRID + "/transitions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}
