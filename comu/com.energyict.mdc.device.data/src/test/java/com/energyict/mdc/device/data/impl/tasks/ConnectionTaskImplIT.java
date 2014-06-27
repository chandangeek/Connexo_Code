package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.tasks.ComTask;
import com.google.common.base.Optional;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    protected static final TimeDuration EVERY_HOUR = new TimeDuration(1, TimeDuration.HOURS);
    private static final String DEVICE_PROTOCOL_DIALECT_NAME = "Limbueregs";

    protected static long PARTIAL_SCHEDULED_CONNECTION_TASK1_ID;
    protected static long PARTIAL_SCHEDULED_CONNECTION_TASK2_ID;
    protected static long PARTIAL_SCHEDULED_CONNECTION_TASK3_ID;
    protected static long PARTIAL_INBOUND_CONNECTION_TASK1_ID;
    protected static long PARTIAL_INBOUND_CONNECTION_TASK2_ID;
    protected static long PARTIAL_INBOUND_CONNECTION_TASK3_ID;
    protected static long PARTIAL_CONNECTION_INITIATION_TASK1_ID;
    protected static long PARTIAL_CONNECTION_INITIATION_TASK2_ID;
    protected static long PARTIAL_CONNECTION_INITIATION_TASK3_ID;

    protected static final long IP_COMPORT_POOL_ID = 1;
    protected static final long MODEM_COMPORT_POOL_ID = IP_COMPORT_POOL_ID + 1;
    protected static final long INBOUND_COMPORT_POOL1_ID = MODEM_COMPORT_POOL_ID + 1;
    protected static final long INBOUND_COMPORT_POOL2_ID = INBOUND_COMPORT_POOL1_ID + 1;
    protected static final String IP_ADDRESS_PROPERTY_VALUE = "192.168.2.100";
    protected static final String UPDATED_IP_ADDRESS_PROPERTY_VALUE = "192.168.100.2";
    protected static final BigDecimal PORT_PROPERTY_VALUE = new BigDecimal(1521);
    protected static final BigDecimal UPDATED_PORT_PROPERTY_VALUE = new BigDecimal(4049);

    protected static ConnectionTypePluggableClass inboundNoParamsConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass outboundNoParamsConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass outboundIpConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass inboundIpConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass modemConnectionTypePluggableClass;
    protected static InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass;
    protected static OutboundComPortPool outboundTcpipComPortPool;
    protected static OutboundComPortPool outboundTcpipComPortPool2;
    protected static InboundComPortPool inboundTcpipComPortPool;
    protected static InboundComPortPool inboundTcpipComPortPool2;
    protected static OutboundComPortPool outboundModemComPortPool;

    protected DeviceCommunicationConfiguration deviceCommunicationConfiguration;
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

    public OnlineComServer getOnlineComServer() {
        return onlineComServer;
    }

    public OnlineComServer getOtherOnlineComServer() {
        return otherOnlineComServer;
    }

    @BeforeClass
    public static void registerConnectionTypePluggableClasses () {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    outboundNoParamsConnectionTypePluggableClass = registerConnectionTypePluggableClass(OutboundNoParamsConnectionTypeImpl.class);
                    inboundNoParamsConnectionTypePluggableClass = registerConnectionTypePluggableClass(InboundNoParamsConnectionTypeImpl.class);
                    inboundIpConnectionTypePluggableClass = registerConnectionTypePluggableClass(InboundIpConnectionTypeImpl.class);
                    outboundIpConnectionTypePluggableClass = registerConnectionTypePluggableClass(OutboundIpConnectionTypeImpl.class);
                    modemConnectionTypePluggableClass = registerConnectionTypePluggableClass(ModemConnectionType.class);
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    protected void refreshConnectionTypePluggableClasses () {
        outboundNoParamsConnectionTypePluggableClass = refreshConnectionTypePluggableClass(OutboundNoParamsConnectionTypeImpl.class);
        inboundNoParamsConnectionTypePluggableClass = refreshConnectionTypePluggableClass(InboundNoParamsConnectionTypeImpl.class);
        inboundIpConnectionTypePluggableClass = refreshConnectionTypePluggableClass(InboundIpConnectionTypeImpl.class);
        outboundIpConnectionTypePluggableClass = refreshConnectionTypePluggableClass(OutboundIpConnectionTypeImpl.class);
        modemConnectionTypePluggableClass = refreshConnectionTypePluggableClass(ModemConnectionType.class);
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass registerConnectionTypePluggableClass(Class<T> connectionTypeClass) {
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(connectionTypeClass.getSimpleName(), connectionTypeClass.getName());
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass refreshConnectionTypePluggableClass (Class<T> connectionTypeClass) {
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
    public static void deleteConnectionTypePluggableClasses () {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    outboundNoParamsConnectionTypePluggableClass.delete();
                    inboundNoParamsConnectionTypePluggableClass.delete();
                    inboundIpConnectionTypePluggableClass.delete();
                    outboundIpConnectionTypePluggableClass.delete();
                    modemConnectionTypePluggableClass.delete();
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    @AfterClass
    public static void deleteDiscoveryProtocolPluggableClasses () {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    discoveryProtocolPluggableClass.delete();
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    @BeforeClass
    public static void createComPortPools () {
        registerDiscoveryProtocolPluggableClasses();
        createIpComPortPools();
        createModemComPortPools();
    }

    @AfterClass
    public static void deleteComPortPools () {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    deleteComPortPool(outboundTcpipComPortPool);
                    deleteComPortPool(outboundTcpipComPortPool2);
                    deleteComPortPool(inboundTcpipComPortPool);
                    deleteComPortPool(inboundTcpipComPortPool2);
                    deleteComPortPool(outboundModemComPortPool);
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    private static void deleteComPortPool(ComPortPool comPortPool) {
        if (comPortPool != null) {
            comPortPool.delete();
        }
    }

    public static void registerDiscoveryProtocolPluggableClasses() {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    discoveryProtocolPluggableClass = registerDiscoveryProtocolPluggableClass(SimpleDiscoveryProtocol.class);
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    private static void createIpComPortPools() {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    outboundTcpipComPortPool = createOutboundIpComPortPool("TCP/IP out(1)");
                    outboundTcpipComPortPool2 = createOutboundIpComPortPool("TCP/IP out(2)");
                    inboundTcpipComPortPool = createInboundIpComPortPool("TCP/IP in(1)");
                    inboundTcpipComPortPool2 = createInboundIpComPortPool("TCP/IP in(2)");
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    private static void createModemComPortPools() {
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    outboundModemComPortPool = createOutboundModemComPortPool("Modem out(1)");
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    private static OutboundComPortPool createOutboundModemComPortPool(String name) {
        OutboundComPortPool modemComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        modemComPortPool.setActive(true);
        modemComPortPool.setComPortType(ComPortType.SERIAL);
        modemComPortPool.setName(name);
        modemComPortPool.setTaskExecutionTimeout(new TimeDuration(1, TimeDuration.MINUTES));
        modemComPortPool.save();
        return modemComPortPool;
    }

    private static InboundComPortPool createInboundIpComPortPool(String name) {
        InboundComPortPool ipComPortPool = inMemoryPersistence.getEngineModelService().newInboundComPortPool();
        ipComPortPool.setActive(true);
        ipComPortPool.setComPortType(ComPortType.TCP);
        ipComPortPool.setName(name);
        ipComPortPool.setDiscoveryProtocolPluggableClass(discoveryProtocolPluggableClass);
        ipComPortPool.save();
        return ipComPortPool;
    }

    protected DeviceDataServiceImpl getDeviceDataService() {
        return inMemoryPersistence.getDeviceDataService();
    }

    @Before
    public void setupComServers () {
        this.onlineComServer = createComServer("First");
        this.otherOnlineComServer = createComServer("Second");
    }

    protected OnlineComServer createComServer(String name) {
        OnlineComServer onlineComServer = inMemoryPersistence.getEngineModelService().newOnlineComServerInstance();
        onlineComServer.setName(name);
        onlineComServer.setNumberOfStoreTaskThreads(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        onlineComServer.setStoreTaskQueueSize(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
        onlineComServer.setStoreTaskThreadPriority(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        onlineComServer.setChangesInterPollDelay(TimeDuration.minutes(5));
        onlineComServer.setSchedulingInterPollDelay(TimeDuration.minutes(5));
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.save();
        return onlineComServer;
    }

    @Before
    public void reloadConnectionTypePluggableClasses () {
        outboundNoParamsConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(outboundNoParamsConnectionTypePluggableClass.getId());
        inboundNoParamsConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(inboundNoParamsConnectionTypePluggableClass.getId());
        inboundIpConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass.getId());
        outboundIpConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass.getId());
        modemConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(modemConnectionTypePluggableClass.getId());
    }

    @Before
    public void initializeMocks () {
        super.initializeMocks();
        this.device = createSimpleDevice("First");
        this.otherDevice = createSimpleDevice("Second");
        ProtocolDialectConfigurationProperties configDialect = createDialectConfigProperties();
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck();
        ComTask comTaskWithLogBooks = createComTaskWithLogBooks();
        ComTask comTaskWithRegisters = createComTaskWithRegisters();

        this.comTaskEnablement1 = createMockedComTaskEnablement(true, configDialect, comTaskWithBasicCheck);
        this.comTaskEnablement2 = createMockedComTaskEnablement(true, configDialect, comTaskWithLogBooks);
        this.comTaskEnablement3 = createMockedComTaskEnablement(true, configDialect, comTaskWithRegisters);

        deviceCommunicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);

        partialInboundConnectionTask = deviceCommunicationConfiguration.newPartialInboundConnectionTask("Inbound (1)", inboundNoParamsConnectionTypePluggableClass).
                build();

        partialInboundConnectionTask2 = deviceCommunicationConfiguration.newPartialInboundConnectionTask("Inbound (2)", inboundNoParamsConnectionTypePluggableClass).
                build();

        partialScheduledConnectionTask = deviceCommunicationConfiguration.newPartialScheduledConnectionTask("Outbound (1)", outboundNoParamsConnectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();

        partialScheduledConnectionTask2 = deviceCommunicationConfiguration.newPartialScheduledConnectionTask("Outbound (2)", outboundNoParamsConnectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();

        partialConnectionInitiationTask = deviceCommunicationConfiguration.newPartialConnectionInitiationTask("Initiation (1)", outboundIpConnectionTypePluggableClass, TimeDuration.minutes(5)).
                build();

        partialConnectionInitiationTask2 = deviceCommunicationConfiguration.newPartialConnectionInitiationTask("Initiation (2)", outboundIpConnectionTypePluggableClass, TimeDuration.minutes(5)).
                build();

        deviceCommunicationConfiguration.save();

        PARTIAL_INBOUND_CONNECTION_TASK1_ID = partialInboundConnectionTask.getId();
        PARTIAL_INBOUND_CONNECTION_TASK2_ID = partialInboundConnectionTask2.getId();
        PARTIAL_SCHEDULED_CONNECTION_TASK1_ID = partialScheduledConnectionTask.getId();
        PARTIAL_SCHEDULED_CONNECTION_TASK2_ID = partialScheduledConnectionTask2.getId();
        PARTIAL_CONNECTION_INITIATION_TASK1_ID = partialConnectionInitiationTask.getId();
        PARTIAL_CONNECTION_INITIATION_TASK2_ID = partialConnectionInitiationTask2.getId();

    }

    private Device createSimpleDevice(String mRID) {
        Device simpleDevice = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "SimpleDevice", mRID);
        simpleDevice.save();
        return simpleDevice;
    }

    protected ScheduledConnectionTask createOutboundWithIpPropertiesWithoutViolations(String name) {
        return this.createOutboundWithIpPropertiesWithoutViolations(name, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }

    protected ScheduledConnectionTask createOutboundWithIpPropertiesWithoutViolations(String name, ConnectionStrategy connectionStrategy) {
        partialConnectionInitiationTask.setName(name);
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTask connectionTask;
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(connectionStrategy)) {
            TemporalExpression nextExecutionSpecs = new TemporalExpression(EVERY_HOUR);
            connectionTask = inMemoryPersistence.getDeviceDataService().newMinimizeConnectionTask(
                    this.device,
                    this.partialScheduledConnectionTask,
                    outboundTcpipComPortPool,
                    nextExecutionSpecs);
        }
        else {
            connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        }
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        ((ScheduledConnectionTaskImpl) connectionTask).save();
        return connectionTask;
    }

    protected List<PropertySpec> getOutboundIpPropertySpecs() {
        return Arrays.asList(
                outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME),
                outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME));
    }

    protected void setIpConnectionProperties(ConnectionTask connectionTask, String ipAddress, BigDecimal port) {
        if (ipAddress != null) {
            connectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, ipAddress);
        }
        if (port != null) {
            connectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, port);
        }
    }

    private ComTaskEnablement createMockedComTaskEnablement(boolean useDefault, ProtocolDialectConfigurationProperties configDialect, ComTask comTask) {
        Optional<ProtocolDialectConfigurationProperties> optionalConfigDialect = Optional.fromNullable(configDialect);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(optionalConfigDialect);
        when(comTaskEnablement.usesDefaultConnectionTask()).thenReturn(useDefault);
        when(comTaskEnablement.getPriority()).thenReturn(comTaskEnablementPriority);
        return comTaskEnablement;
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
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()); // to make sure all elements in the composition are properly loaded
    }

    private ComTask createComTaskWithLogBooks(){
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME + 2);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createLogbooksTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()); // to make sure all elements in the composition are properly loaded
    }

    private ComTask createComTaskWithRegisters(){
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME + 3);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createRegistersTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()); // to make sure all elements in the composition are properly loaded
    }

    protected ComTaskExecution createComTaskExecutionAndSetNextExecutionTimeStamp(Date nextExecutionTimeStamp) {
        return createComTaskExecutionAndSetNextExecutionTimeStamp(nextExecutionTimeStamp, comTaskEnablement1);
    }


    protected ComTaskExecution createComTaskExecutionAndSetNextExecutionTimeStamp(Date nextExecutionTimeStamp, ComTaskEnablement comTaskEnablement) {
        ComTaskExecution comTaskExecution = createComTaskExecution(comTaskEnablement);
        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setNextExecutionTimeStampAndPriority(nextExecutionTimeStamp, 100);
        comTaskExecutionUpdater.update();
        return comTaskExecution;
    }

    protected ComTaskExecution createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(ConnectionTask<?, ?> connectionTask, Date nextExecutionTimeStamp){
        return createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(connectionTask, nextExecutionTimeStamp, comTaskEnablement1);
    }

    protected ComTaskExecution createComTaskExecWithConnectionTaskNextDateAndComTaskEnablement(ConnectionTask<?,?> connectionTask, Date nextExecutionTimeStamp, ComTaskEnablement comTaskEnablement){
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setNextExecutionTimeStampAndPriority(nextExecutionTimeStamp, 100);
        return comTaskExecutionUpdater.update();
    }

    protected ComTaskExecution createComTaskExecution() {
        return createComTaskExecution(comTaskEnablement1);
    }

    protected ComTaskExecution createComTaskExecution(ComTaskEnablement comTaskEnablement) {
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return comTaskExecution;
    }

    protected ComTaskExecution getReloadedComTaskExecution(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getComTaskExecutions().get(0);
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
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            return null;
        }
    }
}