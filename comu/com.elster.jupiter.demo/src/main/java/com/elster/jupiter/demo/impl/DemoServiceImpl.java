package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.engine.model.*;
import com.energyict.mdc.masterdata.*;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
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

    private volatile EngineModelService engineModelService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile MasterDataService masterDataService;
    private volatile MeteringService meteringService;
    private volatile TaskService taskService;

    private final Boolean rethrowExceptions;

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
            TaskService taskService) {
        this.engineModelService = engineModelService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.protocolPluggableService = protocolPluggableService;
        this.masterDataService = masterDataService;
        this.meteringService = meteringService;
        this.taskService = taskService;
        rethrowExceptions = Boolean.TRUE;
    }

    public void createDemoData() {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Map<String, RegisterType> registerTypes = new HashMap<String, RegisterType>();
                Map<String, RegisterGroup> registerGroups = new HashMap<String, RegisterGroup>();
                Map<String, LoadProfileType> loadProfileTypes = new HashMap<String, LoadProfileType>();
                Map<String, LogBookType> logBookTypes = new HashMap<String, LogBookType>();

                OnlineComServer comServer = createComServer("deitvs015");
                OutboundComPort outboundTCPPort = createOutboundTcpComPort("DefaultActiveOutboundTCPPort", comServer);
                createOutboundTcpComPortPool("DefaultActiveComPortPool", outboundTCPPort);
                InboundComPortPool inboundServletComPortPool = createInboundServletComPortPool("DefaultInboundServletComPortPool");
                createInboundServletPort("DefaultActiveInboundServletPort", 4444, comServer, inboundServletComPortPool);
                createRegisterTypes(registerTypes);
                createLoadProfiles(registerTypes, loadProfileTypes);
                createRegisterGroups(registerTypes, registerGroups);
                createLogbookTypes(logBookTypes);
                createCommunicationTasks(registerGroups, loadProfileTypes, logBookTypes);

                throw new IllegalArgumentException("stopper");
            }
        });
    }

    private OnlineComServer createComServer(String name) {
        System.out.println("==> Creating ComServer...");
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
        System.out.println("==> Creating Outbound TCP Port...");
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = comServer.newOutboundComPort(name, 5);
        outboundComPortBuilder.comPortType(ComPortType.TCP).active(true);
        OutboundComPort comPort = outboundComPortBuilder.add();
        comPort.save();
        return comPort;
    }

    private OutboundComPortPool createOutboundTcpComPortPool(String name, OutboundComPort... comPorts) {
        System.out.println("==> Creating Outbound TCP Port Pool...");
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
        System.out.println("==> Creating Inbound Servlet Port Pool...");
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
        System.out.println("==> Creating Inbound Servlet Port...");
        ServletBasedInboundComPort.ServletBasedInboundComPortBuilder comPortBuilder = comServer.newServletBasedInboundComPort(name, "context", 10, portNumber);
        comPortBuilder.active(true).comPortPool(comPortPool).keyStoreSpecsFilePath("");
        ServletBasedInboundComPort comPort = comPortBuilder.add();
        comPort.save();
        return comPort;

    }

    private void createRegisterTypes(Map<String, RegisterType> registerTypes){
        /*
        there is no API for creating ReadingType, SQL script for now
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0', 'Active Energy Import Tariff 1 (kWh)', null, '1', '0', '0', 'Jupiter Installer');
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0', 'Active Energy Import Tariff 1 (Wh)', null, '1', '0', '0', 'Jupiter Installer');
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0', 'Active Energy Import Tariff 2 (kWh)', null, '1', '0', '0', 'Jupiter Installer');
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0', 'Active Energy Import Tariff 2 (Wh)', null, '1', '0', '0', 'Jupiter Installer');
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0', 'Active Energy Export Tariff 1 (kWh)', null, '1', '0', '0', 'Jupiter Installer');
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0', 'Active Energy Export Tariff 1 (Wh)', null, '1', '0', '0', 'Jupiter Installer');
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0', 'Active Energy Export Tariff 2 (kWh)', null, '1', '0', '0', 'Jupiter Installer');
        INSERT INTO "MTR_READINGTYPE" VALUES ('0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0', 'Active Energy Export Tariff 2 (Wh)', null, '1', '0', '0', 'Jupiter Installer');
        */

        System.out.println("==> Creating Register Types...");
        registerTypes.put(ACTIVE_ENERGY_IMPORT_TARIFF_1_K_WH, createRegisterType(ACTIVE_ENERGY_IMPORT_TARIFF_1_K_WH, "1.0.1.8.1.255", "kWh", "0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        registerTypes.put(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH, createRegisterType(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH, "1.0.1.8.1.255", "Wh", "0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        registerTypes.put(ACTIVE_ENERGY_IMPORT_TARIFF_2_K_WH, createRegisterType(ACTIVE_ENERGY_IMPORT_TARIFF_2_K_WH, "1.0.1.8.2.255", "kWh", "0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        registerTypes.put(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH, createRegisterType(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH, "1.0.1.8.2.255", "Wh", "0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        registerTypes.put(ACTIVE_ENERGY_EXPORT_TARIFF_1_K_WH, createRegisterType(ACTIVE_ENERGY_EXPORT_TARIFF_1_K_WH, "1.0.1.8.1.255", "kWh", "0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        registerTypes.put(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH, createRegisterType(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH, "1.0.1.8.1.255", "Wh", "0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        registerTypes.put(ACTIVE_ENERGY_EXPORT_TARIFF_2_K_WH, createRegisterType(ACTIVE_ENERGY_EXPORT_TARIFF_2_K_WH, "1.0.1.8.2.255", "kWh", "0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        registerTypes.put(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH, createRegisterType(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH, "1.0.1.8.2.255", "Wh", "0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        registerTypes.put(ACTIVE_ENERGY_IMPORT_TOTAL_WH, createRegisterType(ACTIVE_ENERGY_IMPORT_TOTAL_WH, "1.0.1.8.0.255", "Wh", "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"));
        registerTypes.put(ACTIVE_ENERGY_EXPORT_TOTAL_WH, createRegisterType(ACTIVE_ENERGY_EXPORT_TOTAL_WH, "1.0.2.8.0.255", "Wh", "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"));
    }

    private RegisterType createRegisterType(String name, String obisCode, String unit, String readingType){
        Optional<ReadingType> readingTypeRef = meteringService.getReadingType(readingType);
        if (!readingTypeRef.isPresent()){
            System.out.println("==> No such reading type"); // how to create?
        }
        ReadingType readingTypeObj = readingTypeRef.get();
        RegisterType registerType = masterDataService.newRegisterType(name, ObisCode.fromString(obisCode), Unit.get(unit), readingTypeObj, readingTypeObj.getTou());
        registerType.save();
        return registerType;
    }

    private void createLoadProfiles(Map<String, RegisterType> registerTypes, Map<String, LoadProfileType> loadProfileTypes){
        System.out.println("==> Creating Load Profiles Types...");

        LoadProfileType dailyElectrisity = createLoadProfile(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY, "1.0.99.2.0.255", new TimeDuration(1, TimeDuration.DAYS));
        dailyElectrisity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        dailyElectrisity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        dailyElectrisity.save();
        loadProfileTypes.put(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY, dailyElectrisity);

        LoadProfileType monthlyElectricity = createLoadProfile(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY, "0.0.98.1.0.255", new TimeDuration(1, TimeDuration.MONTHS));
        monthlyElectricity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        monthlyElectricity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        monthlyElectricity.save();
        loadProfileTypes.put(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY, monthlyElectricity);

        LoadProfileType _15minElectricity = createLoadProfile(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY, "1.0.99.1.0.255", new TimeDuration(15, TimeDuration.MINUTES));
        _15minElectricity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TOTAL_WH));
        _15minElectricity.createChannelTypeForRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TOTAL_WH));
        _15minElectricity.save();
        loadProfileTypes.put(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY, _15minElectricity);
    }

    private LoadProfileType createLoadProfile(String name, String obisCode, TimeDuration duartion){
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(name, ObisCode.fromString(obisCode), duartion);
        loadProfileType.save();
        return loadProfileType;
    }

    private Map<String, RegisterGroup> createRegisterGroups(Map<String, RegisterType> registerTypes, Map<String, RegisterGroup> registerGroups){
        System.out.println("==> Creating Register Groups...");

        RegisterGroup defaultRegisterGroup = masterDataService.newRegisterGroup(REGISTER_GROUP_DEFAULT_GROUP);
        defaultRegisterGroup.addRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TOTAL_WH));
        defaultRegisterGroup.addRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TOTAL_WH));
        defaultRegisterGroup.addRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        defaultRegisterGroup.addRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        defaultRegisterGroup.addRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        defaultRegisterGroup.addRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        defaultRegisterGroup.save();
        registerGroups.put(REGISTER_GROUP_DEFAULT_GROUP, defaultRegisterGroup);

        RegisterGroup tariff1 = masterDataService.newRegisterGroup(REGISTER_GROUP_TARIFF_1);
        tariff1.addRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_1_WH));
        tariff1.addRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_1_WH));
        tariff1.save();
        registerGroups.put(REGISTER_GROUP_TARIFF_1, tariff1);

        RegisterGroup tariff2 = masterDataService.newRegisterGroup(REGISTER_GROUP_TARIFF_2);
        tariff1.addRegisterType(registerTypes.get(ACTIVE_ENERGY_IMPORT_TARIFF_2_WH));
        tariff1.addRegisterType(registerTypes.get(ACTIVE_ENERGY_EXPORT_TARIFF_2_WH));
        tariff2.save();
        registerGroups.put(REGISTER_GROUP_TARIFF_2, tariff2);
        return registerGroups;
    }

    private void createLogbookTypes(Map<String, LogBookType> logBookTypes){
        System.out.println("==> Creating Log Book Types...");
        LogBookType defaultLogBook = masterDataService.newLogBookType(LOG_BOOK_TYPES_DEFAULT_LOGBOOK, ObisCode.fromString("0.0.99.98.0.255"));
        defaultLogBook.save();
        logBookTypes.put(LOG_BOOK_TYPES_DEFAULT_LOGBOOK, defaultLogBook);

        LogBookType powerFailures = masterDataService.newLogBookType(LOG_BOOK_TYPES_POWER_FAILURES, ObisCode.fromString("1.0.99.97.0.255"));
        powerFailures.save();
        logBookTypes.put(LOG_BOOK_TYPES_POWER_FAILURES, powerFailures);

        LogBookType fraudDetection = masterDataService.newLogBookType(LOG_BOOK_TYPES_FRAUD_DETECTIONS, ObisCode.fromString("0.0.99.98.1.255"));
        fraudDetection.save();
        logBookTypes.put(LOG_BOOK_TYPES_FRAUD_DETECTIONS, fraudDetection);
    }

    private void createCommunicationTasks(Map<String, RegisterGroup> registerGroups, Map<String, LoadProfileType> loadProfileTypes, Map<String, LogBookType> logBookTypes){
        System.out.println("==> Creating Communication Tasks...");

        ComTask readAll = taskService.newComTask("ReadAll");
        readAll.createLoadProfilesTask().loadProfileTypes(new ArrayList<LoadProfileType>(loadProfileTypes.values())).add();
        readAll.createRegistersTask().registerGroups(Collections.singletonList(registerGroups.get(REGISTER_GROUP_DEFAULT_GROUP))).add();
        readAll.createLogbooksTask().logBookTypes(new ArrayList<LogBookType>(logBookTypes.values())).add();
        readAll.save();

        ComTask forceClock = taskService.newComTask("Force Clock");
        forceClock.createClockTask(ClockTaskType.FORCECLOCK).add();
        forceClock.save();

        ComTask readDaily = taskService.newComTask("Read Daily");
        readDaily.createClockTask(ClockTaskType.SETCLOCK)
                .minimumClockDifference(new TimeDuration(5, TimeDuration.SECONDS))
                .maximumClockDifference(new TimeDuration(5, TimeDuration.MINUTES)).add();
        LoadProfileType[] loadProfileTypesForReadDayly = {loadProfileTypes.get(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY), loadProfileTypes.get(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY)};
        readDaily.createLoadProfilesTask().loadProfileTypes(Arrays.asList(loadProfileTypesForReadDayly)).add();
        readDaily.createLogbooksTask().logBookTypes(Collections.singletonList(logBookTypes.get(LOG_BOOK_TYPES_DEFAULT_LOGBOOK))).add();
        RegisterGroup[] registerGroupsForReadDayly = {registerGroups.get(REGISTER_GROUP_TARIFF_1), registerGroups.get(REGISTER_GROUP_TARIFF_2)};
        readDaily.createRegistersTask().registerGroups(Arrays.asList(registerGroupsForReadDayly)).add();
        readDaily.save();

        ComTask topology = taskService.newComTask("Topology");
        topology.createTopologyTask(TopologyAction.VERIFY).save();
        topology.save();

        ComTask readRegisterData = taskService.newComTask("Read Register data");
        readRegisterData.createRegistersTask().registerGroups(Collections.singletonList(registerGroups.get(REGISTER_GROUP_DEFAULT_GROUP))).add();
        readRegisterData.save();

        ComTask readLoadProfileData = taskService.newComTask("Read LoadProfile data");
        readLoadProfileData.createLoadProfilesTask().loadProfileTypes(new ArrayList<LoadProfileType>(loadProfileTypes.values())).add();
        readLoadProfileData.save();
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

    private <T> T executeTransaction(Transaction<T> transaction) {
        setPrincipal();
        try {
            T result = transactionService.execute(transaction);
            System.out.println("==> Success");
            return result;
        } catch (Exception ex) {
            System.out.println("==> Fail");
            ex.printStackTrace();
            if (rethrowExceptions){
                throw ex;
            }
            return null;
        } finally {
            clearPrincipal();
        }
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
