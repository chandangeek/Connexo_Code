package test.com.energyict.protocolimplV2.nta.elster.demo;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeConstants;
import com.energyict.cbo.TimeDuration;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.comserver.core.impl.online.ComServerDAOImpl;
import com.energyict.comserver.main.online.ComServerLauncher;
import com.energyict.comserver.tools.Strings;
import com.energyict.cpo.CreateEvent;
import com.energyict.cpo.Environment;
import com.energyict.cpo.IdBusinessObject;
import com.energyict.cpo.ShadowList;
import com.energyict.cpo.Transaction;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.channels.ip.socket.TcpIpConnectionType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortPool;
import com.energyict.mdc.ports.ComPortPoolFactory;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.ports.OutboundComPortPool;
import com.energyict.mdc.protocol.ServerDeviceProtocolPluggableClass;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.shadow.ports.OutboundComPortPoolShadow;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;
import com.energyict.mdc.shadow.protocol.task.BasicCheckTaskShadow;
import com.energyict.mdc.shadow.protocol.task.LoadProfilesTaskShadow;
import com.energyict.mdc.shadow.protocol.task.RegistersTaskShadow;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import com.energyict.mdc.shadow.tasks.ComTaskShadow;
import com.energyict.mdc.shadow.tasks.ConnectionMethodShadow;
import com.energyict.mdc.shadow.tasks.NextExecutionSpecsShadow;
import com.energyict.mdc.shadow.tasks.OutboundConnectionTaskShadow;
import com.energyict.mdc.shadow.tasks.ProtocolDialectPropertiesShadow;
import com.energyict.mdc.shadow.tasks.ScheduledComTaskShadow;
import com.energyict.mdc.system.properties.HostName;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTaskFactory;
import com.energyict.mdc.tasks.ConnectionStrategy;
import com.energyict.mdc.tasks.ConnectionTypePluggableClass;
import com.energyict.mdc.tasks.ConnectionTypePluggableClassImpl;
import com.energyict.mdc.tasks.ConnectionTypeRelationTypeCreator;
import com.energyict.mdc.tasks.DeviceProtocolDialectRelationTypeCreator;
import com.energyict.mdc.tasks.OutboundConnectionTask;
import com.energyict.mdc.tasks.ProtocolDialectProperties;
import com.energyict.mdc.tasks.ScheduledComTask;
import com.energyict.mdc.tasks.ServerNextExecutionSpecs;
import com.energyict.mdw.amr.RegisterGroup;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceConfiguration;
import com.energyict.mdw.core.DeviceType;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileType;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.mdw.core.PluggableClassType;
import com.energyict.mdw.interfacing.mdc.MdcInterface;
import com.energyict.mdw.relation.RelationType;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.DeviceConfigurationShadow;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.mdw.shadow.DeviceTypeShadow;
import com.energyict.mdw.shadow.LoadProfileShadow;
import com.energyict.mdw.shadow.LoadProfileTypeShadow;
import com.energyict.mdw.shadow.PluggableClassShadow;
import com.energyict.mdw.shadow.amr.RegisterGroupShadow;
import com.energyict.mdw.shadow.amr.RegisterSpecShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import test.com.energyict.mdc.tasks.DeviceProtocolDialectNameEnum;
import test.com.energyict.mdc.tasks.Dsmr23DeviceProtocolDialect;
import test.com.energyict.protocolimplV2.nta.elster.AM100;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

/**
 * Launches a demo of the {@link com.energyict.mdc.servers.OnlineComServer} that will actually listen for
 * connections from the OnlineComServer and respond accordingly.
 *
 * @author: sva
 * @since: 19/10/12 (13:43)
 */
public final class ComServerTCPAM100Demo {

    private static final String DATABASE_USER_NAME = "comserverrd";
    private static final String DATABASE_PASSWORD = "comserverrd";
    private static final String JDBC_URL = "jdbcUrl=jdbc:oracle:thin:@10.0.0.12:1521:eiserver";

    private static final String COM_TASK_NAME = "AM100DemoComTask";
    private static final String RTU_TYPE_NAME = "AM100DemoDemoRtuType";
    private static final String RTU_NAME = "AM100DemoDemoRtu";
    private static final String HOST = "10.113.0.28";
    private static final BigDecimal PORT = new BigDecimal(4059);
    private static final BigDecimal TIMEOUT = new BigDecimal("4000");
    private static final String OUTBOUND_DEMO_POOL_NAME = "AM100DemoOutboundDemoPool";
    private static final String DEVICE_PROTOCOL_NAME = "AM100DemoDeviceProtocolName";
    private static final TimeDuration EVERY_MINUTE = new TimeDuration(1, TimeDuration.MINUTES);
    private static final String LOAD_PROFILE_TYPE_ELECTRICITY = "AM100DemoLoadProfileType_Electricity";
    private static final String LOAD_PROFILE_TYPE_DAILY = "AM100DemoLoadProfileType_Daily";
    private static final String LOAD_PROFILE_TYPE_MBUS_PROFILE = "AM100DemoLoadProfileType_Mbus_Profile";
    private static final String PASSWORD = "elster";
    private static final String COMPORT_NAME = "TCP1";
    private static final String DEVICE_CONFIG_AM100_DEMO_NAME = "device_config_am100_demo_name";

