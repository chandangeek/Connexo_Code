package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.*;
import com.energyict.mdc.masterdata.*;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;


@Component(name = "com.elster.jupiter.demo", service = {DemoService.class, DemoServiceImpl.class}, property = {"osgi.command.scope=demo", "osgi.command.function=createDemoData"}, immediate = true)
public class DemoServiceImpl implements DemoService {
    public static final String ACTIVE_ENERGY_IMPORT_TARIFF_1_K_WH = "Active Energy Import Tariff 1 (kWh)";
    public static final String ACTIVE_ENERGY_IMPORT_TARIFF_1_WH = "Active Energy Import Tariff 1 (Wh)";
    public static final String ACTIVE_ENERGY_IMPORT_TARIFF_2_K_WH = "Active Energy Import Tariff 2 (kWh)";
    public static final String ACTIVE_ENERGY_IMPORT_TARIFF_2_WH = "Active Energy Import Tariff 2 (Wh)";

    public static final String ACTIVE_ENERGY_EXPORT_TARIFF_1_K_WH = "Active Energy Export Tariff 1 (kWh)";
    public static final String ACTIVE_ENERGY_EXPORT_TARIFF_1_WH = "Active Energy Export Tariff 1 (Wh)";
    public static final String ACTIVE_ENERGY_EXPORT_TARIFF_2_K_WH = "Active Energy Export Tariff 2 (kWh)";
    public static final String ACTIVE_ENERGY_EXPORT_TARIFF_2_WH = "Active Energy Export Tariff 2 (Wh)";

    public static final String ACTIVE_ENERGY_IMPORT_TOTAL_WH = "Active Energy Import Total (Wh)";
    public static final String ACTIVE_ENERGY_EXPORT_TOTAL_WH = "Active Energy Export Total (Wh)";

    public static final String REGISTER_GROUP_DEFAULT_GROUP = "Default group";
    public static final String REGISTER_GROUP_TARIFF_1 = "Tariff 1";
    public static final String REGISTER_GROUP_TARIFF_2 = "Tariff 2";

    public static final String LOAD_PROFILE_TYPE_DAILY_ELECTRICITY = "Daily Electricity";
    public static final String LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY = "Monthly Electricity";
    public static final String LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY = "15min Electricity";

    public static final String LOG_BOOK_TYPES_DEFAULT_LOGBOOK = "Default Logbook";
    public static final String LOG_BOOK_TYPES_POWER_FAILURES = "Power Failures";
    public static final String LOG_BOOK_TYPES_FRAUD_DETECTIONS = "Fraud Detections";

    public static final String COM_TASK_READ_ALL = "ReadAll";
    public static final String COM_TASK_FORCE_CLOCK = "Force Clock";
    public static final String COM_TASK_READ_DAILY = "Read Daily";
    public static final String COM_TASK_TOPOLOGY = "Topology";
    public static final String COM_TASK_READ_REGISTER_DATA = "Read Register data";
    public static final String COM_TASK_READ_LOAD_PROFILE_DATA = "Read LoadProfile data";

    public static final String OUTBOUND_TCP_POOL_NAME = "Outbound TCP Pool";

    private final Boolean rethrowExceptions;
    private volatile EngineModelService engineModelService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile MasterDataService masterDataService;
    private volatile MeteringService meteringService;
    private volatile TaskService taskService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceDataService deviceDataService;

    private int deviceTypeCount = 0;

    public DemoServiceImpl() {
        rethrowExceptions = Boolean.FALSE;
    }

    @Inject
    public DemoServiceImpl(
            EngineModelService engineModelService,
            TransactionService transactionService,
            ThreadPrincipalService threadPrincipalService,
            ProtocolPluggableService protocolPluggableService,
            MasterDataService masterDataService,
            MeteringService meteringService,
            TaskService taskService,
            DeviceConfigurationService deviceConfigurationService,
            DeviceDataService deviceDataService) {
        this.engineModelService = engineModelService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.protocolPluggableService = protocolPluggableService;
        this.masterDataService = masterDataService;
        this.meteringService = meteringService;
        this.taskService = taskService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceDataService = deviceDataService;
        rethrowExceptions = Boolean.TRUE;
    }

