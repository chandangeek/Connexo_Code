package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.properties.PropertySpec;
import org.junit.Before;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;

/**
 * Provides code reuse opportunities for components that
 * will test the aspects of the ComTaskExecution class hierarchy
 * that relate to or rely on the device topology feature.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-10 (10:55)
 */
public abstract class AbstractComTaskExecutionInTopologyTest extends PersistenceIntegrationTest {

    protected static final String COM_TASK_NAME = "TheNameOfMyComTask";
    protected static final String DEVICE_PROTOCOL_DIALECT_NAME = "Limbueregs";
    protected static final int MAX_NR_OF_TRIES = 27;
    protected static final int COM_TASK_ENABLEMENT_PRIORITY = 213;

    protected ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;

    @Before
    public void getFirstProtocolDialectConfigurationPropertiesFromDeviceConfiguration() {
        deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        this.protocolDialectConfigurationProperties = this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
    }

    @Before
    public void addEventHandlers() {
        ServerTopologyService topologyService = inMemoryPersistence.getTopologyService();
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new DefaultConnectionTaskCreateEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ComTaskExecutionCreateEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ComTaskExecutionUpdateEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ComTaskExecutionObsoleteEventHandler(topologyService, mock(Thesaurus.class))));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new SetDefaultConnectionTaskEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ClearDefaultConnectionTaskEventHandler(topologyService)));
    }

    protected OutboundComPort createOutboundComPort() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerBuilder();
        String name = "ComServer";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.changesInterPollDelay(TimeDuration.minutes(5));
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.schedulingInterPollDelay(TimeDuration.minutes(1));
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.numberOfStoreTaskThreads(2);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = onlineComServer.newOutboundComPort("ComPort", 1);
        outboundComPortBuilder.comPortType(ComPortType.TCP);
        return outboundComPortBuilder.add();
    }

    protected ComTaskEnablement enableComTask(boolean useDefault) {
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        return enableComTask(useDefault, configDialect, COM_TASK_NAME);
    }

    protected ComTaskEnablement enableComTask(boolean useDefault, ProtocolDialectConfigurationProperties configDialect, String comTaskName) {
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck(comTaskName);
        ComTaskEnablementBuilder builder = this.deviceConfiguration.enableComTask(comTaskWithBasicCheck, this.securityPropertySet, configDialect);
        builder.useDefaultConnectionTask(useDefault);
        builder.setPriority(COM_TASK_ENABLEMENT_PRIORITY);
        return builder.add();
    }

    protected ComTask createComTaskWithBasicCheck(String comTaskName) {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(comTaskName);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(MAX_NR_OF_TRIES);
        comTask.createBasicCheckTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    protected ScheduledConnectionTaskImpl createASAPConnectionStandardTask(Device device) {
        return this.createASAPConnectionStandardTask(device, TimeDuration.minutes(5));
    }

    protected ScheduledConnectionTaskImpl createASAPConnectionStandardTask(Device device, TimeDuration frequency) {
        PartialScheduledConnectionTask partialScheduledConnectionTask = createPartialScheduledConnectionTask(frequency);
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        ScheduledConnectionTaskImpl myConnectionTask = createAsapWithNoPropertiesWithoutViolations("MyConnectionTask", device, partialScheduledConnectionTask, outboundPool);
        return myConnectionTask;
    }

    protected PartialScheduledConnectionTask createPartialScheduledConnectionTask(TimeDuration frequency) {
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(
                                OutboundNoParamsConnectionTypeImpl.class.getSimpleName(),
                                OutboundNoParamsConnectionTypeImpl.class.getName());
        connectionTypePluggableClass.save();
        return deviceConfiguration.
                newPartialScheduledConnectionTask(
                        "Outbound (1)",
                        connectionTypePluggableClass,
                        frequency,
                        ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();
    }

    protected OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool(name, ComPortType.TCP, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        ipComPortPool.setActive(true);
        ipComPortPool.update();
        return ipComPortPool;
    }

    protected ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name, Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool outboundTcpipComPortPool) {
        partialConnectionTask.setName(name);
        partialConnectionTask.save();

        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) device.getScheduledConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
        return scheduledConnectionTask;
    }

    protected ComTaskExecution reloadManuallyScheduledComTaskExecution(Device device, ComTaskExecution comTaskExecution) {
        Device reloadedDevice = getReloadedDevice(device);
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            if (comTaskExecution.getId() == taskExecution.getId()) {
                return taskExecution;
            }
        }
        fail("ComTaskExecution with id " + comTaskExecution.getId() + " not found after reloading device " + device.getName());
        return null;
    }

    protected class ComTaskExecutionDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return Property.DEVICE_PROTOCOL_DIALECT.getName();
        }

        @Override
        public List<PropertySpec> getUPLPropertySpecs() {
            return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::new).collect(Collectors.toList());
        }

        @Override
        public String getDeviceProtocolDialectDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }

    }

    private class SubscriberForTopicHandler implements Subscriber {
        private final TopicHandler topicHandler;

        private SubscriberForTopicHandler(TopicHandler topicHandler) {
            super();
            this.topicHandler = topicHandler;
        }

        @Override
        public void handle(Object notification, Object... notificationDetails) {
            LocalEvent event = (LocalEvent) notification;
            if (event.getType().getTopic().equals(this.topicHandler.getTopicMatcher())) {
                this.topicHandler.handle(event);
            }
        }

        @Override
        public Class<?>[] getClasses() {
            return new Class<?>[]{LocalEvent.class};
        }

    }

}