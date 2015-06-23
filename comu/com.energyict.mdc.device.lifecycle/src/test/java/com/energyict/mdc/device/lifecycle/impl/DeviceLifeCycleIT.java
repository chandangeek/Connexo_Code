package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Executes integration tests for the device life cycle.
 * Will create a device and then trigger a number of state
 * changes, asserting the expected data changes
 * and verifying the expected behavior.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-11 (13:27)
 */
public class DeviceLifeCycleIT extends PersistenceIntegrationTest {

    private static final String DEVICE_NAME = "deviceName";

    @Before
    public void restoreDefaultLifeCycle() {
        this.changeInitialState(DefaultState.IN_STOCK);
    }

    @Test
    @Transactional
    public void lifeCycleDatesOnNewDevice() {
        // Business method
        Device device = this.createSimpleDevice("lifeCycleDatesOnNewDevice");

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void activateDeviceSetsInstalledDate() {
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("activateDeviceSetsInstalledDate");

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).contains(activationTime);
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void installInActiveFromInStockDoesNotSetAnyCimDate() {
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("installInActiveFromInStockDoesNotSetAnyCimDate");

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void startCommissioningDoesNotSetAnyCimDate() {
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("startCommissioningDoesNotSetAnyCimDate");

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.COMMISSIONING.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void activateFromCommissioningSetsInstallDate() {
        this.changeInitialState(DefaultState.COMMISSIONING);
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("activateFromCommissioningSetsInstallDate");

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).contains(activationTime);
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void installInActiveFromInCommissioningDoesNotSetAnyCimDate() {
        this.changeInitialState(DefaultState.COMMISSIONING);
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("installInActiveFromInCommissioningDoesNotSetAnyCimDate");

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void deactivateSetsRemovedDate() {
        this.changeInitialState(DefaultState.ACTIVE);
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("deactivateSetsRemovedDate");

        Instant deactivationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(deactivationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(deactivationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRemovedDate()).contains(deactivationTime);
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void reactivateAlsoResetsInstallDate() {
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("reactivateAlsoResetsInstallDate");

        // Activate the device for the first time
        Instant initialActivationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(initialActivationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty activateActionProperty = mock(ExecutableActionProperty.class);
        PropertySpec activatePropertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(activateActionProperty.getPropertySpec()).thenReturn(activatePropertySpec);
        when(activateActionProperty.getValue()).thenReturn(initialActivationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();
        installAndActivateAction.execute(Arrays.asList(activateActionProperty));
        device = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
        State state = device.getState();
        assertThat(DefaultState.from(state)).isPresent();
        assertThat(DefaultState.from(state)).contains(DefaultState.ACTIVE);

        // Deactivate the device
        Instant deactivationTime = Instant.ofEpochMilli(30000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(deactivationTime);
        CustomStateTransitionEventType deactivatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty deactivateActionProperty = mock(ExecutableActionProperty.class);
        PropertySpec deactivatePropertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(deactivateActionProperty.getPropertySpec()).thenReturn(deactivatePropertySpec);
        when(deactivateActionProperty.getValue()).thenReturn(deactivationTime);
        ExecutableAction deactivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, deactivatedEventType).get();
        deactivateAction.execute(Arrays.asList(deactivateActionProperty));
        device = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
        state = device.getState();
        assertThat(DefaultState.from(state)).isPresent();
        assertThat(DefaultState.from(state)).contains(DefaultState.INACTIVE);

        // Activate the device again
        Instant activationTime = Instant.ofEpochMilli(40000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        when(activateActionProperty.getValue()).thenReturn(activationTime);
        ExecutableAction activateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        activateAction.execute(Arrays.asList(activateActionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).contains(activationTime);
        assertThat(device.getLifecycleDates().getRemovedDate()).contains(deactivationTime);
        assertThat(device.getLifecycleDates().getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void decommissionActiveDeviceSetsRetiredDate() {
        this.changeInitialState(DefaultState.ACTIVE);
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("decommissionActiveDeviceSetsRetiredDate");

        Instant decommissioningTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(decommissioningTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DECOMMISSIONED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(decommissioningTime);
        ExecutableAction action = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        action.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).contains(decommissioningTime);
    }

    @Test
    @Transactional
    public void decommissionInactiveDeviceSetsRetiredDate() {
        this.changeInitialState(DefaultState.INACTIVE);
        Instant creationTime = Instant.ofEpochMilli(10000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(creationTime);
        Device device = this.createSimpleDevice("decommissionInactiveDeviceSetsRetiredDate");

        Instant decommissioningTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(decommissioningTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DECOMMISSIONED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty actionProperty = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                        true,
                        new InstantFactory());
        when(actionProperty.getPropertySpec()).thenReturn(propertySpec);
        when(actionProperty.getValue()).thenReturn(decommissioningTime);
        ExecutableAction action = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        action.execute(Arrays.asList(actionProperty));

        // Asserts
        assertThat(device.getLifecycleDates()).isNotNull();
        assertThat(device.getLifecycleDates().getManufacturedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getPurchasedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getReceivedDate()).isPresent();
        assertThat(device.getLifecycleDates().getInstalledDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRemovedDate()).isEmpty();
        assertThat(device.getLifecycleDates().getRetiredDate()).contains(decommissioningTime);
    }

    private void changeInitialState(DefaultState defaultState) {
        FiniteStateMachine stateMachine = deviceType.getDeviceLifeCycle().getFiniteStateMachine();
        Optional<State> state = stateMachine.getState(defaultState.getKey());
        stateMachine.startUpdate().complete(state.get()).save();
    }

    private Device createSimpleDevice(String mRID) {
        return createSimpleDeviceWithName(DEVICE_NAME, mRID);
    }

}