    @Override
    public void createDemoData(final String comServerName, final String host) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Store store = new Store();

                OnlineComServer comServer = createComServer("Deitvs099");
                OutboundComPort outboundTCPPort = createOutboundTcpComPort("Outbound TCP 099", comServer);
                OutboundComPortPool outboundTcpComPortPool = createOutboundTcpComPortPool("Outbound TCP Pool 099", outboundTCPPort);
                InboundComPortPool inboundServletComPortPool = createInboundServletComPortPool("Inbound Servlet Pool 099");
                createInboundServletPort("Inbound Servlet 099", 4444, comServer, inboundServletComPortPool);

                comServer = createComServer(comServerName);
                outboundTCPPort = createOutboundTcpComPort("Outbound TCP", comServer);
                store.getOutboundComPortPools().put(OUTBOUND_TCP_POOL_NAME,createOutboundTcpComPortPool(OUTBOUND_TCP_POOL_NAME, outboundTCPPort));
                inboundServletComPortPool = createInboundServletComPortPool("Inbound Servlet Pool");
                createInboundServletPort("Inbound Servlet", 4444, comServer, inboundServletComPortPool);

                findRegisterTypes(store);
                createLoadProfiles(store);
                createRegisterGroups(store);
                createLogbookTypes(store);
                createCommunicationTasks(store);
                createDeviceTypes(store);
            }
        });
    }

    private OnlineComServer createComServer(String name) {
        System.out.println("==> Creating ComServer '" + name + "' ...");
        OnlineComServer comServer = engineModelService.newOnlineComServerInstance();
        comServer.setName(name);
        comServer.setActive(true);
        comServer.setServerLogLevel(ComServer.LogLevel.INFO);
        comServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        comServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.MINUTES));
        comServer.setSchedulingInterPollDelay(new TimeDuration(60, TimeDuration.SECONDS));
        comServer.setStoreTaskQueueSize(50);
        comServer.setNumberOfStoreTaskThreads(1);
        comServer.setStoreTaskThreadPriority(5);
        comServer.save();
        return comServer;
    }

    private OutboundComPort createOutboundTcpComPort(String name, ComServer comServer) {
        System.out.println("==> Creating Outbound TCP Port '" + name + "'...");
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = comServer.newOutboundComPort(name, 5);
        outboundComPortBuilder.comPortType(ComPortType.TCP).active(true);
        OutboundComPort comPort = outboundComPortBuilder.add();
        comPort.save();
        return comPort;
    }

    private OutboundComPortPool createOutboundTcpComPortPool(String name, OutboundComPort... comPorts) {
        System.out.println("==> Creating Outbound TCP Port Pool '" + name + "'...");
        OutboundComPortPool outboundComPortPool = engineModelService.newOutboundComPortPool();
        outboundComPortPool.setActive(true);
        outboundComPortPool.setComPortType(ComPortType.TCP);
        outboundComPortPool.setName(name);
        outboundComPortPool.setTaskExecutionTimeout(new TimeDuration(0, TimeDuration.SECONDS));
        if (comPorts != null) {
            for (OutboundComPort comPort : comPorts) {
                outboundComPortPool.addOutboundComPort(comPort);
            }
        }
        outboundComPortPool.save();
        return outboundComPortPool;
    }

    private InboundComPortPool createInboundServletComPortPool(String name) {
        System.out.println("==> Creating Inbound Servlet Port Pool '" + name + "'...");
        InboundDeviceProtocolPluggableClass protocolPluggableClass = protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(DlmsSerialNumberDiscover.class.getName()).get(0);
        InboundComPortPool inboundComPortPool = engineModelService.newInboundComPortPool();
        inboundComPortPool.setActive(true);
        inboundComPortPool.setComPortType(ComPortType.SERVLET);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(protocolPluggableClass);
        inboundComPortPool.setName(name);
        inboundComPortPool.save();
        return inboundComPortPool;

    }

    private ServletBasedInboundComPort createInboundServletPort(String name, int portNumber, ComServer comServer, InboundComPortPool comPortPool) {
        System.out.println("==> Creating Inbound Servlet Port '" + name + "'...");
        ServletBasedInboundComPort.ServletBasedInboundComPortBuilder comPortBuilder = comServer.newServletBasedInboundComPort(name, "context", 10, portNumber);
        comPortBuilder.active(true).comPortPool(comPortPool).keyStoreSpecsFilePath("");
        ServletBasedInboundComPort comPort = comPortBuilder.add();
        comPort.save();
        return comPort;

    }

    private void findRegisterTypes(Store store) {
        System.out.println("==> Finding Register Types...");
        store.getRegisterTypes().put(ACTIVE_ENERGY_IMPORT_TARIFF_1_K_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_IMPORT_TARIFF_2_K_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_EXPORT_TARIFF_1_K_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_EXPORT_TARIFF_2_K_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_IMPORT_TOTAL_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"));
        store.getRegisterTypes().put(ACTIVE_ENERGY_EXPORT_TOTAL_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"));
    }

    private RegisterType findRegisterType(String mRid) {
        return masterDataService.findRegisterTypeByReadingType(meteringService.getReadingType(mRid).get()).get();
    }

    private void createLoadProfiles(Store store) {
        System.out.println("==> Creating Load Profiles Types...");

        LoadProfileType dailyElectrisity = createLoadProfile(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY, "1.0.99.2.0.255", new TimeDuration(1, TimeDuration.DAYS));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        dailyElectrisity.save();
        store.getLoadProfileTypes().put(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY, dailyElectrisity);

        LoadProfileType monthlyElectricity = createLoadProfile(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY, "0.0.98.1.0.255", new TimeDuration(1, TimeDuration.MONTHS));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        monthlyElectricity.save();
        store.getLoadProfileTypes().put(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY, monthlyElectricity);

        LoadProfileType _15minElectricity = createLoadProfile(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY, "1.0.99.1.0.255", new TimeDuration(15, TimeDuration.MINUTES));
        _15minElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TOTAL_WH));
        _15minElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TOTAL_WH));
        _15minElectricity.save();
        store.getLoadProfileTypes().put(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY, _15minElectricity);
    }

    private LoadProfileType createLoadProfile(String name, String obisCode, TimeDuration duartion) {
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(name, ObisCode.fromString(obisCode), duartion);
        loadProfileType.save();
        return loadProfileType;
    }

    private void createRegisterGroups(Store store) {
        System.out.println("==> Creating Register Groups...");

        RegisterGroup defaultRegisterGroup = masterDataService.newRegisterGroup(REGISTER_GROUP_DEFAULT_GROUP);
        defaultRegisterGroup.save();
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TOTAL_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TOTAL_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        store.getRegisterGroups().put(REGISTER_GROUP_DEFAULT_GROUP, defaultRegisterGroup);

        RegisterGroup tariff1 = masterDataService.newRegisterGroup(REGISTER_GROUP_TARIFF_1);
        tariff1.save();
        tariff1.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        tariff1.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        store.getRegisterGroups().put(REGISTER_GROUP_TARIFF_1, tariff1);

        RegisterGroup tariff2 = masterDataService.newRegisterGroup(REGISTER_GROUP_TARIFF_2);
        tariff2.save();
        tariff2.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        tariff2.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        store.getRegisterGroups().put(REGISTER_GROUP_TARIFF_2, tariff2);
    }

    private void createLogbookTypes(Store store) {
        System.out.println("==> Creating Log Book Types...");
        createLogBookType(store, LOG_BOOK_TYPES_DEFAULT_LOGBOOK, "0.0.99.98.0.255");
        createLogBookType(store, LOG_BOOK_TYPES_POWER_FAILURES, "1.0.99.97.0.255");
        createLogBookType(store, LOG_BOOK_TYPES_FRAUD_DETECTIONS, "0.0.99.98.1.255");
    }

    private void createLogBookType(Store store, String logBookTypeName, String obisCode) {
        LogBookType logBookType = masterDataService.newLogBookType(logBookTypeName, ObisCode.fromString(obisCode));
        logBookType.save();
        store.getLogBookTypes().put(logBookTypeName, logBookType);
    }

    private void createCommunicationTasks(Store store) {
        System.out.println("==> Creating Communication Tasks...");

        ComTask readAll = taskService.newComTask(COM_TASK_READ_ALL);
        readAll.createLoadProfilesTask().loadProfileTypes(new ArrayList<LoadProfileType>(store.getLoadProfileTypes().values())).add();
        readAll.createRegistersTask().registerGroups(Collections.singletonList(store.getRegisterGroups().get(REGISTER_GROUP_DEFAULT_GROUP))).add();
        readAll.createLogbooksTask().logBookTypes(new ArrayList<LogBookType>(store.getLogBookTypes().values())).add();
        readAll.save();
        store.getComTasks().put(COM_TASK_READ_ALL, readAll);

        ComTask forceClock = taskService.newComTask(COM_TASK_FORCE_CLOCK);
        forceClock.createClockTask(ClockTaskType.FORCECLOCK).add();
        forceClock.save();
        store.getComTasks().put(COM_TASK_FORCE_CLOCK, forceClock);

        ComTask readDaily = taskService.newComTask(COM_TASK_READ_DAILY);
        readDaily.createClockTask(ClockTaskType.SETCLOCK)
                .minimumClockDifference(new TimeDuration(5, TimeDuration.SECONDS))
                .maximumClockDifference(new TimeDuration(5, TimeDuration.MINUTES)).add();
        LoadProfileType[] loadProfileTypesForReadDayly = {store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY), store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY)};
        readDaily.createLoadProfilesTask().loadProfileTypes(Arrays.asList(loadProfileTypesForReadDayly)).add();
        readDaily.createLogbooksTask().logBookTypes(Collections.singletonList(store.getLogBookTypes().get(LOG_BOOK_TYPES_DEFAULT_LOGBOOK))).add();
        RegisterGroup[] registerGroupsForReadDayly = {store.getRegisterGroups().get(REGISTER_GROUP_TARIFF_1), store.getRegisterGroups().get(REGISTER_GROUP_TARIFF_2)};
        readDaily.createRegistersTask().registerGroups(Arrays.asList(registerGroupsForReadDayly)).add();
        readDaily.save();
        store.getComTasks().put(COM_TASK_READ_DAILY, readDaily);

        ComTask topology = taskService.newComTask(COM_TASK_TOPOLOGY);
        topology.createTopologyTask(TopologyAction.VERIFY);
        topology.save();
        store.getComTasks().put(COM_TASK_TOPOLOGY, topology);

        ComTask readRegisterData = taskService.newComTask(COM_TASK_READ_REGISTER_DATA);
        readRegisterData.createRegistersTask().registerGroups(Collections.singletonList(store.getRegisterGroups().get(REGISTER_GROUP_DEFAULT_GROUP))).add();
        readRegisterData.save();
        store.getComTasks().put(COM_TASK_READ_REGISTER_DATA, readRegisterData);

        ComTask readLoadProfileData = taskService.newComTask(COM_TASK_READ_LOAD_PROFILE_DATA);
        readLoadProfileData.createLoadProfilesTask().loadProfileTypes(new ArrayList<LoadProfileType>(store.getLoadProfileTypes().values())).add();
        readLoadProfileData.save();
        store.getComTasks().put(COM_TASK_READ_LOAD_PROFILE_DATA, readLoadProfileData);
    }

    public void createDeviceTypes(Store store) {
        for (int i=0; i < 6; i++){
            createDeviceType(store);
        }
    }

    public void createDeviceType(Store store) {
        System.out.println("==> Creating Create device types...");
        DeviceProtocolPluggableClass webRTUprotocol = protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(WebRTUKP.class.getName()).get(0);
        DeviceType deviceType = deviceConfigurationService.newDeviceType("Elster AMR " + String.format("%03d", ++deviceTypeCount), webRTUprotocol);
        deviceType.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TOTAL_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TOTAL_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY));

        deviceType.addLogBookType(store.getLogBookTypes().get(LOG_BOOK_TYPES_DEFAULT_LOGBOOK));
        deviceType.addLogBookType(store.getLogBookTypes().get(LOG_BOOK_TYPES_FRAUD_DETECTIONS));
        deviceType.addLogBookType(store.getLogBookTypes().get(LOG_BOOK_TYPES_POWER_FAILURES));
        deviceType.save();

        createSimpleDeviceConfiguration(store, deviceType);
        createExtendedDeviceConfiguration(store, deviceType);
    }

    private void createExtendedDeviceConfiguration(Store store, DeviceType deviceType) {
        System.out.println("==> Creating Extended Device Configuration...");
        DeviceType.DeviceConfigurationBuilder configBuilder = deviceType.newConfiguration("Extended Config");
        configBuilder.description("A complex configuration that is closely matched to the DSMR 2.3 Devices");
        configBuilder.canActAsGateway(true);
        configBuilder.isDirectlyAddressable(true);

        addRegisterSpecsToDeviceConfiguration(configBuilder, store,
                ACTIVE_ENERGY_IMPORT_TOTAL_WH,
                ACTIVE_ENERGY_IMPORT_TARIFF_1_WH,
                ACTIVE_ENERGY_IMPORT_TARIFF_2_WH,
                ACTIVE_ENERGY_EXPORT_TOTAL_WH,
                ACTIVE_ENERGY_EXPORT_TARIFF_1_WH,
                ACTIVE_ENERGY_EXPORT_TARIFF_2_WH);

        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(LOG_BOOK_TYPES_DEFAULT_LOGBOOK));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(LOG_BOOK_TYPES_POWER_FAILURES));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(LOG_BOOK_TYPES_FRAUD_DETECTIONS));
        DeviceConfiguration configuration = configBuilder.add();

        addConnectionMethodToDeviceConfiguration(store, configuration);
        createSecurityPropertySetForDeviceConfiguration(configuration);
        setProtocolDialectConfigurationProperties(configuration);
        enableComTasksOnDeviceConfiguration(configuration, store, COM_TASK_READ_DAILY, COM_TASK_TOPOLOGY, COM_TASK_READ_REGISTER_DATA);
        configureChannelsForLoadProfileSpec(configuration);
        configuration.activate();
        configuration.save();
        createDevicesForDeviceConfiguration(configuration);
    }

    /**
     * We expect that required security property set is the first element in collection
     */
    public void enableComTasksOnDeviceConfiguration(DeviceConfiguration configuration, Store store, String... names){
        if (names != null) {
            for (String name : names) {
                configuration.enableComTask(store.getComTasks().get(name), configuration.getSecurityPropertySets().get(0))
                    .setIgnoreNextExecutionSpecsForInbound(true)
                    .setPriority(100)
                    .setProtocolDialectConfigurationProperties(configuration.getProtocolDialectConfigurationPropertiesList().get(0)).add().save();
            }
        }
    }

    private ProtocolDialectConfigurationProperties setProtocolDialectConfigurationProperties(DeviceConfiguration configuration) {
        ProtocolDialectConfigurationProperties configurationProperties = configuration.getProtocolDialectConfigurationPropertiesList().get(0);
        configurationProperties.setProperty("NTASimulationTool", "1");
        configurationProperties.save();
        return configurationProperties;
    }

    private void addRegisterSpecsToDeviceConfiguration(DeviceType.DeviceConfigurationBuilder builder, Store store, String... registerTypesNames){
        if (registerTypesNames != null){
            for (String registerTypesName : registerTypesNames) {
                addRegisterSpecToDeviceConfiguration(builder, store.getRegisterTypes().get(registerTypesName));
            }
        }
    }

    private void addRegisterSpecToDeviceConfiguration(DeviceType.DeviceConfigurationBuilder builder, RegisterType registerType) {
        builder.newNumericalRegisterSpec(registerType)
                .setOverflowValue(new BigDecimal(99999999))
                .setNumberOfDigits(8)
                .setMultiplier(new BigDecimal(1))
                .setNumberOfFractionDigits(0).add();
    }

    private void createSimpleDeviceConfiguration(Store store, DeviceType deviceType) {
        System.out.println("==> Creating Simple Device Configuration...");
        DeviceType.DeviceConfigurationBuilder configBuilder = deviceType.newConfiguration("Default");
        configBuilder.description("A simple device configuration which contains one LoadProfile and a minimal set of Registers.");
        configBuilder.canActAsGateway(true);
        configBuilder.isDirectlyAddressable(true);
        addRegisterSpecsToDeviceConfiguration(configBuilder, store, ACTIVE_ENERGY_IMPORT_TOTAL_WH, ACTIVE_ENERGY_EXPORT_TOTAL_WH);
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(LOG_BOOK_TYPES_DEFAULT_LOGBOOK));
        DeviceConfiguration configuration = configBuilder.add();

        addConnectionMethodToDeviceConfiguration(store, configuration);

        createSecurityPropertySetForDeviceConfiguration(configuration);
        setProtocolDialectConfigurationProperties(configuration);
        enableComTasksOnDeviceConfiguration(configuration, store, COM_TASK_READ_ALL);
        configureChannelsForLoadProfileSpec(configuration);
        configuration.activate();
        configuration.save();
        createDevicesForDeviceConfiguration(configuration);
    }

    private SecurityPropertySet createSecurityPropertySetForDeviceConfiguration(DeviceConfiguration configuration) {
        SecurityPropertySet securityPropertySet = configuration.createSecurityPropertySet("No security").authenticationLevel(0).encryptionLevel(0).build();
        securityPropertySet.update();
        return securityPropertySet;
    }

    private void addConnectionMethodToDeviceConfiguration(Store store, DeviceConfiguration configuration) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp");
        configuration.getCommunicationConfiguration()
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(60, TimeDuration.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(store.getOutboundComPortPools().get(OUTBOUND_TCP_POOL_NAME))
                .addProperty("host", "deitvs015")
                .addProperty("portNumber", new BigDecimal(4059))
                .asDefault(true).build();
    }

    private void configureChannelsForLoadProfileSpec(DeviceConfiguration devConfiguration) {
        for (LoadProfileSpec loadProfileSpec : devConfiguration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            for (ChannelType channelType : availableChannelTypes) {
                devConfiguration.createChannelSpec(channelType, channelType.getPhenomenon(), loadProfileSpec).setMultiplier(new BigDecimal(1)).setOverflow(new BigDecimal(1)).add();
            }
        }
    }

    private void createDevicesForDeviceConfiguration(DeviceConfiguration configuration){
        System.out.println("==> Creating Devices for Configuration...");
        for (int i = 1; i < 9; i++) {
            createDevice(configuration, "ZABF0100" + String.format("%04d", configuration.getId()) + String.format("%04d", i) );
        }
    }

    private void createDevice(DeviceConfiguration configuration, String mrid){
        System.out.println("==> Creating Device '" + mrid + "'...");
        Calendar calendar = Calendar.getInstance();
        Device newDevice = deviceDataService.newDevice(configuration, mrid, mrid);
        calendar.set(2014, 1, 1);
        newDevice.setYearOfCertification(calendar.getTime());
        newDevice.save();
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    private <T> T executeTransaction(Transaction<T> transaction) {
        setPrincipal();
        try {
            T result = transactionService.execute(transaction);
            System.out.println("==> Success");
            return result;
        } catch (Exception ex) {
            System.out.println("==> Fail");
            ex.printStackTrace();
            if (rethrowExceptions) {
                throw ex;
            }
            return null;
        } finally {
            clearPrincipal();
        }
    }

    private int getRandomInt(int min, int max){
        return (int) (min + Math.random() * (max - min));
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        };
    }
}
