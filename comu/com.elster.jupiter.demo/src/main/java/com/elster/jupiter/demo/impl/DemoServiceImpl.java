package com.elster.jupiter.demo.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.demo.impl.factories.AppServerFactory;
import com.elster.jupiter.demo.impl.factories.ComServerFactory;
import com.elster.jupiter.demo.impl.factories.DeviceFactory;
import com.elster.jupiter.demo.impl.factories.DeviceGroupFactory;
import com.elster.jupiter.demo.impl.factories.DynamicKpiFactory;
import com.elster.jupiter.demo.impl.factories.IssueCommentFactory;
import com.elster.jupiter.demo.impl.factories.IssueFactory;
import com.elster.jupiter.demo.impl.factories.IssueReasonFactory;
import com.elster.jupiter.demo.impl.factories.IssueRuleFactory;
import com.elster.jupiter.demo.impl.factories.OutboundTCPComPortFactory;
import com.elster.jupiter.demo.impl.factories.UserFactory;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.cron.CronExpressionParser;
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
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
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
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


@Component(name = "com.elster.jupiter.demo", service = {DemoService.class, DemoServiceImpl.class}, property = {
        "osgi.command.scope=demo",
        "osgi.command.function=createDemoData",
        "osgi.command.function=createUsers",
        "osgi.command.function=createAppServer",
        "osgi.command.function=createCollectRemoteDataSetup",
        "osgi.command.function=createValidationRules"
}, immediate = true)
public class DemoServiceImpl implements DemoService {
    private final boolean rethrowExceptions;
    private volatile EngineConfigurationService engineConfigurationService;
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
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile AppService appService;
    private volatile MessageService messageService;
    private volatile DataExportService dataExportService;
    private volatile CronExpressionParser cronExpressionParser;

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
            EngineConfigurationService engineConfigurationService,
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
            KpiService kpiService,
            IssueDataCollectionService issueDataCollectionService,
            DataCollectionKpiService dataCollectionKpiService,
            AppService appService,
            MessageService messageService,
            DataExportService dataExportService,
            CronExpressionParser cronExpressionParser) {
        setEngineConfigurationService(engineConfigurationService);
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
        setIssueDataCollectionService(issueDataCollectionService);
        setDataCollectionKpiService(dataCollectionKpiService);
        setAppService(appService);
        setMessageService(messageService);
        setDataExportService(dataExportService);
        setCronExpressionParser(cronExpressionParser);
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
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
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
                bind(IssueDataCollectionService.class).toInstance(issueDataCollectionService);
                bind(DataCollectionKpiService.class).toInstance(dataCollectionKpiService);
                bind(AppService.class).toInstance(appService);
                bind(MessageService.class).toInstance(messageService);
                bind(DataExportService.class).toInstance(dataExportService);
                bind(CronExpressionParser.class).toInstance(cronExpressionParser);
            }
        });
    }

    @Override
    public void createDemoData(final String comServerName, final String host) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createCollectRemoteDataSetupImpl(comServerName, host);
                createUsersImpl();
                createValidationRulesImpl();
                createAppServerImpl();
            }
        });
    }

    @Override
    public void createCollectRemoteDataSetup(final String comServerName, final String host) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createCollectRemoteDataSetupImpl(comServerName, host);
            }
        });
    }

    @Override
    public void createAppServer(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createAppServerImpl();
            }
        });
    }


    @Override
    public void createUsers(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createUsersImpl();
            }
        });
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
    private void createCollectRemoteDataSetupImpl(final String comServerName, final String host){
        Optional<License> license = licenseService.getLicenseForApplication("MDC");
        if (!license.isPresent() || !License.Status.ACTIVE.equals(license.get().getStatus())) {
            throw new IllegalStateException("MDC License isn't installed correctly");
        }
        store.addProperty("host", host);

        createComServer("Deitvs099");
        createComServer(comServerName);

        createOutboundTcpComPort("Outbound TCP");
        OutboundComPort outboundTCPPort = store.getLast(OutboundComPort.class).orElseThrow(() -> new UnableToCreate("Unable to find a correct TCP com port"));
        store.getOutboundComPortPools().put(Constants.OutboundComPortPool.VODAFONE, createOutboundTcpComPortPool(Constants.OutboundComPortPool.VODAFONE, outboundTCPPort));
        store.getOutboundComPortPools().put(Constants.OutboundComPortPool.ORANGE, createOutboundTcpComPortPool(Constants.OutboundComPortPool.ORANGE, outboundTCPPort));

        findRegisterTypes(store);
        createLoadProfiles(store);
        createRegisterGroups(store);
        createLogbookTypes(store);
        createCommunicationTasks(store);
        createCommunicationSchedules(store);
        createDeviceTypes(store);
        createDeviceGroups();

        createIssueReasons();
        createCreationRule();
        createKpi();
    }

    private void createCreationRule(){
        injector.getInstance(IssueRuleFactory.class)
                .withName(Constants.CreationRule.CONNECTION_LOST)
                .withType(Constants.IssueCreationRule.TYPE_CONNECTION_LOST)
                .withReason(Constants.IssueReason.CONNECTION_FAILED.getKey())
                .get();
        injector.getInstance(IssueRuleFactory.class)
                .withName(Constants.CreationRule.COMMUNICATION_FAILED)
                .withType(Constants.IssueCreationRule.TYPE_COMMUNICATION_FAILED)
                .withReason(Constants.IssueReason.COMMUNICATION_FAILED.getKey())
                .get();
    }

    private void createIssueReasons(){
        injector.getInstance(IssueReasonFactory.class).get(); // TODO split reasons!
    }

    private void createDeviceGroups(){
        injector.getInstance(DeviceGroupFactory.class)
                .withName(Constants.DeviceGroup.NORTH_REGION)
                .withDeviceTypes(Constants.DeviceType.Elster_AS1440.getName(), Constants.DeviceType.Landis_Gyr_ZMD.getName(), Constants.DeviceType.Actaris_SL7000.name())
                .get();
        injector.getInstance(DeviceGroupFactory.class)
                .withName(Constants.DeviceGroup.SOUTH_REGION)
                .withDeviceTypes(Constants.DeviceType.Elster_AS3000.getName(), Constants.DeviceType.Siemens_7ED.getName(), Constants.DeviceType.Iskra_38.name())
                .get();
    }

    private void createComServer(String name) {
        injector.getInstance(ComServerFactory.class).withName(name).get();
    }

    private void createOutboundTcpComPort(String name) {
        ComServer comServer = store.getLast(ComServer.class).orElseThrow(() -> new UnableToCreate("Unable to find ComServer"));
        injector.getInstance(OutboundTCPComPortFactory.class).withName(name).withComServer(comServer).get();
    }

    private OutboundComPortPool createOutboundTcpComPortPool(String name, OutboundComPort... comPorts) {
        System.out.println("==> Creating Outbound TCP Port Pool '" + name + "'...");
        OutboundComPortPool outboundComPortPool = engineConfigurationService.newOutboundComPortPool(name, ComPortType.TCP, new TimeDuration(0, TimeDuration.TimeUnit.SECONDS));
        outboundComPortPool.setActive(true);
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
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_K_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_K_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_K_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_K_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_0_WH, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_0_WH, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"));

        store.getRegisterTypes().put(Constants.RegisterTypes.ACTIVE_FIRMWARE_VERSION, findOrCreateRegisterType("0.0.0.0.0.41.92.0.0.0.0.0.0.0.0.0.114.0", "1.0.0.2.8.255"));
        store.getRegisterTypes().put(Constants.RegisterTypes.AMR_PROFILE_STATUS_CODE, findOrCreateRegisterType("0.0.0.0.0.41.123.0.0.0.0.0.0.0.0.0.110.0", "0.0.96.10.2.255"));
        store.getRegisterTypes().put(Constants.RegisterTypes.ALARM_REGISTER, findOrCreateRegisterType("0.0.0.0.0.41.118.0.0.0.0.0.0.0.0.0.110.0", "0.0.97.98.0.255"));
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

        LoadProfileType dailyElectrisity = createLoadProfile(Constants.LoadProfileType.DAILY_ELECTRICITY, "1.0.99.2.0.255", new TimeDuration(1, TimeDuration.TimeUnit.DAYS));
        List<ReadingType> types = meteringService.getAvailableReadingTypes().stream().sorted(Comparator.comparing(ReadingType::getMRID)).collect(Collectors.toList());

        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        dailyElectrisity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        dailyElectrisity.save();
        store.getLoadProfileTypes().put(Constants.LoadProfileType.DAILY_ELECTRICITY, dailyElectrisity);

        LoadProfileType monthlyElectricity = createLoadProfile(Constants.LoadProfileType.MONTHLY_ELECTRICITY, "0.0.98.1.0.255", new TimeDuration(1, TimeDuration.TimeUnit.MONTHS));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        monthlyElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        monthlyElectricity.save();
        store.getLoadProfileTypes().put(Constants.LoadProfileType.MONTHLY_ELECTRICITY, monthlyElectricity);

        LoadProfileType _15minElectricity = createLoadProfile(Constants.LoadProfileType._15_MIN_ELECTRICITY, "1.0.99.1.0.255", new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        _15minElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_0_WH));
        _15minElectricity.createChannelTypeForRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_0_WH));
        _15minElectricity.save();
        store.getLoadProfileTypes().put(Constants.LoadProfileType._15_MIN_ELECTRICITY, _15minElectricity);
    }

    private LoadProfileType createLoadProfile(String name, String obisCode, TimeDuration duartion) {
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(name, ObisCode.fromString(obisCode), duartion);
        loadProfileType.save();
        return loadProfileType;
    }

    private void createRegisterGroups(Store store) {
        System.out.println("==> Creating Register Groups...");

        com.energyict.mdc.masterdata.RegisterGroup defaultRegisterGroup = masterDataService.newRegisterGroup(Constants.RegisterGroup.DEFAULT_GROUP);
        defaultRegisterGroup.save();
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_0_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_0_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        store.getRegisterGroups().put(Constants.RegisterGroup.DEFAULT_GROUP, defaultRegisterGroup);

        com.energyict.mdc.masterdata.RegisterGroup tariff1 = masterDataService.newRegisterGroup(Constants.RegisterGroup.TARIFF_1);
        tariff1.save();
        tariff1.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        tariff1.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        store.getRegisterGroups().put(Constants.RegisterGroup.TARIFF_1, tariff1);

        com.energyict.mdc.masterdata.RegisterGroup tariff2 = masterDataService.newRegisterGroup(Constants.RegisterGroup.TARIFF_2);
        tariff2.save();
        tariff2.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        tariff2.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        store.getRegisterGroups().put(Constants.RegisterGroup.TARIFF_2, tariff2);

        com.energyict.mdc.masterdata.RegisterGroup deviceDataRegisterGroup = masterDataService.newRegisterGroup(Constants.RegisterGroup.DEVICE_DATA);
        deviceDataRegisterGroup.save();
        deviceDataRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.ACTIVE_FIRMWARE_VERSION));
        deviceDataRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.AMR_PROFILE_STATUS_CODE));
        deviceDataRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.ALARM_REGISTER));
        store.getRegisterGroups().put(Constants.RegisterGroup.DEVICE_DATA, deviceDataRegisterGroup);
    }

    private void createLogbookTypes(Store store) {
        System.out.println("==> Creating Log Book Types...");
        createLogBookType(store, Constants.LogBookType.DEFAULT_LOGBOOK, "0.0.99.98.0.255");
        createLogBookType(store, Constants.LogBookType.POWER_FAILURES, "1.0.99.97.0.255");
        createLogBookType(store, Constants.LogBookType.FRAUD_DETECTIONS, "0.0.99.98.1.255");
    }

    private void createLogBookType(Store store, String logBookTypeName, String obisCode) {
        com.energyict.mdc.masterdata.LogBookType logBookType = masterDataService.newLogBookType(logBookTypeName, ObisCode.fromString(obisCode));
        logBookType.save();
        store.getLogBookTypes().put(logBookTypeName, logBookType);
    }

    private void createCommunicationTasks(Store store) {
        System.out.println("==> Creating Communication Tasks...");

        ComTask readAll = taskService.newComTask(Constants.CommunicationTask.READ_ALL);
        readAll.createLoadProfilesTask().loadProfileTypes(new ArrayList<>(store.getLoadProfileTypes().values())).add();
        com.energyict.mdc.masterdata.RegisterGroup[] registerGroupsForReadAll = {store.getRegisterGroups().get(Constants.RegisterGroup.DEFAULT_GROUP), store.getRegisterGroups().get(Constants.RegisterGroup.DEVICE_DATA)};
        readAll.createRegistersTask().registerGroups(Arrays.asList(registerGroupsForReadAll)).add();
        readAll.createLogbooksTask().logBookTypes(new ArrayList<>(store.getLogBookTypes().values())).add();
        readAll.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_ALL, readAll);

        ComTask forceClock = taskService.newComTask(Constants.CommunicationTask.FORCE_CLOCK);
        forceClock.createClockTask(ClockTaskType.FORCECLOCK).add();
        forceClock.save();
        store.getComTasks().put(Constants.CommunicationTask.FORCE_CLOCK, forceClock);

        ComTask readDaily = taskService.newComTask(Constants.CommunicationTask.READ_DAILY);
        readDaily.createClockTask(ClockTaskType.SETCLOCK)
                .minimumClockDifference(new TimeDuration(5, TimeDuration.TimeUnit.SECONDS))
                .maximumClockDifference(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES)).add();
        LoadProfileType[] loadProfileTypesForReadDayly = {store.getLoadProfileTypes().get(Constants.LoadProfileType.DAILY_ELECTRICITY), store.getLoadProfileTypes().get(Constants.LoadProfileType._15_MIN_ELECTRICITY)};
        readDaily.createLoadProfilesTask().loadProfileTypes(Arrays.asList(loadProfileTypesForReadDayly)).add();
        readDaily.createLogbooksTask().logBookTypes(Collections.singletonList(store.getLogBookTypes().get(Constants.LogBookType.DEFAULT_LOGBOOK))).add();
        com.energyict.mdc.masterdata.RegisterGroup[] registerGroupsForReadDayly = {store.getRegisterGroups().get(Constants.RegisterGroup.TARIFF_1), store.getRegisterGroups().get(Constants.RegisterGroup.TARIFF_2)};
        readDaily.createRegistersTask().registerGroups(Arrays.asList(registerGroupsForReadDayly)).add();
        readDaily.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_DAILY, readDaily);

        ComTask topology = taskService.newComTask(Constants.CommunicationTask.TOPOLOGY);
        topology.createTopologyTask(TopologyAction.VERIFY);
        topology.save();
        store.getComTasks().put(Constants.CommunicationTask.TOPOLOGY, topology);

        ComTask readRegisterData = taskService.newComTask(Constants.CommunicationTask.READ_REGISTER_BILLING_DATA);
        List<com.energyict.mdc.masterdata.RegisterGroup> regGroupsToComTask = new ArrayList<>(2);
        regGroupsToComTask.add(store.getRegisterGroups().get(Constants.RegisterGroup.TARIFF_1));
        regGroupsToComTask.add(store.getRegisterGroups().get(Constants.RegisterGroup.TARIFF_2));
        readRegisterData.createRegistersTask().registerGroups(regGroupsToComTask).add();
        readRegisterData.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_REGISTER_BILLING_DATA, readRegisterData);

        ComTask readLoadProfileData = taskService.newComTask(Constants.CommunicationTask.READ_LOAD_PROFILE_DATA);
        readLoadProfileData.createLoadProfilesTask().loadProfileTypes(new ArrayList<>(store.getLoadProfileTypes().values())).add();
        readLoadProfileData.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_LOAD_PROFILE_DATA, readLoadProfileData);
    }

    private void createCommunicationSchedules(Store store){
        System.out.println("==> Creating Communication Schedules...");
        createCommunicationSchedule(store, Constants.CommunicationSchedules.DAILY_READ_ALL, Constants.CommunicationTask.READ_ALL, TimeDuration.days(1));
        createCommunicationSchedule(store, Constants.CommunicationSchedules.MONTHLY_BILLING_DATA, Constants.CommunicationTask.READ_REGISTER_BILLING_DATA, TimeDuration.months(1));
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
        DeviceType deviceType = deviceConfigurationService.newDeviceType(Constants.DeviceType.values()[deviceTypeCount].getName(), webRTUProtocols.get(0));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_0_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_0_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_WH));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.ALARM_REGISTER));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.AMR_PROFILE_STATUS_CODE));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.ACTIVE_FIRMWARE_VERSION));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(Constants.LoadProfileType.DAILY_ELECTRICITY));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(Constants.LoadProfileType.MONTHLY_ELECTRICITY));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(Constants.LoadProfileType._15_MIN_ELECTRICITY));

        deviceType.addLogBookType(store.getLogBookTypes().get(Constants.LogBookType.DEFAULT_LOGBOOK));
        deviceType.addLogBookType(store.getLogBookTypes().get(Constants.LogBookType.FRAUD_DETECTIONS));
        deviceType.addLogBookType(store.getLogBookTypes().get(Constants.LogBookType.POWER_FAILURES));
        deviceType.save();

        createSimpleDeviceConfiguration(store, deviceType);
        createExtendedDeviceConfiguration(store, deviceType);
    }

    private void createExtendedDeviceConfiguration(Store store, DeviceType deviceType) {
        System.out.println("==> Creating Extended Device Configuration...");
        DeviceType.DeviceConfigurationBuilder configBuilder = deviceType.newConfiguration(Constants.DeviceConfiguration.EXTENDED_CONFIG);
        configBuilder.description("A complex configuration that is closely matched to the DSMR 2.3 Devices");
        configBuilder.canActAsGateway(true);
        configBuilder.gatewayType(GatewayType.HOME_AREA_NETWORK);
        configBuilder.isDirectlyAddressable(true);

        addRegisterSpecsToDeviceConfiguration(configBuilder, store,
                Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_0_WH, Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_1_WH,
                Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_2_WH, Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_0_WH,
                Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_1_WH, Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_2_WH,
                Constants.RegisterTypes.ALARM_REGISTER, Constants.RegisterTypes.AMR_PROFILE_STATUS_CODE);
        configBuilder.newTextualRegisterSpec(store.getRegisterTypes().get(Constants.RegisterTypes.ACTIVE_FIRMWARE_VERSION));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(Constants.LoadProfileType._15_MIN_ELECTRICITY));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(Constants.LoadProfileType.DAILY_ELECTRICITY));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(Constants.LoadProfileType.MONTHLY_ELECTRICITY));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(Constants.LogBookType.DEFAULT_LOGBOOK));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(Constants.LogBookType.POWER_FAILURES));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(Constants.LogBookType.FRAUD_DETECTIONS));
        DeviceConfiguration configuration = configBuilder.add();

        addConnectionMethodToDeviceConfiguration(store, configuration);
        createSecurityPropertySetForDeviceConfiguration(configuration);
        setProtocolDialectConfigurationProperties(configuration);
        enableComTasksOnDeviceConfiguration(configuration, store, Constants.CommunicationTask.READ_DAILY, Constants.CommunicationTask.TOPOLOGY, Constants.CommunicationTask.READ_REGISTER_BILLING_DATA, Constants.CommunicationTask.READ_ALL);
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
                Constants.RegisterTypes.BULK_A_FORWARD_ALL_PHASES_TOU_0_WH, Constants.RegisterTypes.BULK_A_REVERSE_ALL_PHASES_TOU_0_WH,
                Constants.RegisterTypes.ALARM_REGISTER, Constants.RegisterTypes.AMR_PROFILE_STATUS_CODE);
        configBuilder.newTextualRegisterSpec(store.getRegisterTypes().get(Constants.RegisterTypes.ACTIVE_FIRMWARE_VERSION));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(Constants.LoadProfileType._15_MIN_ELECTRICITY));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(Constants.LogBookType.DEFAULT_LOGBOOK));
        DeviceConfiguration configuration = configBuilder.add();

        addConnectionMethodToDeviceConfiguration(store, configuration);

        createSecurityPropertySetForDeviceConfiguration(configuration);
        setProtocolDialectConfigurationProperties(configuration);
        enableComTasksOnDeviceConfiguration(configuration, store, Constants.CommunicationTask.READ_ALL, Constants.CommunicationTask.READ_DAILY, Constants.CommunicationTask.READ_REGISTER_BILLING_DATA);
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
        configuration
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(60, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(store.getOutboundComPortPools().get(Constants.OutboundComPortPool.OUTBOUND_TCP_POOL))
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
            String mrid = Constants.Device.STANDARD_PREFIX +  serialNumber;
            createDevice(store, configuration, mrid, serialNumber);
        }
        if (Constants.DeviceType.Landis_Gyr_ZMD.getName().equals(configuration.getDeviceType().getName())
                && Constants.DeviceConfiguration.EXTENDED_CONFIG.equals(configuration.getName())){
            createDevice(store, configuration, Constants.Device.DABF_12, "410005812");
            createDevice(store, configuration, Constants.Device.DABF_13, "410005813");
        }
    }

    private void createDevice(Store store, DeviceConfiguration configuration, String mrid, String serialNumber){
        injector.getInstance(DeviceFactory.class).withMrid(mrid).withSerialNumber(serialNumber).withDeviceConfiguration(configuration).get();
        List<Device> devices = store.get(Device.class);
        Device device = devices.get(devices.size() - 1);
        addConnectionMethodToDevice(store, configuration, device);
    }

    private void addConnectionMethodToDevice(Store store, DeviceConfiguration configuration, Device device) {
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        int portNumber = 4059;
        // We want two failing devices for 'Actaris SL7000' device type
        if (Constants.DeviceType.Actaris_SL7000.getName().equals(configuration.getDeviceType().getName()) && device.getmRID().endsWith("8")){
            portNumber = 5049;
        }
        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                .setComPortPool(store.getOutboundComPortPools().get((device.getId() & 1) == 1L ? Constants.OutboundComPortPool.VODAFONE : Constants.OutboundComPortPool.ORANGE))
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
        for (Device device : devices) {
            IssueFactory issueGenerator = injector.getInstance(IssueFactory.class);
            if (device.getmRID().startsWith(Constants.Device.STANDARD_PREFIX)){
                issueGenerator.withDevice(device).withAssignee(device.getId() % 7 == 0 ? Constants.User.SAM : null).get();
            } else if (device.getmRID().equals(Constants.Device.DABF_12)) {
                issueGenerator.withDevice(device)
                        .withDueDate(Instant.now().plus(12, ChronoUnit.DAYS))
                        .withIssueReason(Constants.IssueReason.DAILY_BILLING_READ_FAILED.getKey())
                        .withAssignee(Constants.User.SAM)
                        .get();

                store.getLast(IssueDataCollection.class).ifPresent(
                        i -> injector.getInstance(IssueCommentFactory.class)
                                .withIssue(i)
                                .withUser(Constants.User.SAM)
                                .withComment("System check: Missing data occurred on this channel: <a href=index.html#/devices/" + Constants.Device.DABF_12 + "/channels/" + device.getChannels().get(0).getId() + "/table?filter=%7B%22intervalStart%22%3A%222014-12-18T13%3A00%3A00%22%2C%22duration%22%3A%222weeks%22%2C%22onlySuspect%22%3Atrue%2C%22onlyNonSuspect%22%3Afalse%7D>Bulk A+ all phases (Wh)</a>")
                                .get());
            } else if (device.getmRID().equals(Constants.Device.DABF_13)){
                issueGenerator.withDevice(device)
                        .withDueDate(Instant.now().plus(10, ChronoUnit.DAYS))
                        .withIssueReason(Constants.IssueReason.SUSPECT_VALUES.getKey())
                        .withAssignee(Constants.User.SAM)
                        .get();
                store.getLast(IssueDataCollection.class).ifPresent(
                        i -> injector.getInstance(IssueCommentFactory.class)
                                .withIssue(i)
                                .withUser(Constants.User.SAM)
                                .withComment("System check: Peak detected on this channel: <a href=index.html#/devices/" + Constants.Device.DABF_13 + "/channels/"+ device.getChannels().get(0).getId() + "/graph?filter=%7B%22intervalStart%22%3A%222014-07-30T20%3A00%3A00%22%2C%22duration%22%3A%222weeks%22%2C%22onlySuspect%22%3Afalse%2C%22onlyNonSuspect%22%3Afalse%2C%22id%22%3Anull%7D>Bulk A+ all phases (Wh)</a>")
                                .get());
            }
        }
    }

    public void createUsersImpl(){
        injector.getInstance(UserFactory.class).withName(Constants.User.MELISSA).withRoles(Constants.UserRoles.METER_EXPERT).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.SAM).withLanguage(Locale.US.toLanguageTag()).withRoles(Constants.UserRoles.ADMINISTRATORS).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.PIETER).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.JOLIEN).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.INGE).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.KOEN).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.SEBASTIEN).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.VEERLE).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.KURT).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.EDUARDO).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
    }

    public void createValidationRulesImpl(){
        System.out.println("==> Creating validation rules");
        if (validationService.getValidationRuleSet(Constants.Validation.DETECT_MISSING_VALUES).isPresent()){
            System.out.println("==> Validation rule set " + Constants.Validation.DETECT_MISSING_VALUES + " already exists, skip step.");
        }
        ValidationRuleSet ruleSet = validationService.createValidationRuleSet(Constants.Validation.DETECT_MISSING_VALUES);
        ValidationRule rule = ruleSet.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.MissingValuesValidator", Constants.Validation.DETECT_MISSING_VALUES);
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
        store.get(EndDeviceGroup.class).stream().forEach(g -> injector.getInstance(DynamicKpiFactory.class).withGroup(g).get());
    }

    public void createAppServerImpl(){
        injector.getInstance(AppServerFactory.class).withName(Constants.AppServer.DEFAULT).get();
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("DEMO", "");
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setIssueCreationService(IssueCreationService issueCreationService) {
        this.issueCreationService = issueCreationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
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