    private ComServerDAO comServerDAO;
    private OnlineComServer thisComServer;
    private OutboundComPortPool outboundComPortPool;
    private DeviceType rtuType;
    private Device rtu;
    private ProtocolDialectProperties protocolDialectProperties;
    private ConnectionTypePluggableClass connectionTypePluggableClass;
    private ServerDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private RelationType tcpConnectionTypeRelationType;
    private RelationType dialectRelationType;
    private ComTask comTask;
    private OutboundConnectionTask connectionTask;
    private ScheduledComTask scheduledComTask;

    private ComServerTCPAM100Demo() {
        super();
    }

    public static void main(String[] args) {
        new ComServerTCPAM100Demo().startDemo();
//        new ComServerTCPAM100Demo().cleanUp();
    }

    private void cleanUp() {
        this.connectToDatabase();
        this.startMeteringWarehouse();
        for (ServerNextExecutionSpecs serverNextExecutionSpecs : ManagerFactory.getCurrent().getNextExecutionSpecsFactory().findAll()) {
            safeDeleteBusinessObjects(serverNextExecutionSpecs);
        }

        for (ComTaskExecution comTaskExecution : ManagerFactory.getCurrent().getComTaskExecutionFactory().findAll()) {
            safeDeleteBusinessObjects(comTaskExecution);
        }
        for (ComTask task : ManagerFactory.getCurrent().getComTaskFactory().findAll()) {
            safeDeleteBusinessObjects(task);
        }
    }

    private void safeDeleteBusinessObjects(IdBusinessObject businessObject) {
        try {
            deleteBusinessObject(businessObject);
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to delete default demo objects, see stacktrace above");
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to delete default demo objects, see stacktrace above");
        }
    }

    private void startComServer() {
        System.out.println("Starting the online comserver...");
        ComServerLauncher.main(new String[0]);
    }

    private void startDemo() {
        this.connectToDatabase();
        this.startMeteringWarehouse();
        this.comServerDAO = new ComServerDAOImpl();
        if (this.findOrCreateDefaultObjects()) {
            startComServer();
        }
        this.waitForUserInterrupt();
        System.out.println("Demo ends here!");
        this.deleteDefaultObjects();
        System.exit(0);
    }

