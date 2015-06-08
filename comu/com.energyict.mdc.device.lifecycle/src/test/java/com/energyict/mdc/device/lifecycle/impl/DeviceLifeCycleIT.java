package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.impl.DefaultCustomStateTransitionEventType;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.impl.AppServiceConsoleService;
import com.elster.jupiter.appserver.impl.MessageHandlerLauncherService;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.SwitchStateMachineEventHandlerFactory;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

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
    public static final String APP_SERVER_NAME = "DLC-IT";

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

    /**
     * Creates an additional FiniteStateMachine and DeviceLifeCycle
     * with a matching State so that the DeviceType can be switched.
     * Todo: Figure out with Kore team on how to setup the app server so that it correctly process the queue
     */
    @Test
    @Transactional
    @Ignore
    public void changeDeviceLifeCycleWithSuccess() throws InterruptedException {
        SubscriberSpec subscriberSpec = inMemoryPersistence.getService(MessageService.class)
                .getSubscriberSpec("SwitchStateMachineEventDest", "SwitchStateMachineEventSubsc")
                .get();
        when(inMemoryPersistence.getBundleContext().getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.now());
        AppService appService = inMemoryPersistence.getAppService();
        MessageHandlerLauncherService launcherService = inMemoryPersistence.getService(MessageHandlerLauncherService.class);
        launcherService.activate();
        CountDownLatch latch = new CountDownLatch(1);
        launcherService
                .addResource(
                    new LatchDrivenSwitchStateMachineEventHandlerFactory(latch),
                    ImmutableMap.of("destination", "SwitchStateMachineEventDest", "subscriber", "SwitchStateMachineEventSubsc"));
        AppServer appServer = appService.createAppServer(APP_SERVER_NAME, new DefaultCronExpressionParser().parse("0 0 * * * ? *").get());
        appServer.createSubscriberExecutionSpec(subscriberSpec, 1);
        appServer.activate();
        AppServiceConsoleService consoleService = inMemoryPersistence.getService(AppServiceConsoleService.class);
        consoleService.become(APP_SERVER_NAME);

        DeviceLifeCycle clone = inMemoryPersistence.getDeviceLifeCycleConfigurationService().cloneDeviceLifeCycle(deviceType.getDeviceLifeCycle(), "Cloned");
        Optional<State> clonedInStock = clone.getFiniteStateMachine().getState(DefaultState.IN_STOCK.getKey());
        assertThat(clonedInStock).isPresent();
        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        Device device = this.createSimpleDevice("changeDeviceLifeCycleWithSuccess");

        // Business method
        deviceConfigurationService.changeDeviceLifeCycle(deviceType, clone);

        // Asserts: need to reload the device because it was updated as part of the change of device life cycle
        latch.await();
        Device reloaded = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
        assertThat(reloaded.getState().getId()).isEqualTo(clonedInStock.get().getId());
    }

    private void changeInitialState(DefaultState defaultState) {
        FiniteStateMachine stateMachine = deviceType.getDeviceLifeCycle().getFiniteStateMachine();
        Optional<State> state = stateMachine.getState(defaultState.getKey());
        stateMachine.startUpdate().complete(state.get()).save();
    }

    private Device createSimpleDevice(String mRID) {
        return createSimpleDeviceWithName(DEVICE_NAME, mRID);
    }

    private class LatchDrivenSwitchStateMachineEventHandlerFactory implements MessageHandlerFactory {
        private final CountDownLatch latch;
        private final SwitchStateMachineEventHandlerFactory actualFactory;

        private LatchDrivenSwitchStateMachineEventHandlerFactory(CountDownLatch latch) {
            super();
            this.latch = latch;
            this.actualFactory = inMemoryPersistence.getService(SwitchStateMachineEventHandlerFactory.class);
        }

        @Override
        public MessageHandler newMessageHandler() {
            return new LatchDrivenMessageHandler(this.latch, this.actualFactory.newMessageHandler());
        }
    }

    private class LatchDrivenMessageHandler implements MessageHandler {
        private final CountDownLatch latch;
        private final MessageHandler actualHandler;

        private LatchDrivenMessageHandler(CountDownLatch latch, MessageHandler actualHandler) {
            super();
            this.latch = latch;
            this.actualHandler = actualHandler;
        }

        @Override
        public void process(Message message) {
            this.actualHandler.process(message);
            this.latch.countDown();
        }

        @Override
        public void onMessageDelete(Message message) {
            this.actualHandler.onMessageDelete(message);
        }
    }

}