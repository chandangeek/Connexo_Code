package test.com.energyict.protocolimplV2.elster.ctr.demo;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeConstants;
import com.energyict.cbo.TimeDuration;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.comserver.core.impl.ComServerDAOImpl;
import com.energyict.comserver.main.online.ComServerLauncher;
import com.energyict.comserver.tools.Strings;
import com.energyict.cpo.*;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.channels.serial.*;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.*;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClassImpl;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.shadow.ports.OutboundComPortPoolShadow;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;
import com.energyict.mdc.shadow.protocol.task.BasicCheckTaskShadow;
import com.energyict.mdc.shadow.protocol.task.LoadProfilesTaskShadow;
import com.energyict.mdc.shadow.protocol.task.RegistersTaskShadow;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import com.energyict.mdc.shadow.tasks.*;
import com.energyict.mdc.system.properties.HostName;
import com.energyict.mdc.tasks.*;
import com.energyict.mdw.amr.RtuRegisterGroup;
import com.energyict.mdw.amr.RtuRegisterMapping;
import com.energyict.mdw.core.*;
import com.energyict.mdw.interfacing.mdc.MdcInterface;
import com.energyict.mdw.relation.RelationType;
import com.energyict.mdw.shadow.*;
import com.energyict.mdw.shadow.amr.RtuRegisterGroupShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterSpecShadow;
import com.energyict.obis.ObisCode;
import test.com.energyict.mdc.tasks.CtrDeviceProtocolDialect;
import test.com.energyict.mdc.tasks.DeviceProtocolDialectNameEnum;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.MTU155;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/**
 * Launches a demo of the {@link OnlineComServer} that uses
 * the meter simulation tool that will actually listen for
 * connections from the OnlineComServer and respond accordingly.
 *
 * @author: sva
 * @since: 19/10/12 (13:43)
 */
public final class ComServerOpticalMTU155Demo {

    private static final String DATABASE_USER_NAME = "comserverrd";
    private static final String DATABASE_PASSWORD = "comserverrd";
    private static final String JDBC_URL = "jdbcUrl=jdbc:oracle:thin:@10.0.0.12:1521:eiserver";

    private static final String COM_TASK_NAME = "ComServerOpticalMTU155DemoComTask";
    private static final String RTU_TYPE_NAME = "ComServerOpticalMTU155DemoRtuType";
    private static final String RTU_NAME = "ComServerOpticalMTU155DemoRtu";
    private static final String OUTBOUND_DEMO_POOL_NAME = "ComServerOpticalMTU155DemoOutboundDemoPool";
    private static final String DEVICE_PROTOCOL_NAME = "ComServerOpticalMTU155DemoDeviceProtocolName";
    private static final TimeDuration EVERY_MINUTE = new TimeDuration(1, TimeDuration.MINUTES);
    private static final String LOAD_PROFILE_TYPE_NAME = "OpticalMTU155DemoLoadProfileType";

    private ComServerDAO comServerDAO;
    private OnlineComServer thisComServer;
    private OutboundComPortPool outboundComPortPool;
    private RtuType rtuType;
    private Rtu rtu;
    private ProtocolDialectProperties protocolDialectProperties;
    private ConnectionTypePluggableClass connectionTypePluggableClass;
    private DeviceProtocolPluggableClassImpl deviceProtocolPluggableClass;
    private RelationType serialConnectionTypeRelationType;
    private RelationType dialectRelationType;
    private ComTask comTask;
    private OutboundConnectionTask connectionTask;
    private ScheduledComTask scheduledComTask;
    private String COMPORT_NAME = "COM10";
    private List<LoadProfileType> loadProfileTypes;

    private ComServerOpticalMTU155Demo() {
        super();
    }

