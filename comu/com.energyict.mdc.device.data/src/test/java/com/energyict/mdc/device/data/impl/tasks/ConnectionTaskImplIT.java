/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.Privilege;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides code reuse opportunities for test classes
 * that test components in the {@link ConnectionTaskImpl} class hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-13 (16:33)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class ConnectionTaskImplIT extends PersistenceIntegrationTest {

    protected static final TimeDuration EVERY_HOUR = new TimeDuration(1, TimeDuration.TimeUnit.HOURS);

    protected static long PARTIAL_SCHEDULED_CONNECTION_TASK1_ID;
    protected static long PARTIAL_SCHEDULED_CONNECTION_TASK2_ID;
    protected static long PARTIAL_SCHEDULED_CONNECTION_TASK3_ID;
    protected static long PARTIAL_INBOUND_CONNECTION_TASK1_ID;
    protected static long PARTIAL_INBOUND_CONNECTION_TASK2_ID;
    protected static long PARTIAL_INBOUND_CONNECTION_TASK3_ID;
    protected static long PARTIAL_CONNECTION_INITIATION_TASK1_ID;
    protected static long PARTIAL_CONNECTION_INITIATION_TASK2_ID;

    protected static final long IP_COMPORT_POOL_ID = 1;
    protected static final long MODEM_COMPORT_POOL_ID = IP_COMPORT_POOL_ID + 2;
    protected static final long INBOUND_COMPORT_POOL1_ID = MODEM_COMPORT_POOL_ID + 1;
    protected static final long INBOUND_COMPORT_POOL2_ID = INBOUND_COMPORT_POOL1_ID + 1;
    protected static final String IP_ADDRESS_PROPERTY_VALUE = "192.168.2.100";
    protected static final String UPDATED_IP_ADDRESS_PROPERTY_VALUE = "192.168.100.2";
    protected static final BigDecimal PORT_PROPERTY_VALUE = new BigDecimal(1521);
    protected static final BigDecimal UPDATED_PORT_PROPERTY_VALUE = new BigDecimal(4049);

    protected static ConnectionTypePluggableClass inboundNoParamsConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass outboundNoParamsConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass modemNoParamsConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass outboundIpConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass inboundIpConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass modemConnectionTypePluggableClass;
    protected static InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass;
    protected static OutboundComPortPool outboundTcpipComPortPool;
    protected static OutboundComPortPool outboundTcpipComPortPool2;
    protected static OutboundComPortPool inactiveOutboundTcpipComportPool;
    protected static InboundComPortPool inboundTcpipComPortPool;
    protected static InboundComPortPool inboundTcpipComPortPool2;
    protected static InboundComPortPool inactiveInboundTcpipComPortPool;
    protected static OutboundComPortPool outboundModemComPortPool;

    protected Device device;
    protected Device otherDevice;
    protected PartialInboundConnectionTask partialInboundConnectionTask;
    protected PartialInboundConnectionTask partialInboundConnectionTask2;
    protected PartialScheduledConnectionTask partialScheduledConnectionTask;
    protected PartialScheduledConnectionTask partialScheduledConnectionTask2;
    protected PartialConnectionInitiationTask partialConnectionInitiationTask;
    protected PartialConnectionInitiationTask partialConnectionInitiationTask2;

    protected int comTaskEnablementPriority = 213;
    private OnlineComServer onlineComServer;
    private OnlineComServer otherOnlineComServer;
    private String COM_TASK_NAME = "TheNameOfMyComTask";
    private int maxNrOfTries = 5;
    protected ComTaskEnablement comTaskEnablement1;
    protected ComTaskEnablement comTaskEnablement2;
    protected ComTaskEnablement comTaskEnablement3;

    @Before
    public void getFirstProtocolDialectConfigurationPropertiesFromDeviceConfiguration() {
        this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
    }

    public OnlineComServer getOnlineComServer() {
        return onlineComServer;
    }

    public OnlineComServer getOtherOnlineComServer() {
        return otherOnlineComServer;
    }

    @BeforeClass
    public static void registerConnectionTypePluggableClasses() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                outboundNoParamsConnectionTypePluggableClass = registerConnectionTypePluggableClass(OutboundNoParamsConnectionTypeImpl.class);
                inboundNoParamsConnectionTypePluggableClass = registerConnectionTypePluggableClass(InboundNoParamsConnectionTypeImpl.class);
                inboundIpConnectionTypePluggableClass = registerConnectionTypePluggableClass(InboundIpConnectionTypeImpl.class);
                outboundIpConnectionTypePluggableClass = registerConnectionTypePluggableClass(OutboundIpConnectionTypeImpl.class);
                modemConnectionTypePluggableClass = registerConnectionTypePluggableClass(ModemConnectionType.class);
                modemNoParamsConnectionTypePluggableClass = registerConnectionTypePluggableClass(ModemNoParamsConnectionTypeImpl.class);
            }
        });
    }

    protected void refreshConnectionTypePluggableClasses() {
        outboundNoParamsConnectionTypePluggableClass = refreshConnectionTypePluggableClass(OutboundNoParamsConnectionTypeImpl.class);
        inboundNoParamsConnectionTypePluggableClass = refreshConnectionTypePluggableClass(InboundNoParamsConnectionTypeImpl.class);
        inboundIpConnectionTypePluggableClass = refreshConnectionTypePluggableClass(InboundIpConnectionTypeImpl.class);
        outboundIpConnectionTypePluggableClass = refreshConnectionTypePluggableClass(OutboundIpConnectionTypeImpl.class);
        modemConnectionTypePluggableClass = refreshConnectionTypePluggableClass(ModemConnectionType.class);
        modemNoParamsConnectionTypePluggableClass = refreshConnectionTypePluggableClass(ModemNoParamsConnectionTypeImpl.class);
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass registerConnectionTypePluggableClass(Class<T> connectionTypeClass) {
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(connectionTypeClass.getSimpleName(), connectionTypeClass.getName());
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass refreshConnectionTypePluggableClass(Class<T> connectionTypeClass) {
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses =
                inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClassByClassName(connectionTypeClass.getName());
        return connectionTypePluggableClasses.get(0);
    }

    private static <T extends InboundDeviceProtocol> InboundDeviceProtocolPluggableClass registerDiscoveryProtocolPluggableClass(Class<T> discoveryProtocolClass) {
        InboundDeviceProtocolPluggableClass pluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newInboundDeviceProtocolPluggableClass(discoveryProtocolClass.getSimpleName(), discoveryProtocolClass.getName());
        pluggableClass.save();
        return pluggableClass;
    }

    @AfterClass
    public static void deleteConnectionTypePluggableClasses() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                delete(outboundNoParamsConnectionTypePluggableClass);
                delete(inboundNoParamsConnectionTypePluggableClass);
                delete(inboundIpConnectionTypePluggableClass);
                delete(outboundIpConnectionTypePluggableClass);
                delete(modemConnectionTypePluggableClass);
                delete(modemNoParamsConnectionTypePluggableClass);
            }
        });
    }

    private static void delete(ConnectionTypePluggableClass connectionTypePluggableClass) {
        if (connectionTypePluggableClass != null) {
            connectionTypePluggableClass.delete();
        }
    }

    @AfterClass
    public static void deleteDiscoveryProtocolPluggableClasses() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                delete(discoveryProtocolPluggableClass);
            }
        });
    }

    private static void delete(InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass) {
        if (discoveryProtocolPluggableClass != null) {
            discoveryProtocolPluggableClass.delete();
        }
    }

    @BeforeClass
    public static void createComPortPools() {
        registerDiscoveryProtocolPluggableClasses();
        createIpComPortPools();
        createModemComPortPools();
    }

    @AfterClass
    public static void deleteComPortPools() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                deleteComPortPool(outboundTcpipComPortPool);
                deleteComPortPool(outboundTcpipComPortPool2);
                deleteComPortPool(inactiveOutboundTcpipComportPool);
                deleteComPortPool(inboundTcpipComPortPool);
                deleteComPortPool(inboundTcpipComPortPool2);
                deleteComPortPool(outboundModemComPortPool);
                deleteComPortPool(inactiveInboundTcpipComPortPool);
            }
        });
    }

    private static void deleteComPortPool(ComPortPool comPortPool) {
        if (comPortPool != null) {
            comPortPool.delete();
        }
    }

    public static void registerDiscoveryProtocolPluggableClasses() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                discoveryProtocolPluggableClass = registerDiscoveryProtocolPluggableClass(SimpleDiscoveryProtocol.class);
            }
        });
    }

    private static void createIpComPortPools() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                outboundTcpipComPortPool = createOutboundIpComPortPool("TCP/IP out(1)");
                outboundTcpipComPortPool2 = createOutboundIpComPortPool("TCP/IP out(2)");
                inactiveOutboundTcpipComportPool = createInactiveOutboundIpComPortPool("TCP/IP out(3)");
                inboundTcpipComPortPool = createInboundIpComPortPool("TCP/IP in(1)");
                inboundTcpipComPortPool2 = createInboundIpComPortPool("TCP/IP in(2)");
                inactiveInboundTcpipComPortPool = createInactiveInboundIpComPortPool("TCP/IP in(3)");
            }
        });
    }

    private static void createModemComPortPools() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                outboundModemComPortPool = createOutboundModemComPortPool("Modem out(1)");
            }
        });
    }

    private static OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = createInactiveOutboundIpComPortPool(name);
        ipComPortPool.setActive(true);
        ipComPortPool.update();
        return ipComPortPool;
    }

    private static OutboundComPortPool createInactiveOutboundIpComPortPool(String name) {
        return inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool(name, ComPortType.TCP, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
    }

    private static OutboundComPortPool createOutboundModemComPortPool(String name) {
        OutboundComPortPool modemComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool(name, ComPortType.SERIAL, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        modemComPortPool.setActive(true);
        modemComPortPool.update();
        return modemComPortPool;
    }

    private static InboundComPortPool createInboundIpComPortPool(String name) {
        InboundComPortPool ipComPortPool = createInactiveInboundIpComPortPool(name);
        ipComPortPool.setActive(true);
        ipComPortPool.update();
        return ipComPortPool;
    }

    private static InboundComPortPool createInactiveInboundIpComPortPool(String name) {
        return inMemoryPersistence.getEngineConfigurationService().newInboundComPortPool(name, ComPortType.TCP, discoveryProtocolPluggableClass);
    }

    @Before
    public void setupComServers() {
        this.onlineComServer = createComServer("First");
        this.otherOnlineComServer = createComServer("Second");
    }

    protected OnlineComServer createComServer(String name) {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerBuilder();
        onlineComServer.name(name);
        onlineComServer.numberOfStoreTaskThreads(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        onlineComServer.storeTaskQueueSize(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
        onlineComServer.storeTaskThreadPriority(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        onlineComServer.changesInterPollDelay(TimeDuration.minutes(5));
        onlineComServer.schedulingInterPollDelay(TimeDuration.minutes(5));
        onlineComServer.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return onlineComServer.create();
    }

    @Before
    public void reloadConnectionTypePluggableClasses() {
        outboundNoParamsConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(outboundNoParamsConnectionTypePluggableClass.getId()).get();
        inboundNoParamsConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(inboundNoParamsConnectionTypePluggableClass.getId()).get();
        inboundIpConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass.getId()).get();
        outboundIpConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass.getId()).get();
        modemConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(modemConnectionTypePluggableClass.getId()).get();
        modemNoParamsConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(modemNoParamsConnectionTypePluggableClass.getId()).get();
    }

    @Before
    public void initializeMocks() {
        super.initializeMocks();
        this.device = createSimpleDevice("First");
        this.otherDevice = createSimpleDevice("Second");
        ProtocolDialectConfigurationProperties configDialect = createDialectConfigProperties();
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck();
        ComTask comTaskWithLogBooks = createComTaskWithLogBooks();
        ComTask comTaskWithRegisters = createComTaskWithRegisters();

        this.comTaskEnablement1 = enableComTask(true, configDialect, comTaskWithBasicCheck);
        this.comTaskEnablement2 = enableComTask(true, configDialect, comTaskWithLogBooks);
        this.comTaskEnablement3 = enableComTask(true, configDialect, comTaskWithRegisters);

        partialInboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("Inbound (1)", inboundNoParamsConnectionTypePluggableClass).
                build();

        partialInboundConnectionTask2 = deviceConfiguration.newPartialInboundConnectionTask("Inbound (2)", inboundNoParamsConnectionTypePluggableClass).
                build();

        partialScheduledConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("Outbound (1)", outboundNoParamsConnectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();

        partialScheduledConnectionTask2 = deviceConfiguration.newPartialScheduledConnectionTask("Outbound (2)", outboundNoParamsConnectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();

        partialConnectionInitiationTask = deviceConfiguration.newPartialConnectionInitiationTask("Initiation (1)", outboundNoParamsConnectionTypePluggableClass, TimeDuration.minutes(5)).
                build();

        partialConnectionInitiationTask2 = deviceConfiguration.newPartialConnectionInitiationTask("Initiation (2)", outboundIpConnectionTypePluggableClass, TimeDuration.minutes(5)).
                build();

        deviceConfiguration.save();

        PARTIAL_INBOUND_CONNECTION_TASK1_ID = partialInboundConnectionTask.getId();
        PARTIAL_INBOUND_CONNECTION_TASK2_ID = partialInboundConnectionTask2.getId();
        PARTIAL_SCHEDULED_CONNECTION_TASK1_ID = partialScheduledConnectionTask.getId();
        PARTIAL_SCHEDULED_CONNECTION_TASK2_ID = partialScheduledConnectionTask2.getId();
        PARTIAL_CONNECTION_INITIATION_TASK1_ID = partialConnectionInitiationTask.getId();
        PARTIAL_CONNECTION_INITIATION_TASK2_ID = partialConnectionInitiationTask2.getId();
    }

    private Device createSimpleDevice(String mRID) {
        Device simpleDevice = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, mRID, Instant.now());
        simpleDevice.save();
        return simpleDevice;
    }

    protected ScheduledConnectionTask createOutboundWithIpPropertiesWithoutViolations(String name) {
        return this.createOutboundWithIpPropertiesWithoutViolations(name, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }

    protected ScheduledConnectionTask createOutboundWithIpPropertiesWithoutViolations(String name, ConnectionStrategy connectionStrategy) {
        partialScheduledConnectionTask.setName(name);
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                .setConnectionStrategy(connectionStrategy);

        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(connectionStrategy)) {
            TemporalExpression nextExecutionSpecs = new TemporalExpression(EVERY_HOUR);
            scheduledConnectionTaskBuilder.setNextExecutionSpecsFrom(nextExecutionSpecs);
        }
        this.setIpConnectionProperties(scheduledConnectionTaskBuilder, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        return scheduledConnectionTaskBuilder.add();
    }

    protected List<PropertySpec> getOutboundIpPropertySpecs() {
        return Arrays.asList(
                outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get(),
                outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get());
    }

    protected void setIpConnectionProperties(Device.ScheduledConnectionTaskBuilder connectionTaskBuilder, String ipAddress, BigDecimal port) {
        if (ipAddress != null) {
            connectionTaskBuilder.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), ipAddress);
        }
        if (port != null) {
            connectionTaskBuilder.setProperty(IpConnectionProperties.PORT.propertyName(), port);
        }
    }

    protected void setIpConnectionProperties(ConnectionTask connectionTask, String ipAddress, BigDecimal port) {
        if (ipAddress != null) {
            connectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), ipAddress);
        }
        if (port != null) {
            connectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), port);
        }
    }

    private ComTaskEnablement enableComTask(boolean useDefault, ProtocolDialectConfigurationProperties configDialect, ComTask comTask) {
        ComTaskEnablementBuilder builder = this.deviceConfiguration.enableComTask(comTask, this.securityPropertySet, configDialect);
        builder.useDefaultConnectionTask(useDefault);
        builder.setPriority(this.comTaskEnablementPriority);
        return builder.add();
    }

    private ProtocolDialectConfigurationProperties createDialectConfigProperties() {
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        return configDialect;
    }

    private ComTask createComTaskWithBasicCheck() {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createBasicCheckTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    private ComTask createComTaskWithLogBooks() {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME + 2);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createLogbooksTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    private ComTask createComTaskWithRegisters() {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME + 3);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createRegistersTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    protected ComTaskExecution createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(ConnectionTask<?, ?> connectionTask, Instant nextExecutionTimeStamp) {
        return createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask, nextExecutionTimeStamp, comTaskEnablement1);
    }

    protected ComTaskExecution createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(ConnectionTask<?, ?> connectionTask, Instant nextExecutionTimeStamp, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.forceNextExecutionTimeStampAndPriority(nextExecutionTimeStamp, 100);
        return comTaskExecutionUpdater.update();
    }

    protected ComTaskExecution createComTaskExecution() {
        return createComTaskExecution(comTaskEnablement1);
    }

    protected ComTaskExecution createComTaskExecution(ComTaskEnablement comTaskEnablement) {
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return comTaskExecution;
    }

    private ComSchedule createComSchedule(ComTask comTask) {
        return inMemoryPersistence.getSchedulingService()
                .newComSchedule(comTask.getName(), new TemporalExpression(TimeDuration.days(1)), Instant.now())
                .addComTask(comTask)
                .build();
    }

    protected ComTaskExecution getReloadedComTaskExecution(ComTaskExecution comTaskExecution) {
        return  inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
    }

    protected void grantAllViewAndEditPrivilegesToPrincipal() {
        Set<Privilege> privileges = new HashSet<>();
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(EditPrivilege.LEVEL_1.getPrivilege());
        privileges.add(editPrivilege);
        Privilege viewPrivilege = mock(Privilege.class);
        when(viewPrivilege.getName()).thenReturn(ViewPrivilege.LEVEL_1.getPrivilege());
        privileges.add(viewPrivilege);
        when(inMemoryPersistence.getMockedUser().getPrivileges()).thenReturn(privileges);
        when(inMemoryPersistence.getMockedUser().getPrivileges(anyString())).thenReturn(privileges);
    }

    private class ComTaskExecutionDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return DEVICE_PROTOCOL_DIALECT_NAME;
        }

        @Override
        public String getDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }

    }

}