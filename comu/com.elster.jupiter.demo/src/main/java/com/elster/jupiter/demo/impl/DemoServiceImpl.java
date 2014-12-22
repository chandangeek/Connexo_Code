package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.demo.impl.generators.ComServerGenerator;
import com.elster.jupiter.demo.impl.generators.DeviceGenerator;
import com.elster.jupiter.demo.impl.generators.DeviceGroupGenerator;
import com.elster.jupiter.demo.impl.generators.DynamicKpiGenerator;
import com.elster.jupiter.demo.impl.generators.IssueCommentGenerator;
import com.elster.jupiter.demo.impl.generators.IssueGenerator;
import com.elster.jupiter.demo.impl.generators.IssueReasonGenerator;
import com.elster.jupiter.demo.impl.generators.IssueRuleGenerator;
import com.elster.jupiter.demo.impl.generators.OutboundTCPComPortGenerator;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component(name = "com.elster.jupiter.demo", service = {DemoService.class, DemoServiceImpl.class}, property = {"osgi.command.scope=demo", "osgi.command.function=createDemoData", "osgi.command.function=createDemoUsers", "osgi.command.function=createValidationRules"}, immediate = true)
public class DemoServiceImpl implements DemoService {
    public static final String BULK_A_FORWARD_ALL_PHASES_TOU_1_K_WH = "Active Energy Import Tariff 1 (kWh)";
    public static final String BULK_A_FORWARD_ALL_PHASES_TOU_1_WH = "Active Energy Import Tariff 1 (Wh)";
    public static final String BULK_A_FORWARD_ALL_PHASES_TOU_2_K_WH = "Active Energy Import Tariff 2 (kWh)";
    public static final String BULK_A_FORWARD_ALL_PHASES_TOU_2_WH = "Active Energy Import Tariff 2 (Wh)";

    public static final String BULK_A_MINUS_ALL_PHASES_TOU_1_K_WH = "Active Energy Export Tariff 1 (kWh)";
    public static final String BULK_A_REVERSE_ALL_PHASES_TOU_1_WH = "Active Energy Export Tariff 1 (Wh)";
    public static final String BULK_A_REVERSE_ALL_PHASES_TOU_2_K_WH = "Active Energy Export Tariff 2 (kWh)";
    public static final String BULK_A_REVERSE_ALL_PHASES_TOU_2_WH = "Active Energy Export Tariff 2 (Wh)";

    public static final String BULK_A_FORWARD_ALL_PHASES_TOU_0_WH = "Active Energy Import Total (Wh)";
    public static final String BULK_A_REVERSE_ALL_PHASES_TOU_0_WH = "Active Energy Export Total (Wh)";

    public static final String ALARM_REGISTER = "Alarm register";
    public static final String AMR_PROFILE_STATUS_CODE = "AMR Profile status code";
    public static final String ACTIVE_FIRMWARE_VERSION = "Active firmware version";

    public static final String REGISTER_GROUP_DEFAULT_GROUP = "Default group";
    public static final String REGISTER_GROUP_TARIFF_1 = "Tariff 1";
    public static final String REGISTER_GROUP_TARIFF_2 = "Tariff 2";
    public static final String REGISTER_GROUP_DEVICE_DATA = "Device data";

    public static final String LOAD_PROFILE_TYPE_DAILY_ELECTRICITY = "Daily Electricity";
    public static final String LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY = "Monthly Electricity";
    public static final String LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY = "15min Electricity";

    public static final String LOG_BOOK_TYPES_DEFAULT_LOGBOOK = "Default Logbook";
    public static final String LOG_BOOK_TYPES_POWER_FAILURES = "Power Failures";
    public static final String LOG_BOOK_TYPES_FRAUD_DETECTIONS = "Fraud Detections";

    public static final String COM_TASK_READ_ALL = "Read all";
    public static final String COM_TASK_FORCE_CLOCK = "Force clock";
    public static final String COM_TASK_READ_DAILY = "Read daily";
    public static final String COM_TASK_TOPOLOGY = "Topology";
    public static final String COM_TASK_READ_REGISTER_BILLING_DATA = "Read register billing data";
    public static final String COM_TASK_READ_LOAD_PROFILE_DATA = "Read load profile data";