    public static void main(String[] args) {
        new ComServerOpticalMTU155Demo().startDemo();
        new ComServerOpticalMTU155Demo().cleanUp();
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

    private void safeDeleteBusinessObjects(IdBusinessObject businessObject){
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
            for (ProtocolDialectProperties dialectProperties : ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory().findProtocolDialectPropertiesForRtu(this.rtu.getId())) {
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
        System.out.println("Populating database with Demo ComTask/Rtu/ConnectionType/ConnectionTask/ScheduledComTask");
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
                pluggableClassShadow.setJavaClassName(MTU155.class.getName());
                pluggableClassShadow.setPluggableType(PluggableClassType.DEVICEPROTOCOL);
                deviceProtocol = MeteringWarehouse.getCurrent().getPluggableClassFactory().create(pluggableClassShadow);
            } else {
                System.out.println("Demo DeviceProtocol " + DEVICE_PROTOCOL_NAME + " already existed.");
            }

            this.dialectRelationType = MeteringWarehouse.getCurrent().getRelationTypeFactory().find(DeviceProtocolDialectNameEnum.CTR_DEVICE_PROTOCOL_DIALECT_NAME.getName());
            if (dialectRelationType == null) {
                createDeviceProtocolDialectRelationTypes(deviceProtocol);
            }

            this.deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.newForPluggableClass(deviceProtocol);
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create DeviceProtocol, see stacktrace above");
            return false;
        } catch (SQLException  e) {
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
        List<ScheduledComTask> byRtu = ManagerFactory.getCurrent().getComTaskExecutionFactory().findScheduledByRtu(this.rtu);
        if (byRtu.isEmpty()) {
            try {
                System.out.println("Creating Demo ScheduledComTask for ConnectionTask that will reschedule every " + EVERY_MINUTE.toString());
                ScheduledComTaskShadow shadow = new ScheduledComTaskShadow();
                NextExecutionSpecsShadow nextExecutionSpecs = new NextExecutionSpecsShadow();
                nextExecutionSpecs.setFrequency(EVERY_MINUTE);
                shadow.setNextExecutionSpecs(nextExecutionSpecs);
                shadow.setRtuId(this.rtu.getId());
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
            System.out.println("ScheduledComTask already configured on Rtu, rescheduling it now.");
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
        List<OutboundConnectionTask> outboundByRtu = ManagerFactory.getCurrent().getConnectionTaskFactory().findOutboundByRtu(this.rtu);
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
            System.out.println("Outbound connectionTask already exists on Rtu");
            this.connectionTask = outboundByRtu.get(0);
        }
        return true;
    }

    private OutboundConnectionTask createConnectionTask() throws BusinessException, SQLException {
        System.out.println("Creating Demo Serial ConnectionTask");
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow();
        shadow.setRtuId(this.rtu.getId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        shadow.setDefault(true);
        ConnectionMethodShadow connectionMethodShadow = new ConnectionMethodShadow(this.connectionTypePluggableClass);
        connectionMethodShadow.setComPortPoolId(this.outboundComPortPool.getId());

        ConnectionTaskPropertyShadow baudrateShadow = new ConnectionTaskPropertyShadow(SerialPortConfiguration.BAUDRATE_NAME);
        baudrateShadow.setValue(BaudrateValues.BAUDRATE_9600.getBaudrate());
        connectionMethodShadow.add(baudrateShadow);
        ConnectionTaskPropertyShadow stopBitsShadow = new ConnectionTaskPropertyShadow(SerialPortConfiguration.NR_OF_STOP_BITS_NAME);
        stopBitsShadow.setValue(NrOfStopBits.ONE.getNrOfStopBits());
        connectionMethodShadow.add(stopBitsShadow);
        ConnectionTaskPropertyShadow dataBitsShadow = new ConnectionTaskPropertyShadow(SerialPortConfiguration.NR_OF_DATA_BITS_NAME);
        dataBitsShadow.setValue(NrOfDataBits.EIGHT.getNrOfDataBits());
        connectionMethodShadow.add(dataBitsShadow);
        ConnectionTaskPropertyShadow parityShadow = new ConnectionTaskPropertyShadow(SerialPortConfiguration.PARITY_NAME);
        parityShadow.setValue(Parities.NONE.getParity());
        connectionMethodShadow.add(parityShadow);
        ConnectionTaskPropertyShadow flowControlShadow = new ConnectionTaskPropertyShadow(SerialPortConfiguration.FLOW_CONTROL_NAME);
        flowControlShadow.setValue(FlowControl.NONE.getFlowControl());
        connectionMethodShadow.add(flowControlShadow);
        ConnectionTaskPropertyShadow readTimeOutShadow = new ConnectionTaskPropertyShadow(SerialPortConfiguration.SERIAL_PORT_READ_TIMEOUT_NAME);
        readTimeOutShadow.setValue(SerialPortConfiguration.DEFAULT_SERIAL_PORT_READ_TIMEOUT);
        connectionMethodShadow.add(readTimeOutShadow);
        ConnectionTaskPropertyShadow writeTimeOutShadow = new ConnectionTaskPropertyShadow(SerialPortConfiguration.SERIAL_PORT_WRITE_TIMEOUT_NAME);
        writeTimeOutShadow.setValue(SerialPortConfiguration.DEFAULT_SERIAL_PORT_WRITE_TIMEOUT);
        connectionMethodShadow.add(writeTimeOutShadow);

        shadow.setConnectionMethodShadow(connectionMethodShadow);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_MINUTE);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        return ManagerFactory.getCurrent().getConnectionTaskFactory().createOutbound(shadow);
    }

    private boolean findOrCreateConnectionType() {
        try {
            String connectionTypeName = "Serial ConnectionType";
            PluggableClass pc = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(connectionTypeName);
            if (pc == null) {
                System.out.println("Creating the Demo ConnectionType that uses Serial.");
                PluggableClassShadow pluggableClassShadow = new PluggableClassShadow();
                pluggableClassShadow.setName(connectionTypeName);
                pluggableClassShadow.setJavaClassName(SioSerialConnectionType.class.getName());
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
                this.serialConnectionTypeRelationType = MeteringWarehouse.getCurrent().getRelationTypeFactory().find(SioSerialConnectionType.class.getSimpleName());
            } else {
                System.out.println("The Demo ConnectionType that uses Serial already existed.");
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
                System.out.println("Creating Demo Rtu Type with name " + RTU_TYPE_NAME);
                this.rtuType = this.createRtuType(RTU_TYPE_NAME);
                return true;
            } else {
                System.out.println("Demo Rtu Type with name " + RTU_TYPE_NAME + " already existed.");
            }
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Rtu Type, see stacktrace above");
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Rtu Type, see stacktrace above");
            return false;
        }
        return true;
    }

    public RtuType findRtuType(String name) {
        List<RtuType> rtuTypes = MeteringWarehouse.getCurrent().getRtuTypeFactory().findByName(name);
        if (rtuTypes.isEmpty()) {
            return null;
        }
        return rtuTypes.get(0);
    }

    public RtuType createRtuType(String name) throws BusinessException, SQLException {
        RtuTypeShadow shadow = new RtuTypeShadow();
        shadow.setName(name);
        shadow.setChannelCount(2);
        shadow.setDeviceProtocolId(this.deviceProtocolPluggableClass.getId());
        shadow.setRegisterSpecShadows(findOrCreateAllRegiserSpecShadows());
        return MeteringWarehouse.getCurrent().getRtuTypeFactory().create(shadow);
    }

    private boolean findOrCreateRtu() {
        try {
            this.rtu = this.findOrCreateRtu(rtuType, RTU_NAME, TimeConstants.SECONDS_IN_MINUTE * 15);
            this.protocolDialectProperties = this.createProtocolDialectProperties(this.rtu);
            findOrCreateLoadProfiles();
        } catch (BusinessException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Rtu, see stacktrace above");
            return false;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.out.println("Failed to create Rtu, see stacktrace above");
            return false;
        }
        return true;
    }

    private ShadowList<RtuRegisterSpecShadow> findOrCreateAllRegiserSpecShadows() throws BusinessException, SQLException {
        ShadowList<RtuRegisterSpecShadow> registerSpecs = new ShadowList<RtuRegisterSpecShadow>();
        RtuRegisterGroup rtuRegisterGroup = MeteringWarehouse.getCurrent().getRtuRegisterGroupFactory().findByName("Read Group").get(0);

        RtuRegisterMapping registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Battery time remaining", "F.5.0, Battery time remaining (hours)", false, ObisCode.fromString("0.0.96.6.6.255"), 0, rtuRegisterGroup.getId());
        registerSpecs.add(findOrCreateRegiserSpecShadow(registerMapping));

        registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Battery hours used", "F.5.1, Battery hours used (hours)", false, ObisCode.fromString("0.0.96.6.0.255"), 0, rtuRegisterGroup.getId());
        registerSpecs.add(findOrCreateRegiserSpecShadow(registerMapping));

        registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Battery voltage", "F.5.2, Battery voltage", false, ObisCode.fromString("0.0.96.6.3.255"), 0, rtuRegisterGroup.getId());
        registerSpecs.add(findOrCreateRegiserSpecShadow(registerMapping));
        return registerSpecs;
    }

    private RtuRegisterSpecShadow findOrCreateRegiserSpecShadow(RtuRegisterMapping mapping) {
        RtuRegisterSpecShadow specShadow = new RtuRegisterSpecShadow();
        specShadow.setRegisterMappingId(mapping.getId());
        specShadow.setDeviceChannelIndex(0);
        specShadow.setIntegral(false);
        specShadow.setNumberOfDigits(4);
        return specShadow;
    }

    private ProtocolDialectProperties createProtocolDialectProperties(Rtu rtu) throws BusinessException, SQLException {
        ProtocolDialectPropertiesShadow shadow = new ProtocolDialectPropertiesShadow();
        shadow.setName("DemoDialectName");
        shadow.setRtuId(rtu.getId());
        shadow.setDeviceProtocolDialectName(DeviceProtocolDialectNameEnum.CTR_DEVICE_PROTOCOL_DIALECT_NAME.getName());
        shadow.setDeviceProtocolPluggableClassId(this.deviceProtocolPluggableClass.getId());
        List<DeviceProtocolDialectPropertyShadow> propertyShadows = new ArrayList<DeviceProtocolDialectPropertyShadow>();
        shadow.setPropertyShadows(propertyShadows);
        //All required properties
        DeviceProtocolDialectPropertyShadow prop = new DeviceProtocolDialectPropertyShadow(CtrDeviceProtocolDialect.SECURITY_LEVEL_PROPERTY_NAME, new BigDecimal(1));
        propertyShadows.add(prop);
        prop = new DeviceProtocolDialectPropertyShadow(CtrDeviceProtocolDialect.ENCRYPTION_KEY_C_PROPERTY_NAME, "0000000000000001");
        propertyShadows.add(prop);
        prop = new DeviceProtocolDialectPropertyShadow(CtrDeviceProtocolDialect.ENCRYPTION_KEY_F_PROPERTY_NAME, "0000000000000001");
        propertyShadows.add(prop);
        prop = new DeviceProtocolDialectPropertyShadow(CtrDeviceProtocolDialect.ENCRYPTION_KEY_T_PROPERTY_NAME, "0000000000000001");
        propertyShadows.add(prop);
        prop = new DeviceProtocolDialectPropertyShadow(CtrDeviceProtocolDialect.TIMEOUT_PROPERTY_NAME, new BigDecimal(2000));
        propertyShadows.add(prop);
        return ManagerFactory.getCurrent().getProtocolDialectPropertiesFactory().create(shadow);
    }

    //Note: configured to readout one loadProfile (all channels will belong to the same loadProfile)!
    private void findOrCreateLoadProfiles() {
        if (this.rtu.getLoadProfiles().isEmpty()) {
            List<LoadProfileType> loadProfileTypes = MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().findByName(LOAD_PROFILE_TYPE_NAME);
            if (!loadProfileTypes.isEmpty()) {
                try {
                    LoadProfileShadow loadProfileShadow = new LoadProfileShadow();
                    loadProfileShadow.setLoadProfileTypeId(loadProfileTypes.get(0).getId());
                    Calendar calendar = Calendar.getInstance(rtu.getDeviceTimeZone());
                    calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 2);
                    loadProfileShadow.setLastReading(calendar.getTime());
                    this.rtu.addLoadProfile(loadProfileShadow);

                    LoadProfile loadProfile = this.rtu.getLoadProfiles().get(0);

                    // Add the channels of the rtu to the loadProfile
                    for (int i = 0; i < rtu.getChannels().size(); i++) {
                        Channel channel = rtu.getChannel(i);
                        RtuRegisterMapping rtuRegisterMapping = loadProfile.getLoadProfileType().getRegisterMappings().get(i);
                        loadProfile.addChannel(channel, rtuRegisterMapping);
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
    }

    public Rtu findOrCreateRtu(RtuType rtuType, String name, int intervalInSeconds) throws BusinessException, SQLException {
        Rtu rtu = this.findRtu(name);
        if (rtu == null) {
            rtu = this.createRtu(rtuType, name, intervalInSeconds, MeteringWarehouse.getCurrent().findOrCreateFolder("/ComServer demo"));
        } else {
            System.out.println("Demo Rtu with name " + RTU_NAME + " already existed.");
        }
        RtuShadow shadow = rtu.getShadow();
        TypedProperties properties = rtu.getProperties();
        shadow.setProperties(properties);
        rtu.update(shadow);
        return rtu;
    }

    public Rtu findRtu(String name) {
        List<Rtu> rtus = MeteringWarehouse.getCurrent().getRtuFactory().findByName(name);
        if (rtus.isEmpty()) {
            return null;
        }
        return rtus.get(0);
    }

    public Rtu createRtu(RtuType rtuType, String name, int intervalInSeconds, Folder parent) throws BusinessException, SQLException {
        String serialNumber ="0000000000000000";
        System.out.println("Creating Demo Rtu with name " + RTU_NAME + " and serial number " + serialNumber);
        RtuShadow rtuShadow = rtuType.newRtuShadow();
        rtuShadow.setName(name);
        rtuShadow.setExternalName(name);
        rtuShadow.setIntervalInSeconds(intervalInSeconds);
        rtuShadow.setSerialNumber(serialNumber);
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
                shadow.setType(ComPortType.SERIAL);
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
        comPortShadow.setType(ComPortType.SERIAL);
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
                // ToDo: activate the needed Tasks!
                comTaskShadow.addProtocolTask(getBasicCheckTaskShadow());
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
        ShadowList<RtuRegisterGroupShadow> rtuRegisterGroupShadowList = new ShadowList<RtuRegisterGroupShadow>();
        rtuRegisterGroupShadowList.add(MeteringWarehouse.getCurrent().getRtuRegisterGroupFactory().findByName("Read Group").get(0).getShadow());
        registersTaskShadow.setRtuRegisterGroupShadowList(rtuRegisterGroupShadowList);
        return registersTaskShadow;
    }

    private LoadProfilesTaskShadow getLoadProfilesTaskShadow() {
        LoadProfilesTaskShadow loadProfilesTaskShadow = new LoadProfilesTaskShadow();
        LoadProfileType loadProfileType = findOrCreateLoadProfileType();
        ShadowList<LoadProfileTypeShadow> loadProfileTypeShadowShadowList = new ShadowList<LoadProfileTypeShadow>(Arrays.asList(loadProfileType.getShadow()));
        loadProfilesTaskShadow.setLoadProfileTypeShadows(loadProfileTypeShadowShadowList);
        loadProfilesTaskShadow.setFailIfLoadProfileConfigurationMisMatch(false);
        return loadProfilesTaskShadow;
    }

    //ToDo: WARNING - types are persistent (no auto-clean at the end of demo)! If you change something in this section, please manually clean-up old types (so they get recreated, instead of the old ones being reused)!
    private LoadProfileType findOrCreateLoadProfileType() {
        loadProfileTypes = MeteringWarehouse.getCurrent().getLoadProfileTypeFactory().findByName(LOAD_PROFILE_TYPE_NAME);
        if (loadProfileTypes.isEmpty()) {
            System.out.println("Creating a LoadProfile Type");
            try {
                LoadProfileTypeShadow loadProfileTypeShadow = new LoadProfileTypeShadow();
                loadProfileTypeShadow.setName(LOAD_PROFILE_TYPE_NAME);
                loadProfileTypeShadow.setInterval(new TimeDuration(3600));
                loadProfileTypeShadow.setObisCode(ObisCode.fromString("0.0.99.1.0.255"));

                // Add the different register mappings to the load profile - currently only 1 registerMapping in use.
                RtuRegisterMapping registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Hourly - Qm", "1.0.0, Measured flow (Instantaneous)", false, ObisCode.fromString("7.0.43.0.0.255"), 0, 1);
                loadProfileTypeShadow.addRegisterMappingShadow(registerMapping.getShadow());

                registerMapping = RtuRegisterMappingCRUD.findOrCreateRegisterMapping("Hourly - T", "7.0.0, Metering temperature (Instantaneous)", false, ObisCode.fromString("7.0.41.0.0.255"), 0, 1);
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