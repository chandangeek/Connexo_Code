package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.impl.AppServiceConsoleService;
import com.elster.jupiter.appserver.impl.MessageHandlerLauncherService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.SwitchStateMachineEventHandlerFactory;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.events.DeviceLifeCycleChangeEventHandler;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-23 (15:06)
 */
public class ChangeDeviceLifeCycleWithSuccess extends PersistenceIntegrationTest {

    public static final String APP_SERVER_NAME = "DLC-IT";

    /**
     * Creates an additional FiniteStateMachine and DeviceLifeCycle
     * with a matching State so that the DeviceType can be switched.
     * This test cannot be run in @Transactional because the creation
     * of the device needs to be committed fore the message handler
     * that deals with updating the device's state will find the device.
     */
    @Test
    public void changeDeviceLifeCycleWithSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()) {
            latch = this.setupAppServerInfrastructure(latch);
            context.commit();
        }
        AppServiceConsoleService consoleService = inMemoryPersistence.getService(AppServiceConsoleService.class);
        consoleService.become(APP_SERVER_NAME);

        ((EventServiceImpl) inMemoryPersistence.getInjector()
                .getInstance(EventService.class)).removeTopicHandler(inMemoryPersistence.getDeviceLifeCycleChangeEventHandler());
        DeviceLifeCycleChangeEventHandler deviceLifeCycleChangeEventHandler = new LatchDrivenLifeCycleChangeHandler(
                inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class),
                inMemoryPersistence.getInjector().getInstance(DeviceDataModelService.class),
                inMemoryPersistence.getInjector().getInstance(MeteringService.class), latch);
        ((EventServiceImpl) inMemoryPersistence.getInjector()
                .getInstance(EventService.class)).addTopicHandler(deviceLifeCycleChangeEventHandler);

        DeviceConfigurationService deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        Instant deviceCreationTime = Instant.ofEpochSecond(1423096800L);    // May 2nd 2015 00:40::00 UTC
        when(inMemoryPersistence.getClock().instant()).thenReturn(deviceCreationTime);
        DeviceLifeCycle clone;
        Device device;
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()) {
            clone = inMemoryPersistence.getDeviceLifeCycleConfigurationService()
                    .cloneDeviceLifeCycle(deviceType.getDeviceLifeCycle(), "Cloned");
            device = this.createSimpleDevice("changeDeviceLifeCycleWithSuccess", deviceCreationTime);
            context.commit();
        }
        Optional<State> clonedInStock = clone.getFiniteStateMachine().getState(DefaultState.IN_STOCK.getKey());
        assertThat(clonedInStock).isPresent();

        Instant changeDeviceLifeCycleTime = deviceCreationTime.plusSeconds(1000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(changeDeviceLifeCycleTime);

        // Business method
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()) {
            deviceConfigurationService.changeDeviceLifeCycle(deviceType, clone);
            context.commit();
        }

        // Asserts: need to reload the device because it was updated as part of the change of device life cycle
        Instant now = changeDeviceLifeCycleTime.plusSeconds(1000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(now);
        latch.await();
        Device reloaded = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
        assertThat(reloaded.getState().getId()).isEqualTo(clonedInStock.get().getId());
    }

    private CountDownLatch setupAppServerInfrastructure(CountDownLatch latch) {
        String destinationSpecName = "SwitchStateMachineDest";
        String subscriberName = "SwitchStateMachineEventSubsc";
        SubscriberSpec subscriberSpec =
                inMemoryPersistence
                        .getService(MessageService.class)
                        .getSubscriberSpec(destinationSpecName, subscriberName)
                        .get();
        when(inMemoryPersistence.getBundleContext()
                .getProperty(AppService.SERVER_NAME_PROPERTY_NAME)).thenReturn(APP_SERVER_NAME);
        AppService appService = inMemoryPersistence.getAppService();
        MessageHandlerLauncherService launcherService = inMemoryPersistence.getService(MessageHandlerLauncherService.class);
        launcherService.activate();
        launcherService
                .addResource(
                        new LatchDrivenSwitchStateMachineEventHandlerFactory(latch),
                        ImmutableMap.of("destination", destinationSpecName, "subscriber", subscriberName));
        AppServer appServer = appService.createAppServer(APP_SERVER_NAME, new DefaultCronExpressionParser().parse("0 0 * * * ? *")
                .get());
        appServer.createSubscriberExecutionSpec(subscriberSpec, 1);
        appServer.activate();
        return latch;
    }

    private Device createSimpleDevice(String deviceName, Instant deviceCreationTime) {
        return createSimpleDeviceWithName(deviceName, deviceCreationTime);
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

    private class LatchDrivenLifeCycleChangeHandler extends DeviceLifeCycleChangeEventHandler {
        private final CountDownLatch latch;

        public LatchDrivenLifeCycleChangeHandler(DeviceConfigurationService deviceConfigurationService, DeviceDataModelService deviceDataModelService, MeteringService meteringService, CountDownLatch latch) {
            super(deviceConfigurationService, deviceDataModelService, meteringService);
            this.latch = latch;
        }

        @Override
        public void handle(LocalEvent localEvent) {
            super.handle(localEvent);
            this.latch.countDown();
        }
    }

}