    public static final String OUTBOUND_TCP_POOL_NAME = "Outbound TCP Pool";

    public static final String VODAFONE_TCP_POOL_NAME = "Vodafone";
    public static final String ORANGE_TCP_POOL_NAME = "Orange";

    public static final String COM_SCHEDULE_DAILY_READ_ALL = "Daily read all";
    public static final String COM_SCHEDULE_MOUNTHLY_BILLING_DATA = "Monthly billing data";

    private static final String[] DEVICE_TYPES_NAMES = {"Elster AS1440", "Elster AS3000", "Landis+Gyr ZMD", "Actaris SL7000", "Siemens 7ED", "Iskra 382"};

    public static final String USER_NAME_SAM = "Sam";
    public static final String USER_NAME_MELISSA = "Melissa";
    public static final String USER_NAME_SYSTEM = "System";
    public static final String VALIDATION_DETECT_MISSING_VALUES = "Detect missing values";

    public static final String DEVICE_GROUP_SOUTH_REGION = "South region";
    public static final String DEVICE_GROUP_NORTH_REGION = "North region";
    public static final String DEVICE_DABF_12 = "DABF410005812";
    public static final String DEVICE_DABF_13 = "DABF410005813";
    public static final String KPI_CONNECTION = "Connection KPI";
    public static final String KPI_COMMUNICATION = "Communication KPI";
    public static final String CREATION_RULE_CONNECTION_LOST = "Connection lost rule";
    public static final String CREATION_RULE_COMMUNICATION_FAILED = "Communication failed rule";
    public static final String DEVICE_STANDARD_PREFIX = "ZABF0100";
    public static final String DEVICE_LP_DATA_PREFIX = "DABF";
    public static final String DEVICE_CONFIGURATION_EXTENDED_CONFIG = "Extended Config";

    private final boolean rethrowExceptions;
    private volatile EngineModelService engineModelService;
    private volatile UserService userService;
    private volatile ValidationService validationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile MasterDataService masterDataService;
    private volatile MeteringService meteringService;
    private volatile TaskService taskService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile SchedulingService schedulingService;
    private volatile LicenseService licenseService;
    private volatile DataModel dataModel;
    private volatile IssueService issueService;
    private volatile IssueCreationService issueCreationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile KpiService kpiService;

    private Store store;
    private Injector injector;

    // For OSGi framework
    @SuppressWarnings("unused")
    public DemoServiceImpl() {
        rethrowExceptions = false;
    }

    // For unit testing purposes
    @Inject
    public DemoServiceImpl(
            EngineModelService engineModelService,
            UserService userService,
            ValidationService validationService,
            TransactionService transactionService,
            ThreadPrincipalService threadPrincipalService,
            ProtocolPluggableService protocolPluggableService,
            MasterDataService masterDataService,
            MeteringService meteringService,
            TaskService taskService,
            DeviceConfigurationService deviceConfigurationService,
            DeviceService deviceService,
            ConnectionTaskService connectionTaskService,
            SchedulingService schedulingService,
            LicenseService licenseService,
            OrmService ormService,
            IssueService issueService,
            IssueCreationService issueCreationService,
            MeteringGroupsService meteringGroupsService,
            KpiService kpiService) {
        setEngineModelService(engineModelService);
        setUserService(userService);
        setValidationService(validationService);
        setTransactionService(transactionService);
        setThreadPrincipalService(threadPrincipalService);
        setProtocolPluggableService(protocolPluggableService);
        setMasterDataService(masterDataService);
        setMeteringService(meteringService);
        setTaskService(taskService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceService(deviceService);
        setConnectionTaskService(connectionTaskService);
        setSchedulingService(schedulingService);
        setLicenseService(licenseService);
        setOrmService(ormService);
        setIssueService(issueService);
        setIssueCreationService(issueCreationService);
        setMeteringGroupsService(meteringGroupsService);
        setKpiService(kpiService);
        rethrowExceptions = true;

        activate();
    }

    @Activate
    public void activate(){
        store = new Store();
        this.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Store.class).toInstance(store);
                bind(EngineModelService.class).toInstance(engineModelService);
                bind(UserService.class).toInstance(userService);
                bind(ValidationService.class).toInstance(validationService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(MasterDataService.class).toInstance(masterDataService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(TaskService.class).toInstance(taskService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(LicenseService.class).toInstance(licenseService);
                bind(DataModel.class).toInstance(dataModel);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(KpiService.class).toInstance(kpiService);
            }
        });
    }

