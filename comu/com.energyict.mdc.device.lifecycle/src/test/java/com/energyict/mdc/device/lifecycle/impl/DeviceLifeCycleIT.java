package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.impl.TableSpecs;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
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
        this.inMemoryPersistence.getService(OrmService.class).invalidateCache(FiniteStateMachineService.COMPONENT_NAME, TableSpecs.FSM_FINITE_STATE_MACHINE.name());
        this.changeInitialState(DefaultState.IN_STOCK);

    }

    @Test
    @Transactional
    public void lifeCycleDatesOnNewDevice() {
        // Business method
        Device device = this.createSimpleDevice("lifeCycleDatesOnNewDevice", Instant.now());

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
        Device device = this.createSimpleDevice("activateDeviceSetsInstalledDate",creationTime);

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty lastCheckedProperty = mock(ExecutableActionProperty.class);
        PropertySpec lastCheckedPropertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key(),
                        true,
                        new InstantFactory());
        when(lastCheckedProperty.getPropertySpec()).thenReturn(lastCheckedPropertySpec);
        when(lastCheckedProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(activationTime, Arrays.asList(lastCheckedProperty));

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
        Device device = this.createSimpleDevice("installInActiveFromInStockDoesNotSetAnyCimDate", Instant.now());

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty lastCheckedProperty = mock(ExecutableActionProperty.class);
        PropertySpec lastCheckedPropertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key(),
                        true,
                        new InstantFactory());
        when(lastCheckedProperty.getPropertySpec()).thenReturn(lastCheckedPropertySpec);
        when(lastCheckedProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(activationTime, Arrays.asList(lastCheckedProperty));

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
        Device device = this.createSimpleDevice("startCommissioningDoesNotSetAnyCimDate", creationTime);

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.COMMISSIONING.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(activationTime, Collections.emptyList());

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
        Device device = this.createSimpleDevice("activateFromCommissioningSetsInstallDate", creationTime);

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty lastCheckedProperty = mock(ExecutableActionProperty.class);
        PropertySpec lastCheckedPropertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key(),
                        true,
                        new InstantFactory());
        when(lastCheckedProperty.getPropertySpec()).thenReturn(lastCheckedPropertySpec);
        when(lastCheckedProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(activationTime, Arrays.asList(lastCheckedProperty));

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
        Device device = this.createSimpleDevice("installInActiveFromInCommissioningDoesNotSetAnyCimDate", creationTime);

        Instant activationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty lastCheckedProperty = mock(ExecutableActionProperty.class);
        PropertySpec lastCheckedPropertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key(),
                        true,
                        new InstantFactory());
        when(lastCheckedProperty.getPropertySpec()).thenReturn(lastCheckedPropertySpec);
        when(lastCheckedProperty.getValue()).thenReturn(activationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(activationTime, Arrays.asList(lastCheckedProperty));

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
        Device device = this.createSimpleDevice("deactivateSetsRemovedDate", creationTime);

        Instant deactivationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(deactivationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        installAndActivateAction.execute(deactivationTime, Collections.emptyList());

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
        Device device = this.createSimpleDevice("creactivateAlsoResetsInstallDate", creationTime);

        // Activate the device for the first time
        Instant initialActivationTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(initialActivationTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.ACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableActionProperty lastCheckedProperty = mock(ExecutableActionProperty.class);
        PropertySpec lastCheckedPropertySpec = inMemoryPersistence.getPropertySpecService()
                .basicPropertySpec(
                        DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key(),
                        true,
                        new InstantFactory());
        when(lastCheckedProperty.getPropertySpec()).thenReturn(lastCheckedPropertySpec);
        when(lastCheckedProperty.getValue()).thenReturn(initialActivationTime);
        ExecutableAction installAndActivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();
        installAndActivateAction.execute(initialActivationTime, Arrays.asList(lastCheckedProperty));
        device = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
        State state = device.getState();
        assertThat(DefaultState.from(state)).isPresent();
        assertThat(DefaultState.from(state)).contains(DefaultState.ACTIVE);

        // Deactivate the device
        Instant deactivationTime = Instant.ofEpochMilli(30000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(deactivationTime);
        CustomStateTransitionEventType deactivatedEventType = DefaultCustomStateTransitionEventType.DEACTIVATED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableAction deactivateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, deactivatedEventType).get();
        deactivateAction.execute(deactivationTime, Collections.emptyList());
        device = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
        state = device.getState();
        assertThat(DefaultState.from(state)).isPresent();
        assertThat(DefaultState.from(state)).contains(DefaultState.INACTIVE);

        // Activate the device again
        Instant activationTime = Instant.ofEpochMilli(40000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(activationTime);
        ExecutableAction activateAction = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        activateAction.execute(activationTime, Arrays.asList(lastCheckedProperty));

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
        Device device = this.createSimpleDevice("decommissionActiveDeviceSetsRetiredDate",creationTime);

        assertThat(device.forValidation().isValidationActive()).isTrue();
        assertThat(device.forEstimation().isEstimationActive()).isTrue();

        Instant decommissioningTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(decommissioningTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DECOMMISSIONED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableAction action = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        action.execute(decommissioningTime, Collections.emptyList());

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
        Device device = this.createSimpleDevice("decommissionInactiveDeviceSetsRetiredDate", creationTime);

        Instant decommissioningTime = Instant.ofEpochMilli(20000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(decommissioningTime);
        CustomStateTransitionEventType activatedEventType = DefaultCustomStateTransitionEventType.DECOMMISSIONED.findOrCreate(inMemoryPersistence.getFiniteStateMachineService());
        ExecutableAction action = inMemoryPersistence.getDeviceLifeCycleService().getExecutableActions(device, activatedEventType).get();

        // Business method
        action.execute(decommissioningTime, Collections.emptyList());

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
        if (!stateMachine.getInitialState().equals(state.get())) {
            stateMachine.startUpdate().complete(state.get());
        }
    }

    private Device createSimpleDevice(String mRID, Instant when) {
        Device device =  createSimpleDeviceWithName(DEVICE_NAME, mRID);
        device.forValidation().activateValidation(when);
        device.forEstimation().activateEstimation();
        return device;
    }

}