    private void waitForUserInterrupt() {
        System.out.println("Type stop to end this demo:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean continueDemo = true;
        while (continueDemo) {
            try {
                String commandString = reader.readLine();
                if ("stop".equalsIgnoreCase(commandString)) {
                    continueDemo = false;
                } else if (Strings.isEmpty(commandString)) {
                    Thread.sleep(TimeConstants.SECONDS_IN_MINUTE);
                } else {
                    System.out.println("Type stop to end this demo:");
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void connectToDatabase() {
        System.out.println("Connecting to the eiserver database...");
        Environment.setDefault(this.databaseProperties());
    }

    private Properties databaseProperties() {
        Properties properties = new Properties();
        properties.put("jdbcUrl", JDBC_URL);
        properties.put("dbUser", DATABASE_USER_NAME);
        properties.put("dbPassword", DATABASE_PASSWORD);
        return properties;
    }

    private void startMeteringWarehouse() {
        System.out.println("Starting the metering warehouse...");
        new MeteringWarehouseFactory().getBatch(true);
    }

    private void deleteDefaultObjects() {
        try {
            this.deleteDemoDeviceSetup();
            thisComServer.delete();
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to delete default demo objects, see stacktrace above");
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to delete default demo objects, see stacktrace above");
        }
    }

    private void deleteDemoDeviceSetup() {
        try {
            this.deleteBusinessObject(this.scheduledComTask);
            this.deleteBusinessObject(this.connectionTask);
            List<ComPort> comPorts = ManagerFactory.getCurrent().getComPortFactory().findAll();
            for (ComPort comPort : comPorts) {
                this.deleteBusinessObject(comPort);
            }
            this.deleteBusinessObject(this.outboundComPortPool);
            this.deleteBusinessObject(this.protocolDialectProperties);
            for (ProtocolDialectProperties dialectProperties : ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory().findProtocolDialectPropertiesForDevice(this.rtu.getId())) {
                this.deleteBusinessObject(dialectProperties);
            }
            this.deleteBusinessObject(this.rtu);
            this.deleteBusinessObject(this.rtuType);
            this.deleteBusinessObject(this.comTask);

            PluggableClass pluggableClass = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(DEVICE_PROTOCOL_NAME);
            this.deleteBusinessObject(pluggableClass);
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to delete default demo objects, see stacktrace above");
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to delete default demo objects, see stacktrace above");
        }
    }

    private void deleteBusinessObject(IdBusinessObject obsolete) throws BusinessException, SQLException {
        if (obsolete != null) {
            System.out.println("Deleting " + obsolete.toString());
            obsolete.delete();
        }
    }

    private boolean findOrCreateDefaultObjects() {
        return this.findOrCreateDefaultComServer()
                && this.findOrCreateDefaultOutboundComPortPool()
                && this.findOrCreateDemoDeviceSetup();
    }


    private boolean findOrCreateDemoDeviceSetup() {
        System.out.println("Populating database with Demo ComTask/Device/ConnectionType/ConnectionTask/ScheduledComTask");
        return this.findOrCreateComTask() &&
                this.findOrCreateDeviceProtocol() &&
                this.findOrCreateRtuType() &&
                this.findOrCreateRtu() &&
                this.findOrCreateConnectionType() &&
                this.findOrCreateConnectionTask() &&
                this.findOrCreateScheduledComTask();
    }

    private boolean findOrCreateDeviceProtocol() {
        PluggableClass deviceProtocol = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(DEVICE_PROTOCOL_NAME);
        try {
            if (deviceProtocol == null) {
                System.out.println("Creating demo DeviceProtocol with name " + DEVICE_PROTOCOL_NAME);
                PluggableClassShadow pluggableClassShadow = new PluggableClassShadow();
                pluggableClassShadow.setName(DEVICE_PROTOCOL_NAME);
                pluggableClassShadow.setJavaClassName(AM100.class.getName());
                pluggableClassShadow.setPluggableType(PluggableClassType.DEVICEPROTOCOL);
                deviceProtocol = MeteringWarehouse.getCurrent().getPluggableClassFactory().create(pluggableClassShadow);
            } else {
                System.out.println("Demo DeviceProtocol " + DEVICE_PROTOCOL_NAME + " already existed.");
            }

            this.dialectRelationType = MeteringWarehouse.getCurrent().getRelationTypeFactory().find(DeviceProtocolDialectNameEnum.DSMR23_DEVICE_PROTOCOL_DIALECT_NAME.getName());
            if (dialectRelationType == null) {
                createDeviceProtocolDialectRelationTypes(deviceProtocol);
            }

            this.deviceProtocolPluggableClass = ManagerFactory.getCurrent().getDeviceProtocolPluggableClassFactory().newForPluggableClass(deviceProtocol);
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create DeviceProtocol, see stacktrace above");
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create DeviceProtocol, see stacktrace above");
            return false;
        }
        return true;
    }

    private void createDeviceProtocolDialectRelationTypes(PluggableClass deviceProtocol) throws BusinessException, SQLException {
        final PluggableClass finalDeviceProtocol = deviceProtocol;
        MeteringWarehouse.getCurrent().execute(new Transaction<Object>() {
            @Override
            public Object doExecute() throws BusinessException, SQLException {
                new DeviceProtocolDialectRelationTypeCreator().handleEvent(new CreateEvent(finalDeviceProtocol));
                return null;
            }
        });
    }

    private boolean findOrCreateScheduledComTask() {
        List<ScheduledComTask> byRtu = ManagerFactory.getCurrent().getComTaskExecutionFactory().findScheduledByDevice(this.rtu);
        if (byRtu.isEmpty()) {
            try {
                System.out.println("Creating Demo ScheduledComTask for ConnectionTask that will reschedule every " + EVERY_MINUTE.toString());
                ScheduledComTaskShadow shadow = new ScheduledComTaskShadow();
                NextExecutionSpecsShadow nextExecutionSpecs = new NextExecutionSpecsShadow();
                nextExecutionSpecs.setFrequency(EVERY_MINUTE);
                shadow.setNextExecutionSpecs(nextExecutionSpecs);
                shadow.setDeviceId(this.rtu.getId());
                shadow.setComTaskId(this.comTask.getId());
                shadow.setProtocolDialectPropertiesId(this.protocolDialectProperties.getId());
                this.scheduledComTask = ManagerFactory.getCurrent().getComTaskExecutionFactory().createScheduled(shadow);
                this.scheduledComTask.scheduleNow();
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create ScheduledComTask, see stacktrace above");
                return false;
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create ScheduledComTask, see stacktrace above");
                return false;
            }
        } else {
            System.out.println("ScheduledComTask already configured on Device, rescheduling it now.");
            this.scheduledComTask = byRtu.get(0);
            try {
                this.scheduledComTask.scheduleNow();
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to schedule existing ScheduledComTask, see stacktrace above");
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to schedule existing ScheduledComTask, see stacktrace above");
            }
        }
        return true;
    }

    private boolean findOrCreateConnectionTask() {
        List<OutboundConnectionTask> outboundByRtu = ManagerFactory.getCurrent().getConnectionTaskFactory().findOutboundByDevice(this.rtu);
        if (outboundByRtu == null || outboundByRtu.isEmpty()) {
            try {
                this.connectionTask = this.createConnectionTask();
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create ConnectionTask, see stacktrace above");
                return false;
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create ConnectionTask, see stacktrace above");
                return false;
            }
        } else {
            System.out.println("Outbound connectionTask already exists on Device");
            this.connectionTask = outboundByRtu.get(0);
        }
        return true;
    }

    private OutboundConnectionTask createConnectionTask() throws BusinessException, SQLException {
        System.out.println("Creating Demo TCP ConnectionTask");
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow();
        shadow.setDeviceId(this.rtu.getId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        shadow.setDefault(true);
        ConnectionMethodShadow connectionMethodShadow = new ConnectionMethodShadow(this.connectionTypePluggableClass);
        connectionMethodShadow.setComPortPoolId(this.outboundComPortPool.getId());
        connectionMethodShadow.set(TcpIpConnectionType.HOST_PROPERTY_NAME, HOST);
        connectionMethodShadow.set(TcpIpConnectionType.PORT_PROPERTY_NAME, PORT);
        connectionMethodShadow.set(TcpIpConnectionType.CONNECTION_TIMEOUT_PROPERTY_NAME, TIMEOUT);

        shadow.setConnectionMethodShadow(connectionMethodShadow);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_MINUTE);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        return ManagerFactory.getCurrent().getConnectionTaskFactory().createOutbound(shadow);
    }

    private boolean findOrCreateConnectionType() {
        try {
            String connectionTypeName = "TCP ConnectionType";
            PluggableClass pc = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(connectionTypeName);
            if (pc == null) {
                System.out.println("Creating the Demo ConnectionType that uses TCP.");
                PluggableClassShadow pluggableClassShadow = new PluggableClassShadow();
                pluggableClassShadow.setName(connectionTypeName);
                pluggableClassShadow.setJavaClassName(TcpIpConnectionType.class.getName());
                pluggableClassShadow.setPluggableType(PluggableClassType.CONNECTIONTYPE);
                pc = MeteringWarehouse.getCurrent().getPluggableClassFactory().create(pluggableClassShadow);
                final PluggableClass pluggableClass = pc;
                MeteringWarehouse.getCurrent().execute(new Transaction<Object>() {
                    @Override
                    public Object doExecute() throws BusinessException, SQLException {
                        new ConnectionTypeRelationTypeCreator().handleEvent(new CreateEvent(pluggableClass));
                        return null;
                    }
                });
                this.tcpConnectionTypeRelationType = MeteringWarehouse.getCurrent().getRelationTypeFactory().find(TcpIpConnectionType.class.getSimpleName());
            } else {
                System.out.println("The Demo ConnectionType that uses TCP already existed.");
            }
            this.connectionTypePluggableClass = ConnectionTypePluggableClassImpl.newForPluggableClass(pc);
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create ConnectionType, see stacktrace above");
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create ConnectionType, see stacktrace above");
            return false;
        }
        return true;
    }

    private boolean findOrCreateRtuType() {
        try {
            this.rtuType = this.findRtuType(RTU_TYPE_NAME);
            if (this.rtuType == null) {
                System.out.println("Creating Demo Device Type with name " + RTU_TYPE_NAME);
                this.rtuType = this.createRtuType(RTU_TYPE_NAME);
                return true;
            } else {
                System.out.println("Demo Device Type with name " + RTU_TYPE_NAME + " already existed.");
            }
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Device Type, see stacktrace above");
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Device Type, see stacktrace above");
            return false;
        }
        return true;
    }

    public DeviceType findRtuType(String name) {
        List<DeviceType> rtuTypes = MeteringWarehouse.getCurrent().getDeviceTypeFactory().findByName(name);
        if (rtuTypes.isEmpty()) {
            return null;
        }
        return rtuTypes.get(0);
    }

    public DeviceType createRtuType(String name) throws BusinessException, SQLException {
        DeviceTypeShadow shadow = new DeviceTypeShadow();
        shadow.setName(name);
        shadow.setChannelCount(7);
        shadow.setDeviceProtocolId(this.deviceProtocolPluggableClass.getId());
        // TODO create the proper DeviceRegisterMappingShadows
//        shadow.setRegisterSpecShadows(findOrCreateAllRegiserSpecShadows());
        DeviceType type = MeteringWarehouse.getCurrent().getDeviceTypeFactory().create(shadow);
        // Update the deviceConfig with the correct DeviceType ID
        List<DeviceConfiguration> deviceConfigurations = MeteringWarehouse.getCurrent().getDeviceConfigFactory().findByName(DEVICE_CONFIG_AM100_DEMO_NAME);
        DeviceConfiguration deviceConfig = deviceConfigurations.get(0);
        if (deviceConfig != null) {
            DeviceConfigurationShadow configShadow = deviceConfig.getShadow();
            configShadow.setDeviceTypeId(type.getId());
            deviceConfig.update(configShadow);
        }
        return type;
    }

    private boolean findOrCreateRtu() {
        try {
            this.rtu = this.findOrCreateRtu(rtuType, RTU_NAME, TimeConstants.SECONDS_IN_MINUTE * 15);
            this.protocolDialectProperties = this.createProtocolDialectProperties(this.rtu);
            findOrCreateLoadProfileTypes();
            findOrCreateLoadProfiles();
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Device, see stacktrace above");
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Device, see stacktrace above");
            return false;
        }
        return true;
    }

    private ShadowList<RegisterSpecShadow> findOrCreateAllRegiserSpecShadows() throws BusinessException, SQLException {   //ToDo: add all needed registers here
        ShadowList<RegisterSpecShadow> registerSpecs = new ShadowList<RegisterSpecShadow>();
        RegisterGroup rtuRegisterGroup = MeteringWarehouse.getCurrent().getRegisterGroupFactory().findByName("Read Group").get(0);

        RegisterMapping registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Gas volume (OMS)", "Current Gas consumption [OMS]", false, ObisCode.fromString("7.1.3.0.0.255"), 0, rtuRegisterGroup.getId());
        registerSpecs.add(findOrCreateRegiserSpecShadow(registerMapping));

        registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Instantaneous voltage (L1) ", "Instantaneous voltage (L1)", false, ObisCode.fromString("1.0.32.7.0.255"), 0, rtuRegisterGroup.getId());
        registerSpecs.add(findOrCreateRegiserSpecShadow(registerMapping));
        return registerSpecs;
    }

    private RegisterSpecShadow findOrCreateRegiserSpecShadow(RegisterMapping mapping) throws BusinessException, SQLException {
        RegisterSpecShadow specShadow = new RegisterSpecShadow();
        specShadow.setRegisterMappingId(mapping.getId());
        specShadow.setDeviceChannelIndex(0);
        specShadow.setIntegral(false);
        specShadow.setNumberOfDigits(4);
        specShadow.setDeviceConfigId(getDeviceConfig().getId());
        return specShadow;
    }

    private DeviceConfiguration getDeviceConfig() throws BusinessException, SQLException {
        List<DeviceConfiguration> deviceConfigurations = MeteringWarehouse.getCurrent().getDeviceConfigFactory().findByName(DEVICE_CONFIG_AM100_DEMO_NAME);
        DeviceConfiguration deviceConfig = deviceConfigurations.get(0);
        if (deviceConfig == null) {
            DeviceConfigurationShadow shadow = new DeviceConfigurationShadow();
            shadow.setName(DEVICE_CONFIG_AM100_DEMO_NAME);
            shadow.setActive(true);
            shadow.setDeviceTypeId(121);    //ToDo: this is a wrong ID (cause ID of correct type not yet known, as we are creating it...)!!! After the creation of the type, the DeviceType will be updated with the correct ID.
            return MeteringWarehouse.getCurrent().getDeviceConfigFactory().create(shadow);
        }
        return deviceConfig;
    }

    private ProtocolDialectProperties createProtocolDialectProperties(Device rtu) throws BusinessException, SQLException {
        ProtocolDialectPropertiesShadow shadow = new ProtocolDialectPropertiesShadow();
        shadow.setName("DSM23DemoDialectName");
        shadow.setDeviceId(rtu.getId());
        shadow.setDeviceProtocolDialectName(DeviceProtocolDialectNameEnum.DSMR23_DEVICE_PROTOCOL_DIALECT_NAME.getName());
        shadow.getProperties().setProperty(Dsmr23DeviceProtocolDialect.SECURITY_LEVEL_PROPERTY_NAME, "1:0");
        shadow.getProperties().setProperty(Dsmr23DeviceProtocolDialect.FIX_MBUS_HEX_SHORT_ID, true);
        return ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory().create(shadow);
    }

    private void findOrCreateLoadProfileTypes() {
        // ToDo: add all LoadProfileTypes here
        findOrCreateElectricityLoadProfileType();
        findOrCreateDailyLoadProfileType();
        //ToDo: quick & dirty testing - the Mbus profile is attached to the Master - while running this demo, the serial numbers of the channels should be switched to the slave one in order the correct profile is read!
        findOrCreateMbusLoadProfile();
    }

    private void findOrCreateLoadProfiles() {
        if (this.rtu.getLoadProfiles().isEmpty()) {
            try {
                this.rtu.addLoadProfile(createLoadProfile(LOAD_PROFILE_TYPE_ELECTRICITY));
                this.rtu.addLoadProfile(createLoadProfile(LOAD_PROFILE_TYPE_DAILY));
                this.rtu.addLoadProfile(createLoadProfile(LOAD_PROFILE_TYPE_MBUS_PROFILE));
                //ToDo: add other load profiles here

                // Add the channels of the rtu to the loadProfiles
                List<LoadProfile> loadProfiles = this.rtu.getLoadProfiles();
                int y = 0;
                for (LoadProfile profile : loadProfiles) {
                    List<RegisterMapping> registerMappings = profile.getLoadProfileType().getRegisterMappings();
                    for (int i = 0; i < registerMappings.size(); i++) {
                        Channel channel = rtu.getChannel(y++);
                        profile.addChannel(channel, registerMappings.get(i));
                    }
                }
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to add a LoadProfile to the RTU, see stacktrace above");
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to add a LoadProfile to the RTU, see stacktrace above");
            }
        }
    }

    private LoadProfileShadow createLoadProfile(String typeName) throws BusinessException {
        List<LoadProfileType> loadProfileTypes = MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().findByName(typeName);
        if (!loadProfileTypes.isEmpty()) {
            LoadProfileShadow loadProfileShadow = new LoadProfileShadow();
            // TODO need to set the loadprofilespec ID
//            loadProfileShadow.setLoadProfileTypeId(loadProfileTypes.get(0).getId());
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 2);
            loadProfileShadow.setLastReading(calendar.getTime());
            return loadProfileShadow;
        }
        throw new BusinessException("Could not create loadProfile cause LoadProfileType " + typeName + " does not exists.");
    }

    public Device findOrCreateRtu(DeviceType rtuType, String name, int intervalInSeconds) throws BusinessException, SQLException {
        Device rtu = this.findRtu(name);
        if (rtu == null) {
            rtu = this.createRtu(rtuType, name, intervalInSeconds, MeteringWarehouse.getCurrent().findOrCreateFolder("/ComServer demo"));
        } else {
            System.out.println("Demo Device with name " + RTU_NAME + " already existed.");
        }
        DeviceShadow shadow = rtu.getShadow();
        TypedProperties properties = rtu.getProperties();
        properties.setProperty(MeterProtocol.PASSWORD, "elster");
        shadow.setProperties(properties);
        rtu.update(shadow);
        return rtu;
    }

    public Device findRtu(String name) {
        List<Device> rtus = MeteringWarehouse.getCurrent().getDeviceFactory().findByName(name);
        if (rtus.isEmpty()) {
            return null;
        }
        return rtus.get(0);
    }

    public Device createRtu(DeviceType rtuType, String name, int intervalInSeconds, Folder parent) throws BusinessException, SQLException {
        String serialNumber = "3228018";
        System.out.println("Creating Demo Device with name " + RTU_NAME + " and serial number " + serialNumber);
        DeviceShadow rtuShadow = rtuType.getConfigurations().get(0).newDeviceShadow();
        rtuShadow.setName(name);
        rtuShadow.setExternalName(name);
        rtuShadow.setSerialNumber(serialNumber);
        for (ChannelShadow channelShadow : rtuShadow.getChannelShadows().getNewShadows()) {
            channelShadow.setInterval(new TimeDuration(intervalInSeconds));
        }
        return parent.createRtu(rtuShadow);
    }

    private boolean findOrCreateDefaultOutboundComPortPool() {
        ComPortPoolFactory comPortPoolFactory = ManagerFactory.getCurrent().getComPortPoolFactory();
        ComPortPool comPortPool = comPortPoolFactory.find(OUTBOUND_DEMO_POOL_NAME);
        if (comPortPool == null) {
            try {
                OutboundComPortPoolShadow shadow = new OutboundComPortPoolShadow();
                shadow.setName(OUTBOUND_DEMO_POOL_NAME);
                shadow.setActive(true);
                shadow.setType(ComPortType.TCP);
                this.outboundComPortPool = comPortPoolFactory.createOutbound(shadow);
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create outbound ComPortPool, see stacktrace above");
                return false;
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create outbound ComPortPool, see stacktrace above");
                return false;
            }
        } else {
            this.outboundComPortPool = (OutboundComPortPool) comPortPool;
        }
        List<ComPort> allComPorts = ManagerFactory.getCurrent().getComPortFactory().findByComServer(thisComServer);
        OutboundComPortPoolShadow shadow = this.outboundComPortPool.getShadow();
        List<OutboundComPort> comPorts = this.outboundComPortPool.getComPorts();
        for (ComPort comPort : allComPorts) {
            if (!comPort.isInbound() && !comPorts.contains(comPort)) {
                shadow.addOutboundComPortId(comPort.getId());
            }
        }
        try {
            this.outboundComPortPool.update(shadow);
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to add outbound ComPorts to outbound ComPortPool, see stacktrace above");
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to add outbound ComPorts to outbound ComPortPool, see stacktrace above");
            return false;
        }
        return true;
    }

    private boolean findOrCreateDefaultComServer() {
        thisComServer = (OnlineComServer) this.comServerDAO.getThisComServer();
        if (thisComServer == null) {
            System.out.println("ComServer does not exist, creating it now...");
            try {
                thisComServer = this.createDefaultComServer();
                return true;
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create ComServer for this machine, see stacktrace above");
                return false;
            } catch (SQLException e) {
                System.out.println("Failed to create ComServer for this machine, see stacktrace above");
                return false;
            }
        } else {
            return true;
        }
    }

    private OnlineComServer createDefaultComServer() throws BusinessException, SQLException {
        return ManagerFactory.getCurrent().getComServerFactory().createOnline(this.defaultComServerShadow());
    }

    private OnlineComServerShadow defaultComServerShadow() {
        OnlineComServerShadow shadow = new OnlineComServerShadow();
        shadow.setActive(true);
        shadow.setName(HostName.getCurrent());
        shadow.setServerLogLevel(ComServer.LogLevel.INFO);
        shadow.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        shadow.setChangesInterPollDelay(EVERY_MINUTE);
        shadow.setSchedulingInterPollDelay(EVERY_MINUTE);
        this.addOutboundComPorts(shadow);
        return shadow;
    }

    private void addOutboundComPorts(OnlineComServerShadow shadow) {
        this.addOutbound(shadow, COMPORT_NAME);
    }

    private void addOutbound(OnlineComServerShadow shadow, String portName) {
        shadow.addOutboundComPort(this.outboundComPortShadow(portName));
    }

    private OutboundComPortShadow outboundComPortShadow(String portName) {
        OutboundComPortShadow comPortShadow = new OutboundComPortShadow();
        comPortShadow.setName(portName);
        comPortShadow.setActive(true);
        comPortShadow.setNumberOfSimultaneousConnections(1);
        comPortShadow.setType(ComPortType.TCP);
        return comPortShadow;
    }

    private ComTaskFactory getComTaskFactory() {
        MdcInterface mdcInterface = (MdcInterface) Environment.getDefault().get(MdcInterface.COMPONENT_NAME);
        return mdcInterface.getManager().getComTaskFactory();
    }

    private boolean findOrCreateComTask() {
        ComTaskFactory factory = getComTaskFactory();
        List<ComTask> comTasks = factory.find(COM_TASK_NAME);
        if (comTasks.isEmpty()) {
            System.out.println("Creating ComTask with BasicCheck task " +
                    "[#BasicCheckTask -> verifySerialNumber, verifyTimeDifference]");
            try {
                ComTaskShadow comTaskShadow = new ComTaskShadow();
                comTaskShadow.setName(COM_TASK_NAME);
                comTaskShadow.setStoreData(true);
                comTaskShadow.addProtocolTask(getBasicCheckTaskShadow());
                // ToDo: activate the needed tasks here
                comTaskShadow.addProtocolTask(getRegistersTaskShadow());
                comTaskShadow.addProtocolTask(getLoadProfilesTaskShadow());
                this.comTask = getComTaskFactory().createComTask(comTaskShadow);
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the Demo ComTask, see stacktrace above");
                return false;
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the Demo ComTask, see stacktrace above");
                return false;
            }
            return true;
        } else if (comTasks.size() == 1) {
            System.out.println("ComTask with BasicCheck task. [BasicCheckTask -> verifySerialNumber, verifyTimeDifference] already existed.");
            this.comTask = comTasks.get(0);
            return true;
        } else {
            return false;
        }

    }

    private BasicCheckTaskShadow getBasicCheckTaskShadow() {
        BasicCheckTaskShadow basicCheckTaskShadow = new BasicCheckTaskShadow();
        basicCheckTaskShadow.setVerifySerialNumber(true);
        basicCheckTaskShadow.setReadClockDifference(true);
        return basicCheckTaskShadow;
    }

    private RegistersTaskShadow getRegistersTaskShadow() {
        RegistersTaskShadow registersTaskShadow = new RegistersTaskShadow();
        ShadowList<RegisterGroupShadow> rtuRegisterGroupShadowList = new ShadowList<RegisterGroupShadow>();
        rtuRegisterGroupShadowList.add(MeteringWarehouse.getCurrent().getRegisterGroupFactory().findByName("Read Group").get(0).getShadow());
        registersTaskShadow.setRegisterGroupShadows(rtuRegisterGroupShadowList);
        return registersTaskShadow;
    }

    private LoadProfilesTaskShadow getLoadProfilesTaskShadow() {
        LoadProfilesTaskShadow loadProfilesTaskShadow = new LoadProfilesTaskShadow();
        ShadowList<LoadProfileTypeShadow> loadProfileTypeShadowShadowList = new ShadowList<LoadProfileTypeShadow>();
        loadProfileTypeShadowShadowList.add(findOrCreateElectricityLoadProfileType().getShadow());
        loadProfileTypeShadowShadowList.add(findOrCreateDailyLoadProfileType().getShadow());
        loadProfileTypeShadowShadowList.add(findOrCreateMbusLoadProfile().getShadow());
        //ToDo: add other loadProfile types here
        loadProfilesTaskShadow.setLoadProfileTypeShadows(loadProfileTypeShadowShadowList);
        loadProfilesTaskShadow.setFailIfLoadProfileConfigurationMisMatch(false);
        return loadProfilesTaskShadow;
    }

    //ToDo: WARNING - types are persistent (no auto-clean at the end of demo)! If you change something in this section, please manually clean-up old types (so they are recreated, instead of the old ones being reused)!
    private LoadProfileType findOrCreateElectricityLoadProfileType() {
        List<LoadProfileType> loadProfileTypes = MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().findByName(LOAD_PROFILE_TYPE_ELECTRICITY);
        if (loadProfileTypes.isEmpty()) {
            System.out.println("Creating LoadProfile Type " + LOAD_PROFILE_TYPE_ELECTRICITY);
            try {
                LoadProfileTypeShadow loadProfileTypeShadow = new LoadProfileTypeShadow();
                loadProfileTypeShadow.setName(LOAD_PROFILE_TYPE_ELECTRICITY);
                loadProfileTypeShadow.setInterval(new TimeDuration(900));
                loadProfileTypeShadow.setObisCode(ObisCode.fromString("1.0.99.1.0.255"));

                // Add the different register mappings to the load profile
                RegisterMapping registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Active Energy Import", "1.0.1.8.0.255");
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Active Energy Export", "1.0.1.8.0.255");
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                return MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().create(loadProfileTypeShadow);
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the LoadProfile type, see stacktrace above");
                return null;
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the LoadProfile type, see stacktrace above");
                return null;
            }

        } else {
            return loadProfileTypes.get(0);
        }
    }

    //ToDo: WARNING - types are persistent (no auto-clean at the end of demo)! If you change something in this section, please manually clean-up old types (so they are recreated, instead of the old ones being reused)!
    private LoadProfileType findOrCreateDailyLoadProfileType() {
        List<LoadProfileType> loadProfileTypes = MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().findByName(LOAD_PROFILE_TYPE_DAILY);
        if (loadProfileTypes.isEmpty()) {
            System.out.println("Creating LoadProfile Type " + LOAD_PROFILE_TYPE_DAILY);
            try {
                LoadProfileTypeShadow loadProfileTypeShadow = new LoadProfileTypeShadow();
                loadProfileTypeShadow.setName(LOAD_PROFILE_TYPE_DAILY);
                loadProfileTypeShadow.setInterval(new TimeDuration(86400));
                loadProfileTypeShadow.setObisCode(ObisCode.fromString("1.0.99.2.0.255"));

                // Add the different register mappings to the load profile
                RegisterMapping registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Daily Import Rate 1", "1.0.1.8.1.255");
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Daily Import Rate 2", "1.0.1.8.2.255");
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Daily Export Rate 1", "1.0.2.8.1.255");
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Daily Export Rate 2", "1.0.2.8.2.255");
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                return MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().create(loadProfileTypeShadow);
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the LoadProfile type, see stacktrace above");
                return null;
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the LoadProfile type, see stacktrace above");
                return null;
            }

        } else {
            return loadProfileTypes.get(0);
        }
    }

    //ToDo: WARNING - types are persistent (no auto-clean at the end of demo)! If you change something in this section, please manually clean-up old types (so they are recreated, instead of the old ones being reused)!
    private LoadProfileType findOrCreateMbusLoadProfile() {
        List<LoadProfileType> loadProfileTypes = MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().findByName(LOAD_PROFILE_TYPE_MBUS_PROFILE);
        if (loadProfileTypes.isEmpty()) {
            System.out.println("Creating LoadProfile Type " + LOAD_PROFILE_TYPE_MBUS_PROFILE);
            try {
                LoadProfileTypeShadow loadProfileTypeShadow = new LoadProfileTypeShadow();
                loadProfileTypeShadow.setName(LOAD_PROFILE_TYPE_MBUS_PROFILE);
                loadProfileTypeShadow.setInterval(new TimeDuration(3600));
                loadProfileTypeShadow.setObisCode(ObisCode.fromString("0.x.24.3.0.255"));

                // Add the different register mappings to the load profile
                RegisterMapping registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Mbus gas consumption", "7.x.3.0.0.255");
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                return MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().create(loadProfileTypeShadow);
            } catch (SQLException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the LoadProfile type, see stacktrace above");
                return null;
            } catch (BusinessException e) {
                e.printStackTrace(System.err);
                System.out.println("Failed to create the LoadProfile type, see stacktrace above");
                return null;
            }

        } else {
            return loadProfileTypes.get(0);
        }
    }
}