    @Override
    public void createDemoData(final String comServerName, final String host) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<License> license = licenseService.getLicenseForApplication("MDC");
                if (!license.isPresent() || !License.Status.ACTIVE.equals(license.get().getStatus())) {
                    throw new IllegalStateException("MDC License isn't installed correctly");
                }
                store.addProperty("host", host);

                createComServer("Deitvs099");
                createComServer(comServerName);

                createOutboundTcpComPort("Outbound TCP");
                OutboundComPort outboundTCPPort = store.getLast(OutboundComPort.class).orElseThrow(() -> new UnableToCreate("Unable to find a correct TCP com port"));
                store.getOutboundComPortPools().put(VODAFONE_TCP_POOL_NAME, createOutboundTcpComPortPool(VODAFONE_TCP_POOL_NAME, outboundTCPPort));
                store.getOutboundComPortPools().put(ORANGE_TCP_POOL_NAME, createOutboundTcpComPortPool(ORANGE_TCP_POOL_NAME, outboundTCPPort));

                findRegisterTypes(store);
                createLoadProfiles(store);
                createRegisterGroups(store);
                createLogbookTypes(store);
                createCommunicationTasks(store);
                createCommunicationSchedules(store);
                createDeviceTypes(store);
                createDeviceGroups();
                createDemoUsersImpl();
                createValidationRulesImpl();

