package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.DeviceFactory;
import com.energyict.mdc.device.data.PartialConnectionTaskFactory;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
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

    protected static final int DEVICE_ID = 100;
    protected static final int CODE_TABLE_ID = DEVICE_ID + 1;
    protected static final TimeDuration EVERY_HOUR = new TimeDuration(1, TimeDuration.HOURS);

    protected static final int PARTIAL_OUTBOUND_CONNECTION_TASK1_ID = CODE_TABLE_ID + 1;
    protected static final int PARTIAL_OUTBOUND_CONNECTION_TASK2_ID = PARTIAL_OUTBOUND_CONNECTION_TASK1_ID + 1;
    protected static final int PARTIAL_OUTBOUND_CONNECTION_TASK3_ID = PARTIAL_OUTBOUND_CONNECTION_TASK2_ID + 1;
    protected static final int PARTIAL_INBOUND_CONNECTION_TASK1_ID = PARTIAL_OUTBOUND_CONNECTION_TASK3_ID + 1;
    protected static final int PARTIAL_INBOUND_CONNECTION_TASK2_ID = PARTIAL_INBOUND_CONNECTION_TASK1_ID + 1;
    protected static final int PARTIAL_INBOUND_CONNECTION_TASK3_ID = PARTIAL_INBOUND_CONNECTION_TASK2_ID + 1;

    protected static final long IP_COMPORT_POOL_ID = 1;
    protected static final long MODEM_COMPORT_POOL_ID = IP_COMPORT_POOL_ID + 1;
    protected static final long INBOUND_COMPORT_POOL1_ID = MODEM_COMPORT_POOL_ID + 1;
    protected static final long INBOUND_COMPORT_POOL2_ID = INBOUND_COMPORT_POOL1_ID + 1;
    protected static final String IP_ADDRESS_PROPERTY_VALUE = "192.168.2.100";
    protected static final String UPDATED_IP_ADDRESS_PROPERTY_VALUE = "192.168.100.2";
    protected static final BigDecimal PORT_PROPERTY_VALUE = new BigDecimal(1521);
    protected static final BigDecimal UPDATED_PORT_PROPERTY_VALUE = new BigDecimal(4049);

    protected static ConnectionTypePluggableClass noParamsConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass ipConnectionTypePluggableClass;
    protected static ConnectionTypePluggableClass modemConnectionTypePluggableClass;
    protected static InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass;
    protected static OutboundComPortPool outboundTcpipComPortPool;
    protected static OutboundComPortPool outboundTcpipComPortPool2;
    protected static InboundComPortPool inboundTcpipComPortPool;
    protected static InboundComPortPool inboundTcpipComPortPool2;
    protected static OutboundComPortPool outboundModemComPortPool;

    @Mock
    protected DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    protected Device device;
    @Mock
    protected PartialInboundConnectionTask partialInboundConnectionTask;
    @Mock
    protected PartialInboundConnectionTask partialInboundConnectionTask2;
    @Mock
    protected PartialOutboundConnectionTask partialOutboundConnectionTask;
    @Mock
    protected PartialOutboundConnectionTask partialOutboundConnectionTask2;

    protected static Code codeTable;
    private static IdBusinessObjectFactory<Code> codeTableFactory;
    private OnlineComServer onlineComServer;

    public OnlineComServer getOnlineComServer() {
        return onlineComServer;
    }

    @BeforeClass
    public static void registerConnectionTypePluggableClasses () {
        initializeCodeTable();
        try {
            Environment.DEFAULT.get().execute(new Transaction<Object>() {
                @Override
                public Object doExecute() {
                    noParamsConnectionTypePluggableClass = registerConnectionTypePluggableClass(NoParamsConnectionType.class);
                    ipConnectionTypePluggableClass = registerConnectionTypePluggableClass(IpConnectionType.class);
                    modemConnectionTypePluggableClass = registerConnectionTypePluggableClass(ModemConnectionType.class);
                    return null;
                }
            });
        }
        catch (BusinessException | SQLException e) {
            // Not thrown by the transaction
        }
    }

    private static void initializeCodeTable () {
        codeTable = mock(Code.class);
        when(codeTable.getId()).thenReturn(CODE_TABLE_ID);
        when(codeTable.getName()).thenReturn("ConnectionTaskImplIT");
        when(codeTable.getBusinessObject()).thenReturn(codeTable);
        codeTableFactory = mock(IdBusinessObjectFactory.class);
        when(codeTableFactory.getInstanceType()).thenReturn(Code.class);
        when(codeTableFactory.findAll()).thenReturn(Arrays.asList(codeTable));
        when(codeTableFactory.get(CODE_TABLE_ID)).thenReturn(codeTable);
        when(codeTableFactory.findByPrimaryKey(CODE_TABLE_ID)).thenReturn(codeTable);
        when(codeTableFactory.getId()).thenReturn(FactoryIds.CODE.id());
        IpConnectionType.setCodeTableFactory(codeTableFactory);

        ApplicationContext applicationContext = Environment.DEFAULT.get().getApplicationContext();
        when(applicationContext.findFactory(FactoryIds.CODE.id())).thenReturn(codeTableFactory);
        when(applicationContext.findFactory(Code.class.getName())).thenReturn(codeTableFactory);
    }

    @AfterClass
    public static void clearCodeTableFactory () {
        IpConnectionType.setCodeTableFactory(null);
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass registerConnectionTypePluggableClass(Class<T> connectionTypeClass) {
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(connectionTypeClass.getSimpleName(), connectionTypeClass.getName());
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
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
                    noParamsConnectionTypePluggableClass.delete();
                    ipConnectionTypePluggableClass.delete();
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

    private static OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        ipComPortPool.setActive(true);
        ipComPortPool.setComPortType(ComPortType.TCP);
        ipComPortPool.setName(name);
        ipComPortPool.setTaskExecutionTimeout(new TimeDuration(1, TimeDuration.MINUTES));
        ipComPortPool.save();
        return ipComPortPool;
    }

    private static InboundComPortPool createInboundIpComPortPool(String name) {
        InboundComPortPool ipComPortPool = inMemoryPersistence.getEngineModelService().newInboundComPortPool();
        ipComPortPool.setActive(true);
        ipComPortPool.setComPortType(ComPortType.TCP);
        ipComPortPool.setName(name);
        ipComPortPool.setDiscoveryProtocolPluggableClassId(discoveryProtocolPluggableClass.getId());
        ipComPortPool.save();
        return ipComPortPool;
    }

    protected DeviceDataServiceImpl getDeviceDataService() {
        return inMemoryPersistence.getDeviceDataService();
    }

    @Before
    public void setupComServers () {
        this.onlineComServer = createComServer("First");
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
        noParamsConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(noParamsConnectionTypePluggableClass.getId());
        ipConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(ipConnectionTypePluggableClass.getId());
        modemConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().findConnectionTypePluggableClass(modemConnectionTypePluggableClass.getId());
    }

    @Before
    public void initializeMocks () {
        super.initializeMocks();
        when(this.device.getId()).thenReturn(DEVICE_ID);
        DeviceFactory deviceFactory = mock(DeviceFactory.class);
        when(deviceFactory.findDevice(DEVICE_ID)).thenReturn(this.device);
        List<DeviceFactory> deviceFactories = Arrays.asList(deviceFactory);
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceFactory.class)).thenReturn(deviceFactories);

        when(this.deviceCommunicationConfiguration.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);

        when(this.partialInboundConnectionTask.getId()).thenReturn(PARTIAL_INBOUND_CONNECTION_TASK1_ID);
        when(this.partialInboundConnectionTask.getName()).thenReturn("Inbound (1)");
        when(this.partialInboundConnectionTask.getConfiguration()).thenReturn(this.deviceCommunicationConfiguration);
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(noParamsConnectionTypePluggableClass);

        when(this.partialInboundConnectionTask2.getId()).thenReturn(PARTIAL_INBOUND_CONNECTION_TASK2_ID);
        when(this.partialInboundConnectionTask2.getName()).thenReturn("Inbound (2)");
        when(this.partialInboundConnectionTask2.getConfiguration()).thenReturn(this.deviceCommunicationConfiguration);
        when(this.partialInboundConnectionTask2.getPluggableClass()).thenReturn(noParamsConnectionTypePluggableClass);

        when(this.partialOutboundConnectionTask.getId()).thenReturn(PARTIAL_OUTBOUND_CONNECTION_TASK1_ID);
        when(this.partialOutboundConnectionTask.getName()).thenReturn("Outbound (1)");
        when(this.partialOutboundConnectionTask.getConfiguration()).thenReturn(this.deviceCommunicationConfiguration);
        when(this.partialOutboundConnectionTask.getPluggableClass()).thenReturn(noParamsConnectionTypePluggableClass);

        when(this.partialOutboundConnectionTask2.getId()).thenReturn(PARTIAL_OUTBOUND_CONNECTION_TASK2_ID);
        when(this.partialOutboundConnectionTask2.getName()).thenReturn("Outbound (2)");
        when(this.partialOutboundConnectionTask2.getConfiguration()).thenReturn(this.deviceCommunicationConfiguration);
        when(this.partialOutboundConnectionTask2.getPluggableClass()).thenReturn(noParamsConnectionTypePluggableClass);

        PartialConnectionTaskFactory partialConnectionTaskFactory = mock(PartialConnectionTaskFactory.class);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_INBOUND_CONNECTION_TASK1_ID)).thenReturn(this.partialInboundConnectionTask);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_INBOUND_CONNECTION_TASK2_ID)).thenReturn(this.partialInboundConnectionTask2);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_OUTBOUND_CONNECTION_TASK1_ID)).thenReturn(this.partialOutboundConnectionTask);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_OUTBOUND_CONNECTION_TASK2_ID)).thenReturn(this.partialOutboundConnectionTask2);
        List<PartialConnectionTaskFactory> partialConnectionTaskFactories = Arrays.asList(partialConnectionTaskFactory);
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(PartialConnectionTaskFactory.class)).thenReturn(partialConnectionTaskFactories);
    }

    protected ScheduledConnectionTask createOutboundWithIpPropertiesWithoutViolations(String name) {
        return createOutboundWithIpPropertiesWithoutViolations(name, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }

    protected ScheduledConnectionTask createOutboundWithIpPropertiesWithoutViolations(String name, ConnectionStrategy connectionStrategy) {
        when(this.partialOutboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask.getName()).thenReturn(name);
        ScheduledConnectionTask connectionTask;
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(connectionStrategy)) {
            TemporalExpression nextExecutionSpecs = new TemporalExpression(EVERY_HOUR);
            connectionTask = inMemoryPersistence.getDeviceDataService().newMinimizeConnectionTask(
                    this.device,
                   this.partialOutboundConnectionTask,
                    outboundTcpipComPortPool,
                    nextExecutionSpecs);
        }
        else {
            connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialOutboundConnectionTask, outboundTcpipComPortPool);
        }
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        connectionTask.save();
        return connectionTask;
    }

    protected List<PropertySpec> getOutboundIpPropertySpecs() {
        return Arrays.asList(
                ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME),
                ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME),
                ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME));
    }

    protected void addIpConnectionProperties (InboundConnectionTask connectionTask, String ipAddress, BigDecimal port, Code codeTable) {
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, ipAddress, port, codeTable);
    }

    protected void addIpConnectionProperties (OutboundConnectionTask connectionTask, String ipAddress, BigDecimal port, Code codeTable) {
        this.addIpConnectionProperties(connectionTask, outboundTcpipComPortPool, ipAddress, port, codeTable);
    }

    protected void addIpConnectionProperties (InboundConnectionTask connectionTask, InboundComPortPool comPortPool, String ipAddress, BigDecimal port, Code codeTable) {
        connectionTask.setComPortPool(comPortPool);
        this.setIpConnectionProperties(connectionTask, ipAddress, port, codeTable);
    }

    protected <T extends OutboundConnectionTask> void addIpConnectionProperties (T connectionTask, OutboundComPortPool comPortPool, String ipAddress, BigDecimal port, Code codeTable) {
        connectionTask.setComPortPool(comPortPool);
        this.setIpConnectionProperties(connectionTask, ipAddress, port, codeTable);
    }

    private void setIpConnectionProperties (ConnectionTask connectionTask, String ipAddress, BigDecimal port, Code codeTable) {
        if (ipAddress != null) {
            connectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, ipAddress);
        }
        if (port != null) {
            connectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, port);
        }
        if (codeTable != null) {
            connectionTask.setProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME, codeTable);
        }
    }

}