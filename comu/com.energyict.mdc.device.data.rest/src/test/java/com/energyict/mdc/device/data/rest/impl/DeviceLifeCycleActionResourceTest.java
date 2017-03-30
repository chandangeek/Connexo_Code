/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ActionDoesNotRelateToDeviceStateException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DeviceLifeCycleActionViolationImpl;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceLifeCycleActionResourceTest extends DeviceDataRestApplicationJerseyTest {
    private static final String MAIN_DEVICE_NAME = "device1";

    @Mock
    Device device;
    @Mock
    DeviceConfiguration deviceConfiguration;
    @Mock
    DeviceType deviceType;
    @Mock
    State state;
    @Mock
    CIMLifecycleDates lifecycleDates;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(deviceService.findDeviceByName(MAIN_DEVICE_NAME)).thenReturn(Optional.of(device));
        when(device.getmRID()).thenReturn(MAIN_DEVICE_NAME);
        when(device.getId()).thenReturn(1L);
        when(device.getVersion()).thenReturn(1L);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getLifecycleDates()).thenReturn(lifecycleDates);
        when(state.getName()).thenReturn("Target state");
        when(lifecycleDates.getReceivedDate()).thenReturn(Optional.empty());
    }

    protected List<PropertySpec> mockLastCheckedPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key());
        when(propertySpec.getValueFactory()).thenReturn(new InstantFactory());
        when(propertySpec.isRequired()).thenReturn(true);
        return Arrays.asList(propertySpec);
    }

    private ExecutableAction mockExecutableAction(Device device, AuthorizedAction authorizedAction) {
        ExecutableAction action = mock(ExecutableAction.class);
        when(action.getDevice()).thenReturn(device);
        when(action.getAction()).thenReturn(authorizedAction);
        return action;
    }

    @Test
    public void testGetAllAvailableTransitions() {
        AuthorizedTransitionAction action1 = mock(AuthorizedTransitionAction.class);
        when(action1.getId()).thenReturn(1L);
        when(action1.getName()).thenReturn("Z-Transition name 1");
        AuthorizedTransitionAction action2 = mock(AuthorizedTransitionAction.class);
        when(action2.getId()).thenReturn(2L);
        when(action2.getName()).thenReturn("A-Transition name 2");
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action1), mockExecutableAction(device, action2));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);

        String response = target("/devices/" + MAIN_DEVICE_NAME + "/transitions").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List<?>>get("$.transitions")).isNotEmpty();
        assertThat(model.<Number>get("$.transitions[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.transitions[0].name")).isEqualTo("A-Transition name 2");
        assertThat(model.<Number>get("$.transitions[1].id")).isEqualTo(1);
        assertThat(model.<String>get("$.transitions[1].name")).isEqualTo("Z-Transition name 1");
    }

    @Test
    public void testGetPropertiesForAction() {
        AuthorizedTransitionAction action1 = mock(AuthorizedTransitionAction.class);
        when(action1.getId()).thenReturn(1L);
        when(action1.getName()).thenReturn("Transition name 1");
        when(action1.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));
        when(action1.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action1));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);
        List<PropertySpec> propertySpecs = mockLastCheckedPropertySpec();
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.ENABLE_VALIDATION)).thenReturn(propertySpecs);

        when(deviceService.findDeviceByName(MAIN_DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(eq(MAIN_DEVICE_NAME), anyLong())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfiguration));
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        PropertyValueConverter propertyValueConverter = mock(PropertyValueConverter.class);
        when(propertyValueInfoService.getConverter(any())).thenReturn(propertyValueConverter);
        String response = target("/devices/" + MAIN_DEVICE_NAME + "/transitions/1").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("Transition name 1");
        assertThat(model.<List>get("$.properties")).isNotEmpty();
        assertThat(model.<String>get("$.properties[0].key")).isNotEmpty();
    }

    @Test
    public void testExecuteBadVersion() {
        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.id = 1L;
        info.device = new DeviceInfo();
        info.device.name = MAIN_DEVICE_NAME;
        info.device.version = 56L;
        info.device.parent = new VersionInfo<>(1L, 1L);

        when(deviceService.findDeviceByName(MAIN_DEVICE_NAME)).thenReturn(Optional.empty());
        when(deviceService.findAndLockDeviceByNameAndVersion(eq(MAIN_DEVICE_NAME), anyLong())).thenReturn(Optional.<Device>empty());
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.empty());

        Response response = target("/devices/" + MAIN_DEVICE_NAME + "/transitions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testExecuteAndThrowSecurityException() throws Exception {
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getId()).thenReturn(1L);
        when(action.getName()).thenReturn("Transition name 1");
        when(action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));
        when(action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));
        StateTransition transition = mock(StateTransition.class);
        when(transition.getTo()).thenReturn(state);
        when(action.getStateTransition()).thenReturn(transition);
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);
        List<PropertySpec> propertySpecs = mockLastCheckedPropertySpec();
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.ENABLE_VALIDATION)).thenReturn(propertySpecs);
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        doReturn(actionProperty).when(deviceLifeCycleService).toExecutableActionProperty(Matchers.any(Object.class), Matchers.eq(propertySpecs.get(0)));
        Instant now = Instant.now();
        doThrow(new SecurityException("Security exception")).when(executableActions.get(0)).execute(eq(now), Matchers.anyList());

        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.id = 1L;
        info.device = new DeviceInfo();
        info.device.name = MAIN_DEVICE_NAME;
        info.device.version = 56L;
        info.device.parent = new VersionInfo<>(1L, 1L);

        when(deviceService.findDeviceByName(MAIN_DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(eq(MAIN_DEVICE_NAME), anyLong())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfiguration));

        info.properties = new ArrayList<>();
        PropertyInfo property = new PropertyInfo();
        info.effectiveTimestamp = now;
        info.properties.add(property);
        property.key = propertySpecs.get(0).getName();
        property.propertyValueInfo = new PropertyValueInfo<>(now.toEpochMilli(), null);

        Response response = target("/devices/" + MAIN_DEVICE_NAME + "/transitions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();
        DeviceLifeCycleActionResultInfo wizardResult = response.readEntity(DeviceLifeCycleActionResultInfo.class);
        assertThat(wizardResult.message).isEqualTo("Security exception");
        assertThat(wizardResult.targetState).isEqualTo("Target state");
        assertThat(wizardResult.result).isFalse();

        verify(transactionContext, never()).commit();
    }

    @Test
    public void testExecuteAndWrongState() throws Exception {
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 1L)).thenReturn(Optional.of(device));
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getId()).thenReturn(1L);
        when(action.getName()).thenReturn("Transition name 1");
        when(action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));
        when(action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));
        StateTransition transition = mock(StateTransition.class);
        when(transition.getTo()).thenReturn(state);
        when(action.getStateTransition()).thenReturn(transition);
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);
        List<PropertySpec> propertySpecs = mockLastCheckedPropertySpec();
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.ENABLE_VALIDATION)).thenReturn(propertySpecs);
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        doReturn(actionProperty).when(deviceLifeCycleService).toExecutableActionProperty(Matchers.any(Object.class), Matchers.eq(propertySpecs.get(0)));

        com.energyict.mdc.device.lifecycle.impl.MessageSeeds errorMessageSeeds = MessageSeeds.TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE;
        ActionDoesNotRelateToDeviceStateException exception = new ActionDoesNotRelateToDeviceStateException(action, device, thesaurus, errorMessageSeeds);
        Instant now = Instant.now();
        doThrow(exception).when(executableActions.get(0)).execute(eq(now), Matchers.anyList());

        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.id = 1L;
        info.device = new DeviceInfo();
        info.device.name = MAIN_DEVICE_NAME;
        info.device.version = 56L;
        info.device.parent = new VersionInfo<>(1L, 1L);

        when(deviceService.findDeviceByName(MAIN_DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(eq(MAIN_DEVICE_NAME), anyLong())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfiguration));

        info.properties = new ArrayList<>();
        PropertyInfo property = new PropertyInfo();
        info.effectiveTimestamp = now;
        info.properties.add(property);
        property.key = propertySpecs.get(0).getName();
        property.propertyValueInfo = new PropertyValueInfo<>(now.toEpochMilli(), null);

        Response response = target("/devices/" + MAIN_DEVICE_NAME + "/transitions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();
        DeviceLifeCycleActionResultInfo wizardResult = response.readEntity(DeviceLifeCycleActionResultInfo.class);
        assertThat(wizardResult.message).isNotEmpty();
        assertThat(wizardResult.targetState).isEqualTo("Target state");
        assertThat(wizardResult.result).isFalse();

        verify(transactionContext, never()).commit();
    }

    @Test
    public void testExecuteAndCheckFailed() throws Exception {
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getId()).thenReturn(1L);
        when(action.getName()).thenReturn("Transition name 1");
        when(action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));
        when(action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));
        StateTransition transition = mock(StateTransition.class);
        when(transition.getTo()).thenReturn(state);
        when(action.getStateTransition()).thenReturn(transition);
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);
        List<PropertySpec> propertySpecs = mockLastCheckedPropertySpec();
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.ENABLE_VALIDATION)).thenReturn(propertySpecs);
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        doReturn(actionProperty).when(deviceLifeCycleService).toExecutableActionProperty(Matchers.any(Object.class), Matchers.eq(propertySpecs.get(0)));

        Instant now = Instant.now();
        DeviceLifeCycleActionViolation violation = new DeviceLifeCycleActionViolationImpl(thesaurus,
                com.energyict.mdc.device.lifecycle.impl.MessageSeeds.ALL_DATA_VALID, MicroCheck.ALL_DATA_VALIDATED);
        MultipleMicroCheckViolationsException exception = new MultipleMicroCheckViolationsException(thesaurus,
                com.energyict.mdc.device.lifecycle.impl.MessageSeeds.MULTIPLE_MICRO_CHECKS_FAILED, Collections.singletonList(violation));
        doThrow(exception).when(executableActions.get(0)).execute(eq(now), Matchers.anyList());

        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.id = 1L;
        info.device = new DeviceInfo();
        info.device.name = MAIN_DEVICE_NAME;
        info.device.version = 56L;
        info.device.parent = new VersionInfo<>(1L, 1L);

        when(deviceService.findDeviceByName(MAIN_DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(eq(MAIN_DEVICE_NAME), anyLong())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfiguration));

        info.effectiveTimestamp = now;
        info.properties = new ArrayList<>();
        PropertyInfo property = new PropertyInfo();
        info.properties.add(property);
        property.key = propertySpecs.get(0).getName();
        property.propertyValueInfo = new PropertyValueInfo<>(now.toEpochMilli(), null);

        Response response = target("/devices/" + MAIN_DEVICE_NAME + "/transitions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();
        DeviceLifeCycleActionResultInfo wizardResult = response.readEntity(DeviceLifeCycleActionResultInfo.class);
        assertThat(wizardResult.message).isNotEmpty();
        assertThat(wizardResult.targetState).isEqualTo("Target state");
        assertThat(wizardResult.result).isFalse();

        verify(transactionContext, never()).commit();
    }

    @Test
    public void testExecuteSuccessful() throws Exception {
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 1L)).thenReturn(Optional.of(device));
        AuthorizedTransitionAction action = mock(AuthorizedTransitionAction.class);
        when(action.getId()).thenReturn(1L);
        when(action.getName()).thenReturn("Transition name 1");
        when(action.getActions()).thenReturn(new HashSet<>(Arrays.asList(MicroAction.values())));
        when(action.getChecks()).thenReturn(new HashSet<>(Arrays.asList(MicroCheck.values())));
        StateTransition transition = mock(StateTransition.class);
        when(transition.getTo()).thenReturn(state);
        when(action.getStateTransition()).thenReturn(transition);
        List<ExecutableAction> executableActions = Arrays.asList(mockExecutableAction(device, action));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(executableActions);
        List<PropertySpec> propertySpecs = mockLastCheckedPropertySpec();
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.ENABLE_VALIDATION)).thenReturn(propertySpecs);
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        doReturn(actionProperty).when(deviceLifeCycleService).toExecutableActionProperty(Matchers.any(Object.class), Matchers.eq(propertySpecs.get(0)));

        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.id = 1L;
        info.device = new DeviceInfo();
        info.device.name = MAIN_DEVICE_NAME;
        info.device.version = 56L;
        info.device.parent = new VersionInfo<>(1L, 1L);

        when(deviceService.findDeviceByName(MAIN_DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(eq(MAIN_DEVICE_NAME), anyLong())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(eq(1L), anyLong())).thenReturn(Optional.of(deviceConfiguration));

        info.properties = new ArrayList<>();
        PropertyInfo property = new PropertyInfo();
        info.properties.add(property);
        property.key = propertySpecs.get(0).getName();
        property.propertyValueInfo = new PropertyValueInfo<>(Instant.now().toEpochMilli(), null);

        Response response = target("/devices/" + MAIN_DEVICE_NAME + "/transitions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNotNull();
        DeviceLifeCycleActionResultInfo wizardResult = response.readEntity(DeviceLifeCycleActionResultInfo.class);
        assertThat(wizardResult.message).isNull();
        assertThat(wizardResult.targetState).isEqualTo("Target state");
        assertThat(wizardResult.result).isTrue();

        verify(transactionContext).commit();
    }
}