                createIssueReasons();
                createCreationRule();
                createIssues();
                createKpi();
            }
        });
    }

    private void createCreationRule(){
        injector.getInstance(IssueRuleGenerator.class).withName(CREATION_RULE_CONNECTION_LOST).withType(IssueRuleGenerator.TYPE_CONNECTION_LOST).create();
        injector.getInstance(IssueRuleGenerator.class).withName(CREATION_RULE_COMMUNICATION_FAILED).withType(IssueRuleGenerator.TYPE_COMMUNICATION_FAILED).create();
    }

    private void createIssueReasons(){
        injector.getInstance(IssueReasonGenerator.class).create();
    }

    private void createDeviceGroups(){
        injector.getInstance(DeviceGroupGenerator.class).withName(DEVICE_GROUP_NORTH_REGION).withDeviceTypes(DEVICE_TYPES_NAMES[0], DEVICE_TYPES_NAMES[2], DEVICE_TYPES_NAMES[3]).create();
        injector.getInstance(DeviceGroupGenerator.class).withName(DEVICE_GROUP_SOUTH_REGION).withDeviceTypes(DEVICE_TYPES_NAMES[1], DEVICE_TYPES_NAMES[4], DEVICE_TYPES_NAMES[5]).create();
    }

    private void createComServer(String name) {
        injector.getInstance(ComServerGenerator.class).withName(name).create();
    }

    private void createOutboundTcpComPort(String name) {
        ComServer comServer = store.getLast(ComServer.class).orElseThrow(() -> new UnableToCreate("Unable to find ComServer"));
        injector.getInstance(OutboundTCPComPortGenerator.class).withName(name).withComServer(comServer).create();
    }

    private OutboundComPortPool createOutboundTcpComPortPool(String name, OutboundComPort... comPorts) {
        System.out.println("==> Creating Outbound TCP Port Pool '" + name + "'...");
        OutboundComPortPool outboundComPortPool = engineModelService.newOutboundComPortPool();
        outboundComPortPool.setActive(true);
        outboundComPortPool.setComPortType(ComPortType.TCP);
        outboundComPortPool.setName(name);
        outboundComPortPool.setTaskExecutionTimeout(new TimeDuration(0, TimeDuration.TimeUnit.SECONDS));
        if (comPorts != null) {
            for (OutboundComPort comPort : comPorts) {
                outboundComPortPool.addOutboundComPort(comPort);
            }
        }
        outboundComPortPool.save();
        return outboundComPortPool;
    }

  private void findRegisterTypes(Store store) {
        System.out.println("==> Finding Register Types...");
        store.getRegisterTypes().put(BULK_A_FORWARD_ALL_PHASES_TOU_1_K_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        store.getRegisterTypes().put(BULK_A_FORWARD_ALL_PHASES_TOU_1_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(BULK_A_FORWARD_ALL_PHASES_TOU_2_K_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        store.getRegisterTypes().put(BULK_A_FORWARD_ALL_PHASES_TOU_2_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        store.getRegisterTypes().put(BULK_A_MINUS_ALL_PHASES_TOU_1_K_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        store.getRegisterTypes().put(BULK_A_REVERSE_ALL_PHASES_TOU_1_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(BULK_A_REVERSE_ALL_PHASES_TOU_2_K_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        store.getRegisterTypes().put(BULK_A_REVERSE_ALL_PHASES_TOU_2_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        store.getRegisterTypes().put(BULK_A_FORWARD_ALL_PHASES_TOU_0_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"));
        store.getRegisterTypes().put(BULK_A_REVERSE_ALL_PHASES_TOU_0_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"));

        store.getRegisterTypes().put(ACTIVE_FIRMWARE_VERSION, findOrCreateRegisterType("0.0.0.0.0.41.92.0.0.0.0.0.0.0.0.0.114.0", "1.0.0.2.8.255"));
        store.getRegisterTypes().put(AMR_PROFILE_STATUS_CODE, findOrCreateRegisterType("0.0.0.0.0.41.123.0.0.0.0.0.0.0.0.0.110.0", "0.0.96.10.2.255"));
        store.getRegisterTypes().put(ALARM_REGISTER, findOrCreateRegisterType("0.0.0.0.0.41.118.0.0.0.0.0.0.0.0.0.110.0", "0.0.97.98.0.255"));
    }

    private RegisterType findRegisterType(String mRid) {
        return findOrCreateRegisterType(mRid, "");
    }

    private RegisterType findOrCreateRegisterType(String mRid, String obisCode){
        Optional<ReadingType> readingTypeRef = meteringService.getReadingType(mRid);
        if (!readingTypeRef.isPresent()){
            System.out.println("Unable to found reading type with mrid = " + mRid);
        }
        ReadingType readingType = readingTypeRef.get();
        Optional<RegisterType> registerTypeRef = masterDataService.findRegisterTypeByReadingType(readingType);
        if (!registerTypeRef.isPresent()){
            Unit unit = Unit.get(readingType.getUnit().getUnit().getSymbol());
            if (unit == null){
                unit = Unit.get(BaseUnit.UNITLESS);
            }
            RegisterType registerType = masterDataService.newRegisterType(
                    readingType.getName(),
                    ObisCode.fromString(obisCode),
                    unit,
                    readingType,
                    readingType.getTou());
            registerType.save();
            registerTypeRef = Optional.of(registerType);
        }
        return registerTypeRef.get();
    }

    private void createLoadProfiles(Store store) {
        System.out.println("==> Creating Load Profiles Types...");

        LoadProfileType dailyElectrisity = createLoadProfile(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY, "1.0.99.2.0.255", new TimeDuration(1, TimeDuration.TimeUnit.DAYS));
        List<ReadingType> types = meteringService.getAvailableReadingTypes().stream().sorted(Comparator.comparing(ReadingType::getMRID)).collect(Collectors.toList());

        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        dailyElectrisity.save();
        store.getLoadProfileTypes().put(LOAD_PROFILE_TYPE_DAILY_ELECTRICITY, dailyElectrisity);

        LoadProfileType monthlyElectricity = createLoadProfile(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY, "0.0.98.1.0.255", new TimeDuration(1, TimeDuration.TimeUnit.MONTHS));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        monthlyElectricity.save();
        store.getLoadProfileTypes().put(LOAD_PROFILE_TYPE_MONTHLY_ELECTRICITY, monthlyElectricity);

        LoadProfileType _15minElectricity = createLoadProfile(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY, "1.0.99.1.0.255", new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        _15minElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_0_WH));
        _15minElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_0_WH));
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
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_0_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_0_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        store.getRegisterGroups().put(REGISTER_GROUP_DEFAULT_GROUP, defaultRegisterGroup);

        RegisterGroup tariff1 = masterDataService.newRegisterGroup(REGISTER_GROUP_TARIFF_1);
        tariff1.save();
        tariff1.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        tariff1.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        store.getRegisterGroups().put(REGISTER_GROUP_TARIFF_1, tariff1);

        RegisterGroup tariff2 = masterDataService.newRegisterGroup(REGISTER_GROUP_TARIFF_2);
        tariff2.save();
        tariff2.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        tariff2.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        store.getRegisterGroups().put(REGISTER_GROUP_TARIFF_2, tariff2);

        RegisterGroup deviceDataRegisterGroup = masterDataService.newRegisterGroup(REGISTER_GROUP_DEVICE_DATA);
        deviceDataRegisterGroup.save();
        deviceDataRegisterGroup.addRegisterType(store.getRegisterTypes().get(ACTIVE_FIRMWARE_VERSION));
        deviceDataRegisterGroup.addRegisterType(store.getRegisterTypes().get(AMR_PROFILE_STATUS_CODE));
        deviceDataRegisterGroup.addRegisterType(store.getRegisterTypes().get(ALARM_REGISTER));
        store.getRegisterGroups().put(REGISTER_GROUP_DEVICE_DATA, deviceDataRegisterGroup);
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
        readAll.createLoadProfilesTask().loadProfileTypes(new ArrayList<>(store.getLoadProfileTypes().values())).add();
        RegisterGroup[] registerGroupsForReadAll = {store.getRegisterGroups().get(REGISTER_GROUP_DEFAULT_GROUP), store.getRegisterGroups().get(REGISTER_GROUP_DEVICE_DATA)};
        readAll.createRegistersTask().registerGroups(Arrays.asList(registerGroupsForReadAll)).add();
        readAll.createLogbooksTask().logBookTypes(new ArrayList<>(store.getLogBookTypes().values())).add();
        readAll.save();
        store.getComTasks().put(COM_TASK_READ_ALL, readAll);

        ComTask forceClock = taskService.newComTask(COM_TASK_FORCE_CLOCK);
        forceClock.createClockTask(ClockTaskType.FORCECLOCK).add();
        forceClock.save();
        store.getComTasks().put(COM_TASK_FORCE_CLOCK, forceClock);

        ComTask readDaily = taskService.newComTask(COM_TASK_READ_DAILY);
        readDaily.createClockTask(ClockTaskType.SETCLOCK)
                .minimumClockDifference(new TimeDuration(5, TimeDuration.TimeUnit.SECONDS))
                .maximumClockDifference(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES)).add();
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

        ComTask readRegisterData = taskService.newComTask(COM_TASK_READ_REGISTER_BILLING_DATA);
        List<RegisterGroup> regGroupsToComTask = new ArrayList<>(2);
        regGroupsToComTask.add(store.getRegisterGroups().get(REGISTER_GROUP_TARIFF_1));
        regGroupsToComTask.add(store.getRegisterGroups().get(REGISTER_GROUP_TARIFF_2));
        readRegisterData.createRegistersTask().registerGroups(regGroupsToComTask).add();
        readRegisterData.save();
        store.getComTasks().put(COM_TASK_READ_REGISTER_BILLING_DATA, readRegisterData);

        ComTask readLoadProfileData = taskService.newComTask(COM_TASK_READ_LOAD_PROFILE_DATA);
        readLoadProfileData.createLoadProfilesTask().loadProfileTypes(new ArrayList<>(store.getLoadProfileTypes().values())).add();
        readLoadProfileData.save();
        store.getComTasks().put(COM_TASK_READ_LOAD_PROFILE_DATA, readLoadProfileData);
    }

    private void createCommunicationSchedules(Store store){
        System.out.println("==> Creating Communication Schedules...");
        createCommunicationSchedule(store, COM_SCHEDULE_DAILY_READ_ALL, COM_TASK_READ_ALL, TimeDuration.days(1));
        createCommunicationSchedule(store, COM_SCHEDULE_MOUNTHLY_BILLING_DATA, COM_TASK_READ_REGISTER_BILLING_DATA, TimeDuration.months(1));
    }

    private void createCommunicationSchedule(Store store, String comScheduleName, String taskName, TimeDuration every) {
        //long timeBefore = System.currentTimeMillis() - every.getMilliSeconds() - DateTimeConstants.MILLIS_PER_DAY;
        Instant timeBefore = Instant.now().minusMillis(every.getMilliSeconds()).minus(1, ChronoUnit.DAYS);
        ComSchedule comSchedule = schedulingService.newComSchedule(comScheduleName, new TemporalExpression(every), timeBefore).build();
        comSchedule.addComTask(store.getComTasks().get(taskName));
        comSchedule.save();
        store.getComSchedules().put(comScheduleName, comSchedule);
    }

    public void createDeviceTypes(Store store) {
        for (int i=0; i < 6; i++){
            createDeviceType(store, i);
        }
    }

    public void createDeviceType(Store store, int deviceTypeCount) {
        System.out.println("==> Creating device types...");
        List<DeviceProtocolPluggableClass> webRTUProtocols = protocolPluggableService.findDeviceProtocolPluggableClassesByClassName("com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP");
        if (webRTUProtocols.isEmpty()){
            throw new IllegalStateException("Unable to retrieve the WebRTU KP protocol. Please check that license was correctly installed and that indexing process was finished for protocols.");
        }
        DeviceType deviceType = deviceConfigurationService.newDeviceType(DEVICE_TYPES_NAMES[deviceTypeCount], webRTUProtocols.get(0));
        deviceType.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_0_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_0_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(ALARM_REGISTER));
        deviceType.addRegisterType(store.getRegisterTypes().get(AMR_PROFILE_STATUS_CODE));
        deviceType.addRegisterType(store.getRegisterTypes().get(ACTIVE_FIRMWARE_VERSION));
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
        DeviceType.DeviceConfigurationBuilder configBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_EXTENDED_CONFIG);
        configBuilder.description("A complex configuration that is closely matched to the DSMR 2.3 Devices");
        configBuilder.canActAsGateway(true);
        configBuilder.gatewayType(GatewayType.HOME_AREA_NETWORK);
        configBuilder.isDirectlyAddressable(true);

        addRegisterSpecsToDeviceConfiguration(configBuilder, store,
                BULK_A_FORWARD_ALL_PHASES_TOU_0_WH, BULK_A_FORWARD_ALL_PHASES_TOU_1_WH,
                BULK_A_FORWARD_ALL_PHASES_TOU_2_WH, BULK_A_REVERSE_ALL_PHASES_TOU_0_WH,
                BULK_A_REVERSE_ALL_PHASES_TOU_1_WH, BULK_A_REVERSE_ALL_PHASES_TOU_2_WH,
                ALARM_REGISTER, AMR_PROFILE_STATUS_CODE);
        configBuilder.newTextualRegisterSpec(store.getRegisterTypes().get(ACTIVE_FIRMWARE_VERSION));
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
        enableComTasksOnDeviceConfiguration(configuration, store, COM_TASK_READ_DAILY, COM_TASK_TOPOLOGY, COM_TASK_READ_REGISTER_BILLING_DATA, COM_TASK_READ_ALL);
        configureChannelsForLoadProfileSpec(configuration);
        configuration.activate();
        configuration.save();
        createDevicesForDeviceConfiguration(store, configuration);
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
        configurationProperties.setProperty("SecurityLevel", "0:0");
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
                .setNumberOfFractionDigits(0);
    }

    private void createSimpleDeviceConfiguration(Store store, DeviceType deviceType) {
        System.out.println("==> Creating Simple Device Configuration...");
        DeviceType.DeviceConfigurationBuilder configBuilder = deviceType.newConfiguration("Default");
        configBuilder.description("A simple device configuration which contains one LoadProfile and a minimal set of Registers.");
        configBuilder.canActAsGateway(true);
        configBuilder.gatewayType(GatewayType.HOME_AREA_NETWORK);
        configBuilder.isDirectlyAddressable(true);
        addRegisterSpecsToDeviceConfiguration(configBuilder, store,
                BULK_A_FORWARD_ALL_PHASES_TOU_0_WH, BULK_A_REVERSE_ALL_PHASES_TOU_0_WH,
                ALARM_REGISTER, AMR_PROFILE_STATUS_CODE);
        configBuilder.newTextualRegisterSpec(store.getRegisterTypes().get(ACTIVE_FIRMWARE_VERSION));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(LOAD_PROFILE_TYPE_15_MIN_ELECTRICITY));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(LOG_BOOK_TYPES_DEFAULT_LOGBOOK));
        DeviceConfiguration configuration = configBuilder.add();

        addConnectionMethodToDeviceConfiguration(store, configuration);

        createSecurityPropertySetForDeviceConfiguration(configuration);
        setProtocolDialectConfigurationProperties(configuration);
        enableComTasksOnDeviceConfiguration(configuration, store, COM_TASK_READ_ALL, COM_TASK_READ_DAILY, COM_TASK_READ_REGISTER_BILLING_DATA);
        configureChannelsForLoadProfileSpec(configuration);
        configuration.activate();
        configuration.save();
        createDevicesForDeviceConfiguration(store, configuration);
    }

    private SecurityPropertySet createSecurityPropertySetForDeviceConfiguration(DeviceConfiguration configuration) {
        SecurityPropertySet securityPropertySet = configuration.createSecurityPropertySet("No security").authenticationLevel(0).encryptionLevel(0).build();
        securityPropertySet.update();
        return securityPropertySet;
    }

    private void addConnectionMethodToDeviceConfiguration(Store store, DeviceConfiguration configuration) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp").get();
        configuration.getCommunicationConfiguration()
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(60, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(store.getOutboundComPortPools().get(OUTBOUND_TCP_POOL_NAME))
                .addProperty("host", store.getProperty("host"))
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

    private void createDevicesForDeviceConfiguration(Store store, DeviceConfiguration configuration){
        System.out.println("==> Creating Devices for Configuration...");
        for (int i = 1; i < 9; i++) {
            // 8 devices for each configuration
            String serialNumber = String.format("%04d", configuration.getId()) + String.format("%04d", i);
            String mrid = DEVICE_STANDARD_PREFIX +  serialNumber;
            createDevice(store, configuration, mrid, serialNumber);
        }
        if (DEVICE_TYPES_NAMES[2].equals(configuration.getDeviceType().getName())
                && DEVICE_CONFIGURATION_EXTENDED_CONFIG.equals(configuration.getName())){
            createDevice(store, configuration, DEVICE_DABF_12, "410005812");
            createDevice(store, configuration, DEVICE_DABF_13, "410005813");
        }
    }

    private void createDevice(Store store, DeviceConfiguration configuration, String mrid, String serialNumber){
        injector.getInstance(DeviceGenerator.class).withMrid(mrid).withSerialNumber(serialNumber).withDeviceConfiguration(configuration).create();
        List<Device> devices = store.get(Device.class);
        Device device = devices.get(devices.size() - 1);
        addConnectionMethodToDevice(store, configuration, device);
    }

    private void addConnectionMethodToDevice(Store store, DeviceConfiguration configuration, Device device) {
        PartialScheduledConnectionTask connectionTask = configuration.getCommunicationConfiguration().getPartialOutboundConnectionTasks().get(0);
        int portNumber = 4059;
        // We want two failing devices for 'Actaris SL7000' device type
        if (DEVICE_TYPES_NAMES[3].equals(configuration.getDeviceType().getName()) && device.getmRID().endsWith("8")){
            portNumber = 5049;
        }
        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                .setComPortPool(store.getOutboundComPortPools().get((device.getId() & 1) == 1L ? VODAFONE_TCP_POOL_NAME : ORANGE_TCP_POOL_NAME))
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setNextExecutionSpecsFrom(null)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .setProperty("host", store.getProperty("host"))
                .setProperty("portNumber", new BigDecimal(portNumber))
                .setSimultaneousConnectionsAllowed(false)
                .add();
        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
    }

    private void createIssues(){
        List<Device> devices = deviceService.findAllDevices(Condition.TRUE).find();
        IssueGenerator issueGenerator = injector.getInstance(IssueGenerator.class);
        for (Device device : devices) {
            if (device.getmRID().startsWith(DEVICE_STANDARD_PREFIX)){
                issueGenerator.withDevice(device).create();
            } else if (device.getmRID().equals(DEVICE_DABF_12)) {
                issueGenerator.withDevice(device)
                        .withDueDate(Instant.now().plus(12, ChronoUnit.DAYS))
                        .withIssueReason(IssueReasonGenerator.MessageSeeds.REASON_DAILY_BILLING_READ_FAILED.getKey())
                        .create();

                store.getLast(Issue.class).ifPresent(
                        i -> injector.getInstance(IssueCommentGenerator.class)
                                .withIssue(i)
                                .withUser(USER_NAME_SYSTEM)
                                .withComment("System check: Missing data occurred on this channel: <a href=index.html#/devices/" + DEVICE_DABF_12 + "/channels/" + device.getChannels().get(0).getId() + "/table?filter=%7B%22intervalStart%22%3A%222014-12-18T13%3A00%3A00%22%2C%22duration%22%3A%222weeks%22%2C%22onlySuspect%22%3Atrue%2C%22onlyNonSuspect%22%3Afalse%7D>Bulk A+ all phases (Wh)</a>")
                                .create());
            } else if (device.getmRID().equals(DEVICE_DABF_13)){
                issueGenerator.withDevice(device)
                        .withDueDate(Instant.now().plus(10, ChronoUnit.DAYS))
                        .withIssueReason(IssueReasonGenerator.MessageSeeds.REASON_SUSPECT_VALUES.getKey())
                        .create();
                store.getLast(Issue.class).ifPresent(
                        i -> injector.getInstance(IssueCommentGenerator.class)
                                .withIssue(i)
                                .withUser(USER_NAME_SYSTEM)
                                .withComment("System check: Peak detected on this channel: <a href=index.html#/devices/" + DEVICE_DABF_13 + "/channels/"+ device.getChannels().get(0).getId() + "/graph?filter=%7B%22intervalStart%22%3A%222014-07-30T20%3A00%3A00%22%2C%22duration%22%3A%222weeks%22%2C%22onlySuspect%22%3Afalse%2C%22onlyNonSuspect%22%3Afalse%2C%22id%22%3Anull%7D>Bulk A+ all phases (Wh)</a>")
                                .create());
            }
        }
    }

    @Override
    public void createDemoUsers(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createDemoUsersImpl();
            }
        });
    }

    public void createDemoUsersImpl(){
        System.out.println("==> Creating demo users...");
        createUserAndJoinAllGroups(USER_NAME_SAM);
        createUserAndJoinAllGroups(USER_NAME_MELISSA);
        createUserAndJoinAllGroups(USER_NAME_SYSTEM);
    }

    private void createUserAndJoinAllGroups(String userName) {
        User user = userService.findUser(userName).orElse(null);
        if (user == null){
            String pass = "admin";
            System.out.println("==> Creating new user: " + userName + " with password: " + pass);
            user = userService.createUser(userName, "");
            user.setPassword(pass);
        }
        for (Group group : userService.getGroups()) {
            user.join(group);
        }
        user.save();
    }

    @Override
    public void createValidationRules(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createValidationRulesImpl();
            }
        });
    }

    public void createValidationRulesImpl(){
        System.out.println("==> Creating validation rules");
        if (validationService.getValidationRuleSet(VALIDATION_DETECT_MISSING_VALUES).isPresent()){
            System.out.println("==> Validation rule set " + VALIDATION_DETECT_MISSING_VALUES + " already exists, skip step.");
        }
        ValidationRuleSet ruleSet = validationService.createValidationRuleSet(VALIDATION_DETECT_MISSING_VALUES);
        ValidationRule rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.MissingValuesValidator", VALIDATION_DETECT_MISSING_VALUES);
        // 15min Electricity
        rule.addReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        rule.addReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        // Daily Electricity
        rule.addReadingType("11.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("11.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        // Monthly Electricity
        rule.addReadingType("13.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0");
        rule.addReadingType("13.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0");
        rule.activate();

        ruleSet.save();

        List<DeviceConfiguration> configurations = deviceConfigurationService.getLinkableDeviceConfigurations(ruleSet);
        for (DeviceConfiguration configuration : configurations) {
            System.out.println("==> Validation rule set added to: " + configuration.getName() + " (id = " + configuration.getId() + ")");
            configuration.addValidationRuleSet(ruleSet);
            configuration.save();
        }
    }

    public void createKpi(){
        injector.getInstance(DynamicKpiGenerator.class).withName(KPI_CONNECTION).create();
        injector.getInstance(DynamicKpiGenerator.class).withName(KPI_COMMUNICATION).create();
    }

    @Reference
    @SuppressWarnings("unused")
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("DEMO", "");
    }

    @Reference
    @SuppressWarnings("unused")
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setIssueCreationService(IssueCreationService issueCreationService) {
        this.issueCreationService = issueCreationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
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
        return () -> "console";
    }

}