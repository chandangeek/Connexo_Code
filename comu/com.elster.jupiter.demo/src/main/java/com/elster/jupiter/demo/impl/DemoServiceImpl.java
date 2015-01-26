package com.elster.jupiter.demo.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.demo.impl.commands.CreateA3DeviceCommand;
import com.elster.jupiter.demo.impl.commands.upload.UploadAllCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddIntervalChannelReadingsCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddNoneIntervalChannelReadingsCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddRegisterReadingsCommand;
import com.elster.jupiter.demo.impl.factories.AppServerFactory;
import com.elster.jupiter.demo.impl.factories.AssignmentRuleFactory;
import com.elster.jupiter.demo.impl.factories.ComServerFactory;
import com.elster.jupiter.demo.impl.factories.DataExportTaskFactory;
import com.elster.jupiter.demo.impl.factories.DeviceFactory;
import com.elster.jupiter.demo.impl.factories.DeviceGroupFactory;
import com.elster.jupiter.demo.impl.factories.DynamicKpiFactory;
import com.elster.jupiter.demo.impl.factories.FavoriteGroupFactory;
import com.elster.jupiter.demo.impl.factories.InboundComPortPoolFactory;
import com.elster.jupiter.demo.impl.factories.IssueFactory;
import com.elster.jupiter.demo.impl.factories.IssueRuleFactory;
import com.elster.jupiter.demo.impl.factories.NTASimToolFactory;
import com.elster.jupiter.demo.impl.factories.OutboundTCPComPortFactory;
import com.elster.jupiter.demo.impl.factories.OutboundTCPComPortPoolFactory;
import com.elster.jupiter.demo.impl.factories.UserFactory;
import com.elster.jupiter.demo.impl.factories.ValidationRuleSetFactory;
import com.elster.jupiter.demo.impl.finders.ComTaskFinder;
import com.elster.jupiter.demo.impl.finders.LogBookFinder;
import com.elster.jupiter.demo.impl.finders.OutboundComPortPoolFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
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
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.demo", service = {DemoService.class, DemoServiceImpl.class}, property = {
        "osgi.command.scope=demo",
        "osgi.command.function=createDemoData",
        "osgi.command.function=createUserManagement",
        "osgi.command.function=createIssues",
        "osgi.command.function=createApplicationServer",
        "osgi.command.function=createA3Device",
        "osgi.command.function=createNtaConfig",
        "osgi.command.function=createMockedDataDevice",
        "osgi.command.function=createValidationDevice",
        "osgi.command.function=createDeliverDataSetup",
        "osgi.command.function=createCollectRemoteDataSetup",
        "osgi.command.function=createValidationSetup",
        "osgi.command.function=createAssignmentRules",
        "osgi.command.function=addIntervalChannelReadings",
        "osgi.command.function=addNoneIntervalChannelReadings",
        "osgi.command.function=addRegisterReadings"
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
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile KpiService kpiService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile AppService appService;
    private volatile MessageService messageService;
    private volatile DataExportService dataExportService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile TimeService timeService;
    private volatile FavoritesService favoritesService;

    private Store store;
    private Injector injector;
    private int deviceCounter = 0;

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
            MeteringGroupsService meteringGroupsService,
            KpiService kpiService,
            IssueDataCollectionService issueDataCollectionService,
            DataCollectionKpiService dataCollectionKpiService,
            AppService appService,
            MessageService messageService,
            DataExportService dataExportService,
            CronExpressionParser cronExpressionParser,
            TimeService timeService,
            FavoritesService favoritesService) {
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
        setMeteringGroupsService(meteringGroupsService);
        setKpiService(kpiService);
        setIssueDataCollectionService(issueDataCollectionService);
        setDataCollectionKpiService(dataCollectionKpiService);
        setAppService(appService);
        setMessageService(messageService);
        setDataExportService(dataExportService);
        setCronExpressionParser(cronExpressionParser);
        setTimeService(timeService);
        setFavoritesService(favoritesService);
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
                bind(IssueAssignmentService.class).toInstance(issueAssignmentService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(KpiService.class).toInstance(kpiService);
                bind(IssueDataCollectionService.class).toInstance(issueDataCollectionService);
                bind(DataCollectionKpiService.class).toInstance(dataCollectionKpiService);
                bind(AppService.class).toInstance(appService);
                bind(MessageService.class).toInstance(messageService);
                bind(DataExportService.class).toInstance(dataExportService);
                bind(CronExpressionParser.class).toInstance(cronExpressionParser);
                bind(TimeService.class).toInstance(timeService);
                bind(FavoritesService.class).toInstance(favoritesService);
            }
        });
    }

    @SuppressWarnings("unused")
    public void addIntervalChannelReadings(String mrid, String startDate, String path) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                AddIntervalChannelReadingsCommand command = injector.getInstance(AddIntervalChannelReadingsCommand.class);
                command.setMeter(mrid);
                command.setStartDate(startDate);
                command.setSource(path);
                command.run();
            }
        });
    }

    @SuppressWarnings("unused")
    public void addNoneIntervalChannelReadings(String mrid, String startDate, String path) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                AddNoneIntervalChannelReadingsCommand command = injector.getInstance(AddNoneIntervalChannelReadingsCommand.class);
                command.setMeter(mrid);
                command.setStartDate(startDate);
                command.setSource(path);
                command.run();
            }
        });
    }


    @SuppressWarnings("unused")
    public void addRegisterReadings(String mrid, String startDate, String path) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                AddRegisterReadingsCommand command = injector.getInstance(AddRegisterReadingsCommand.class);
                command.setMeter(mrid);
                command.setStartDate(startDate);
                command.setSource(path);
                command.run();
            }
        });
    }


    @Override
    public void createDemoData(final String comServerName, final String host, final String startDate) {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createUserManagementImpl();
                createCollectRemoteDataSetupImpl(comServerName, host);
                createValidationSetupImpl();
                createDeliverDataSetupImpl();
                createMockedDataDeviceImpl(Constants.Device.MOCKED_REALISTIC_DEVICE, Constants.Device.MOCKED_REALISTIC_SERIAL_NUMBER);
                createApplicationServerImpl(comServerName); // the same name as for comserver
                createNtaConfigImpl();
            }
        });
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                uploadAllData(startDate);
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
    public void createApplicationServer(final String appServerName){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createApplicationServerImpl(appServerName);
            }
        });
    }

    @Override
    public void createUserManagement(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createUserManagementImpl();
            }
        });
    }

    @Override
    public void createValidationSetup(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createValidationSetupImpl();
            }
        });
    }

    @Override
    public void createDeliverDataSetup(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createDeliverDataSetupImpl();
            }
        });
    }

    @Override
    public void createIssues(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createIssuesImpl();
            }
        });
    }

    @Override
    public void createA3Device(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                CreateA3DeviceCommand command = injector.getInstance(CreateA3DeviceCommand.class);
                command.run();
            }
        });
    }

    @Override
    public void createMockedDataDevice(String serialNumber){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createMockedDataDeviceImpl(Constants.Device.MOCKED_REALISTIC_DEVICE, serialNumber);
            }
        });
    }

    @Override
    public void createValidationDevice(String serialNumber){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createMockedDataDeviceImpl(Constants.Device.MOCKED_VALIDATION_DEVICE, serialNumber);
                activateValidation(Constants.Device.MOCKED_VALIDATION_DEVICE + serialNumber, Constants.Validation.RULE_SET_NAME);
            }
        });
    }


    @Override
    public void createNtaConfig(){
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createNtaConfigImpl();
            }
        });
    }

    private void createCollectRemoteDataSetupImpl(final String comServerName, final String host){
        Optional<License> license = licenseService.getLicenseForApplication("MDC");
        if (!license.isPresent() || !License.Status.ACTIVE.equals(license.get().getStatus())) {
            throw new IllegalStateException("MDC License isn't installed correctly");
        }
        store.addProperty("host", host);

        createComServers(comServerName);

        createComPortsAndPools();
        findRegisterTypes(store);
        createLoadProfiles(store);
        createRegisterGroups(store);
        createLogbookTypes(store);
        createCommunicationTasks(store);
        createCommunicationSchedules(store);
        createDeviceTypes(store);
        createDeviceGroups();
        createCreationRule();
        createKpi();
    }

    private void createComServers(String comServerName) {
        injector.getInstance(ComServerFactory.class).withName("Deitvs099").withActiveStatus(false).get();
        injector.getInstance(ComServerFactory.class).withName(comServerName).get();
    }

    private void createCreationRule(){
        injector.getInstance(IssueRuleFactory.class)
                .withName(Constants.CreationRule.CONNECTION_LOST)
                .withType(Constants.IssueCreationRule.TYPE_CONNECTION_LOST)
                .withReason(Constants.IssueReason.CONNECTION_FAILED)
                .get();
        injector.getInstance(IssueRuleFactory.class)
                .withName(Constants.CreationRule.COMMUNICATION_FAILED)
                .withType(Constants.IssueCreationRule.TYPE_COMMUNICATION_FAILED)
                .withReason(Constants.IssueReason.COMMUNICATION_FAILED)
                .get();
        injector.getInstance(IssueRuleFactory.class)
                .withName(Constants.CreationRule.CONNECTION_SETUP_LOST)
                .withType(Constants.IssueCreationRule.TYPE_CONNECTION_SETUP_LOST)
                .withReason(Constants.IssueReason.CONNECTION_SETUP_FAILED)
                .get();
    }
    
    public void createAssignmentRules() {
        executeTransaction(new VoidTransaction() {
            @Override
            protected void doPerform() {
                createAssignmentRulesImp();
            }
        });
    }
    
    private void createAssignmentRulesImp() {
        System.out.println("==> Create assignment rules...");
        Optional<User> bob = userService.findUser(Constants.User.BOB);
        if (!bob.isPresent()) {
            System.out.println("==> Need BOB user but not found...");
            return;
        }
        
        injector.getInstance(AssignmentRuleFactory.class)
                .withName(Constants.AssignmentRule.ASSIGNMENTRULE_DEFAULT_TO_BOB)
                .withDescription(Constants.AssignmentRule.ASSIGNMENTRULE_DEFAULT_TO_BOB)
                .withRuleData(Constants.AssignmentRule.ASSIGNMENTRULE_TO_BOB.replace("@USERID", Long.toString(bob.get().getId())))
                .get();
        
        Optional<User> sam = userService.findUser(Constants.User.SAM);
        if (!sam.isPresent()) {
            System.out.println("==> Need SAM user but not found...");
            return;
        }
        Optional<IssueReason> reason = issueService.findReason(Constants.IssueReason.UNKNOWN_OUTBOUND_SLAVE);
        if (!reason.isPresent()) {
            System.out.println("==> Need issue reason 'Unknown outbound device' but not found...");
            return;
        }
        
        injector.getInstance(AssignmentRuleFactory.class)
                .withName(Constants.AssignmentRule.ASSIGNMENTRULE_TO_SAM_UNBOUND_REASON)
                .withDescription(Constants.AssignmentRule.ASSIGNMENTRULE_TO_SAM_UNBOUND_REASON)
                .withRuleData(Constants.AssignmentRule.ASSIGNMENTRULE_TO_SAM
                        .replace("@USERID", Long.toString(sam.get().getId()))
                        .replace("@REASON", reason.get().getName()))
                .get();
    }

    private void createDeviceGroups(){
        EndDeviceGroup group = injector.getInstance(DeviceGroupFactory.class)
                .withName(Constants.DeviceGroup.NORTH_REGION)
                .withDeviceTypes(Constants.DeviceType.Elster_AS1440.getName(), Constants.DeviceType.Landis_Gyr_ZMD.getName(), Constants.DeviceType.Actaris_SL7000.getName())
                .get();
        injector.getInstance(FavoriteGroupFactory.class).withGroup(group).get();

        group = injector.getInstance(DeviceGroupFactory.class)
                .withName(Constants.DeviceGroup.SOUTH_REGION)
                .withDeviceTypes(Constants.DeviceType.Elster_AS3000.getName(), Constants.DeviceType.Siemens_7ED.getName(), Constants.DeviceType.Iskra_38.getName())
                .get();
        injector.getInstance(FavoriteGroupFactory.class).withGroup(group).get();

        group = injector.getInstance(DeviceGroupFactory.class)
                .withName(Constants.DeviceGroup.ALL_ELECTRICITY_DEVICES)
                .withDeviceTypes(Constants.DeviceType.Elster_AS1440.getName(), Constants.DeviceType.Landis_Gyr_ZMD.getName(), Constants.DeviceType.Actaris_SL7000.getName(), Constants.DeviceType.Elster_AS3000.getName(), Constants.DeviceType.Siemens_7ED.getName(), Constants.DeviceType.Iskra_38.getName())
                .get();
        injector.getInstance(FavoriteGroupFactory.class).withGroup(group).get();
    }

    private void createComPortsAndPools() {
        for (ComServer comServer : store.get(ComServer.class)) {
            injector.getInstance(OutboundTCPComPortFactory.class).withName(Constants.OutboundTcpComPort.TCP_1).withComServer(comServer).get();
            injector.getInstance(OutboundTCPComPortFactory.class).withName(Constants.OutboundTcpComPort.TCP_2).withComServer(comServer).get();
        }
        injector.getInstance(OutboundTCPComPortPoolFactory.class).withName(Constants.ComPortPool.VODAFONE).withComPorts(Constants.OutboundTcpComPort.TCP_1, Constants.OutboundTcpComPort.TCP_2).get();
        injector.getInstance(OutboundTCPComPortPoolFactory.class).withName(Constants.ComPortPool.ORANGE).withComPorts(Constants.OutboundTcpComPort.TCP_1, Constants.OutboundTcpComPort.TCP_2).get();
        injector.getInstance(InboundComPortPoolFactory.class).withName(Constants.ComPortPool.INBOUND_SERVLET_POOL).get();
    }

    private void uploadAllData(String startDate) {
        UploadAllCommand command = injector.getInstance(UploadAllCommand.class);
        command.setStartDate(startDate);
        command.run();
    }

    private void findRegisterTypes(Store store) {
        System.out.println("==> Finding Register Types...");

        store.getRegisterTypes().put(Constants.RegisterTypes.B_F_E_S_M_E, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.B_R_E_S_M_E, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.B_F_E_S_M_E_T1, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.B_F_E_S_M_E_T2, findRegisterType("0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.B_R_E_S_M_E_T1, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0"));
        store.getRegisterTypes().put(Constants.RegisterTypes.B_R_E_S_M_E_T2, findRegisterType("0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.0.72.0"));
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
        RegisterType[] dailyRegisterTypes = {
                store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T1),
                store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T2),
                store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T1),
                store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T2)
        };
        LoadProfileType dailyElectrisity = createLoadProfile(Constants.LoadProfileType.DAILY_ELECTRICITY, "1.0.99.2.0.255", new TimeDuration(1, TimeDuration.TimeUnit.DAYS), Arrays.asList(dailyRegisterTypes));
        store.getLoadProfileTypes().put(Constants.LoadProfileType.DAILY_ELECTRICITY, dailyElectrisity);

        RegisterType[] monthlyRegisterTypes = {
                store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T1),
                store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T2),
                store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T1),
                store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T2)
        };
        LoadProfileType monthlyElectricity = createLoadProfile(Constants.LoadProfileType.MONTHLY_ELECTRICITY, "0.0.98.1.0.255", new TimeDuration(1, TimeDuration.TimeUnit.MONTHS), Arrays.asList(monthlyRegisterTypes));
        store.getLoadProfileTypes().put(Constants.LoadProfileType.MONTHLY_ELECTRICITY, monthlyElectricity);

        RegisterType[] _15minRegisterTypes = {
                store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E),
                store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E)
        };
        LoadProfileType _15minElectricity = createLoadProfile(Constants.LoadProfileType._15_MIN_ELECTRICITY, "1.0.99.1.0.255", new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), Arrays.asList(_15minRegisterTypes));
        store.getLoadProfileTypes().put(Constants.LoadProfileType._15_MIN_ELECTRICITY, _15minElectricity);
    }

    private LoadProfileType createLoadProfile(String name, String obisCode, TimeDuration duartion, Collection<RegisterType> registerTypes) {
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(name, ObisCode.fromString(obisCode), duartion, registerTypes);
        loadProfileType.save();
        return loadProfileType;
    }

    private void createRegisterGroups(Store store) {
        System.out.println("==> Creating Register Groups...");

        RegisterGroup defaultRegisterGroup = masterDataService.newRegisterGroup(Constants.RegisterGroup.DEVICE_DATA);
        defaultRegisterGroup.save();
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T1));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T2));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T1));
        defaultRegisterGroup.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T2));
        store.getRegisterGroups().put(Constants.RegisterGroup.DEVICE_DATA, defaultRegisterGroup);

        RegisterGroup tariff1 = masterDataService.newRegisterGroup(Constants.RegisterGroup.TARIFF_1);
        tariff1.save();
        tariff1.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T1));
        tariff1.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T1));
        store.getRegisterGroups().put(Constants.RegisterGroup.TARIFF_1, tariff1);

        RegisterGroup tariff2 = masterDataService.newRegisterGroup(Constants.RegisterGroup.TARIFF_2);
        tariff2.save();
        tariff2.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T2));
        tariff2.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T2));
        store.getRegisterGroups().put(Constants.RegisterGroup.TARIFF_2, tariff2);
    }

    private void createLogbookTypes(Store store) {
        System.out.println("==> Creating Log Book Types...");
        createLogBookType(store, Constants.LogBookType.GENERIC_LOGBOOK, "0.0.99.98.0.255");
    }

    private void createLogBookType(Store store, String logBookTypeName, String obisCode) {
        LogBookType logBookType = masterDataService.newLogBookType(logBookTypeName, ObisCode.fromString(obisCode));
        logBookType.save();
        store.getLogBookTypes().put(logBookTypeName, logBookType);
    }

    private void createCommunicationTasks(Store store) {
        System.out.println("==> Creating Communication Tasks...");

        RegisterGroup[] registerGroupsForReadAll = {
                store.getRegisterGroups().get(Constants.RegisterGroup.DEVICE_DATA)
        };
        ComTask readAll = taskService.newComTask(Constants.CommunicationTask.READ_ALL);
        readAll.createLoadProfilesTask().loadProfileTypes(new ArrayList<>(store.getLoadProfileTypes().values())).add();
        readAll.createRegistersTask().registerGroups(Arrays.asList(registerGroupsForReadAll)).add();
        readAll.createLogbooksTask().logBookTypes(new ArrayList<>(store.getLogBookTypes().values())).add();
        readAll.createClockTask(ClockTaskType.SETCLOCK).minimumClockDifference(TimeDuration.minutes(5)).maximumClockDifference(TimeDuration.hours(1)).add();
        readAll.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_ALL, readAll);

        ComTask readLoadProfileData = taskService.newComTask(Constants.CommunicationTask.READ_LOAD_PROFILE_DATA);
        readLoadProfileData.createLoadProfilesTask().loadProfileTypes(new ArrayList<>(store.getLoadProfileTypes().values())).add();
        readLoadProfileData.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_LOAD_PROFILE_DATA, readLoadProfileData);

        RegisterGroup[] registerGroupsForRegisterData = {
                store.getRegisterGroups().get(Constants.RegisterGroup.DEVICE_DATA)
        };
        ComTask readRegisterData = taskService.newComTask(Constants.CommunicationTask.READ_REGISTER_DATA);
        readRegisterData.createRegistersTask().registerGroups(Arrays.asList(registerGroupsForRegisterData)).add();
        readRegisterData.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_REGISTER_DATA, readRegisterData);

        ComTask readLogBookData = taskService.newComTask(Constants.CommunicationTask.READ_LOG_BOOK_DATA);
        readLogBookData.createLogbooksTask().logBookTypes(new ArrayList<>(store.getLogBookTypes().values())).add();
        readLogBookData.save();
        store.getComTasks().put(Constants.CommunicationTask.READ_LOG_BOOK_DATA, readLogBookData);

        ComTask topology = taskService.newComTask(Constants.CommunicationTask.TOPOLOGY);
        topology.createTopologyTask(TopologyAction.VERIFY);
        topology.save();
        store.getComTasks().put(Constants.CommunicationTask.TOPOLOGY, topology);
    }

    private void createCommunicationSchedules(Store store){
        System.out.println("==> Creating Communication Schedules...");
        createCommunicationSchedule(store, Constants.CommunicationSchedules.DAILY_READ_ALL, Constants.CommunicationTask.READ_ALL, TimeDuration.days(1));
    }

    private void createCommunicationSchedule(Store store, String comScheduleName, String taskName, TimeDuration every) {
        //long timeBefore = System.currentTimeMillis() - every.getMilliSeconds() - DateTimeConstants.MILLIS_PER_DAY;
        Instant timeBefore = Instant.now().minusMillis(every.getMilliSeconds()).minus(1, ChronoUnit.DAYS);
        ComSchedule comSchedule = schedulingService.newComSchedule(comScheduleName, new TemporalExpression(every), timeBefore).build();
        comSchedule.addComTask(store.getComTasks().get(taskName));
        LocalDateTime startOn = LocalDateTime.now();
        startOn = startOn.withSecond(0).withMinute(0).withHour(0);
        comSchedule.setStartDate(startOn.toInstant(ZoneOffset.UTC));
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
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T1));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_F_E_S_M_E_T2));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T1));
        deviceType.addRegisterType(store.getRegisterTypes().get(Constants.RegisterTypes.B_R_E_S_M_E_T2));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(Constants.LoadProfileType.DAILY_ELECTRICITY));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(Constants.LoadProfileType.MONTHLY_ELECTRICITY));
        deviceType.addLoadProfileType(store.getLoadProfileTypes().get(Constants.LoadProfileType._15_MIN_ELECTRICITY));
        deviceType.addLogBookType(store.getLogBookTypes().get(Constants.LogBookType.GENERIC_LOGBOOK));
        deviceType.save();

        createDeviceConfiguration(store, deviceType, Constants.DeviceConfiguration.EXTENDED, false);
        createDeviceConfiguration(store, deviceType, Constants.DeviceConfiguration.DEFAULT, true);
    }

    private void createDeviceConfiguration(Store store, DeviceType deviceType, String name, boolean hasDevices) {
        System.out.println("==> Creating Default Device Configuration...");
        DeviceType.DeviceConfigurationBuilder configBuilder = deviceType.newConfiguration(name);
        configBuilder.description(name + " configuration for device type: " + deviceType.getName());
        configBuilder.canActAsGateway(true);
        configBuilder.gatewayType(GatewayType.HOME_AREA_NETWORK);
        configBuilder.isDirectlyAddressable(true);

        addRegisterSpecsToDeviceConfiguration(configBuilder, store,
                Constants.RegisterTypes.B_F_E_S_M_E,
                Constants.RegisterTypes.B_R_E_S_M_E,
                Constants.RegisterTypes.B_F_E_S_M_E_T1,
                Constants.RegisterTypes.B_F_E_S_M_E_T2,
                Constants.RegisterTypes.B_R_E_S_M_E_T1,
                Constants.RegisterTypes.B_R_E_S_M_E_T2
        );
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(Constants.LoadProfileType._15_MIN_ELECTRICITY));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(Constants.LoadProfileType.DAILY_ELECTRICITY));
        configBuilder.newLoadProfileSpec(store.getLoadProfileTypes().get(Constants.LoadProfileType.MONTHLY_ELECTRICITY));
        configBuilder.newLogBookSpec(store.getLogBookTypes().get(Constants.LogBookType.GENERIC_LOGBOOK));
        DeviceConfiguration configuration = configBuilder.add();

        addConnectionMethodToDeviceConfiguration(store, configuration);
        createSecurityPropertySetForDeviceConfiguration(configuration);
        setProtocolDialectConfigurationProperties(configuration);
        enableComTasksOnDeviceConfiguration(configuration, Constants.CommunicationTask.READ_ALL, Constants.CommunicationTask.READ_LOAD_PROFILE_DATA, Constants.CommunicationTask.READ_REGISTER_DATA, Constants.CommunicationTask.READ_LOG_BOOK_DATA, Constants.CommunicationTask.TOPOLOGY);
        configureChannelsForLoadProfileSpec(configuration);
        configuration.activate();
        configuration.save();
        if (hasDevices){
            createDevicesForDeviceConfiguration(store, configuration);
        }
    }

    /**
     * We expect that required security property set is the first element in collection
     */
    public void enableComTasksOnDeviceConfiguration(DeviceConfiguration configuration, String... names){
        if (names != null) {
            for (String name : names) {
                configuration.enableComTask(injector.getInstance(ComTaskFinder.class).withName(name).find(), configuration.getSecurityPropertySets().get(0))
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

    private SecurityPropertySet createSecurityPropertySetForDeviceConfiguration(DeviceConfiguration configuration) {
        SecurityPropertySet securityPropertySet = configuration.createSecurityPropertySet(Constants.SecuritySet.NO_SECURITY).authenticationLevel(0).encryptionLevel(0).build();
        securityPropertySet.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        securityPropertySet.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        securityPropertySet.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1);
        securityPropertySet.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2);
        securityPropertySet.update();

        SecurityPropertySet strongSecSet = configuration.createSecurityPropertySet("High level authentication (MD5) and encryption").authenticationLevel(3).encryptionLevel(3).build();
        strongSecSet.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        strongSecSet.addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2);
        strongSecSet.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1);
        strongSecSet.addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2);
        strongSecSet.update();
        return securityPropertySet;
    }

    private void addConnectionMethodToDeviceConfiguration(Store store, DeviceConfiguration configuration) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp").get();
        configuration
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(60, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(store.get(OutboundComPortPool.class, Constants.ComPortPool.ORANGE))
                .addProperty("host", store.getProperty("host"))
                .addProperty("portNumber", new BigDecimal(4059))
                .addProperty("connectionTimeout", TimeDuration.minutes(1))
                .asDefault(true).build();
    }

    private void configureChannelsForLoadProfileSpec(DeviceConfiguration devConfiguration) {
        for (LoadProfileSpec loadProfileSpec : devConfiguration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            for (ChannelType channelType : availableChannelTypes) {
                devConfiguration.createChannelSpec(channelType, channelType.getPhenomenon(), loadProfileSpec).setMultiplier(new BigDecimal(1)).setOverflow(new BigDecimal(9999999999L)).setNbrOfFractionDigits(0).add();
            }
        }
    }

    private void createDevicesForDeviceConfiguration(Store store, DeviceConfiguration configuration){
        System.out.println("==> Creating Devices for Configuration...");
        String deviceTypeName = configuration.getDeviceType().getName();
        for (int i = 1; i <= 1 /*Constants.DeviceType.from(deviceTypeName).get().getDeviceCount()*/; i++) {
            deviceCounter++;
            String serialNumber = "01000001" + String.format("%04d", deviceCounter);
            String mrid = Constants.Device.STANDARD_PREFIX +  serialNumber;
            createDevice(store, configuration, mrid, serialNumber);
        }
    }

    private void createDevice(Store store, DeviceConfiguration configuration, String mrid, String serialNumber){
        Device device = injector.getInstance(DeviceFactory.class)
                .withMrid(mrid)
                .withSerialNumber(serialNumber)
                .withDeviceConfiguration(configuration)
                .withComSchedules(Constants.CommunicationSchedules.DAILY_READ_ALL)
                .get();
        addConnectionMethodToDevice(store, configuration, device);

        setSecurityPropertiesForDevice(configuration, device);
    }

    private void setSecurityPropertiesForDevice(DeviceConfiguration configuration, Device device) {
        for (SecurityPropertySet securityPropertySet : configuration.getSecurityPropertySets()) {
            TypedProperties typedProperties = TypedProperties.empty();
            typedProperties.setProperty("ClientMacAddress", new BigDecimal(1));
            device.setSecurityProperties(securityPropertySet, typedProperties);
       }
    }

    private void addConnectionMethodToDevice(Store store, DeviceConfiguration configuration, Device device) {
        PartialScheduledConnectionTask connectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        int portNumber = 4059;
        String deviceTypeName = device.getDeviceType().getName();
        ScheduledConnectionTask deviceConnectionTask = device.getScheduledConnectionTaskBuilder(connectionTask)
                .setComPortPool(store.get(OutboundComPortPool.class, Constants.DeviceType.getComPortPoolName(deviceTypeName)))
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setNextExecutionSpecsFrom(null)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .setProperty("host", store.getProperty("host"))
                .setProperty("portNumber", new BigDecimal(portNumber))
                .setProperty("connectionTimeout", TimeDuration.minutes(1))
                .setSimultaneousConnectionsAllowed(false)
                .add();
        connectionTaskService.setDefaultConnectionTask(deviceConnectionTask);
    }

    public void createIssuesImpl(){
        List<Device> devices = deviceService.findAllDevices(Condition.TRUE).find();
        for (Device device : devices) {
            IssueFactory issueGenerator = injector.getInstance(IssueFactory.class);
            if (device.getmRID().startsWith(Constants.Device.STANDARD_PREFIX)){
                issueGenerator.withDevice(device).withAssignee(device.getId() % 7 == 0 ? Constants.User.SAM : null).get();
            }
        }
    }

    public void createUserManagementImpl(){
        injector.getInstance(UserFactory.class).withName(Constants.User.MELISSA).withRoles(Constants.UserRoles.METER_EXPERT).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.SAM).withLanguage(Locale.US.toLanguageTag()).withRoles(Constants.UserRoles.ADMINISTRATORS).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.MONICA).withRoles(Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.PIETER).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.JOLIEN).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.INGE).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.KOEN).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.SEBASTIEN).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.VEERLE).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.KURT).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.EDUARDO).withLanguage(Locale.US.toLanguageTag()).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        injector.getInstance(UserFactory.class).withName(Constants.User.BOB).withLanguage(Locale.US.toLanguageTag()).withRoles(Constants.UserRoles.ADMINISTRATORS, Constants.UserRoles.METER_EXPERT, Constants.UserRoles.METER_OPERATOR).get();
        User admin = userService.findUser("admin").get();
        admin.setPassword("D3moAdmin");
        admin.save();
    }

    public void createValidationSetupImpl(){
        ValidationRuleSet ruleSet = injector.getInstance(ValidationRuleSetFactory.class)
                .withName(Constants.Validation.RULE_SET_NAME)
                .withDescription(Constants.Validation.RULE_SET_DESCRIPTION)
                .get();
        String prefix = Constants.Device.MOCKED_VALIDATION_DEVICE;
        if (deviceService.findByUniqueMrid(prefix + Constants.Device.MOCKED_VALIDATION_SERIAL_NUMBER) == null){
            createMockedDataDeviceImpl(prefix, Constants.Device.MOCKED_VALIDATION_SERIAL_NUMBER);
        }
        List<DeviceConfiguration> configurations = deviceConfigurationService.getLinkableDeviceConfigurations(ruleSet);
        for (DeviceConfiguration configuration : configurations) {
            if (configuration.getName().equals(Constants.DeviceConfiguration.DEFAULT)){
                continue;
            }
            System.out.println("==> Validation rule set added to: " + configuration.getName() + " (id = " + configuration.getId() + ")");
            configuration.addValidationRuleSet(ruleSet);
            configuration.save();
        }

        Condition devicesForActivation = where("mRID").like(Constants.Device.STANDARD_PREFIX + "%");
        devicesForActivation = devicesForActivation.or(where("mRID").like(Constants.Device.MOCKED_VALIDATION_DEVICE + "%"));

        List<Meter> meters = meteringService.getMeterQuery().select(devicesForActivation);
        for (Meter meter : meters) {
            validationService.activateValidation(meter);
        }
    }

    public void createDeliverDataSetupImpl(){
        injector.getInstance(DataExportTaskFactory.class).withName(Constants.DataExportTask.DEFAULT_PREFIX + Constants.DeviceGroup.NORTH_REGION)
                .withGroup(Constants.DeviceGroup.NORTH_REGION).get();
        injector.getInstance(DataExportTaskFactory.class).withName(Constants.DataExportTask.DEFAULT_PREFIX + Constants.DeviceGroup.SOUTH_REGION)
                .withGroup(Constants.DeviceGroup.SOUTH_REGION).get();
    }

    public void createKpi(){
        injector.getInstance(DynamicKpiFactory.class).withGroup(store.get(EndDeviceGroup.class, gr -> gr.getName().equals(Constants.DeviceGroup.NORTH_REGION))).get();
        injector.getInstance(DynamicKpiFactory.class).withGroup(store.get(EndDeviceGroup.class, gr -> gr.getName().equals(Constants.DeviceGroup.SOUTH_REGION))).get();
    }

    public void createApplicationServerImpl(final String appServerName){
        injector.getInstance(AppServerFactory.class).withName(appServerName.toUpperCase()).get();
    }

    private void createMockedDataDeviceImpl(String prefix, String serialNumber){
        String deviceTypeName = Constants.DeviceType.Elster_AS1440.getName();
        Optional<DeviceType> deviceTypeByName = deviceConfigurationService.findDeviceTypeByName(deviceTypeName);
        if (!deviceTypeByName.isPresent()){
            throw new UnableToCreate("Unable to find corresponding device type with name: " + deviceTypeName);
        }
        Optional<DeviceConfiguration> deviceConfiguration = deviceTypeByName.get().getConfigurations().stream().filter(dc -> {
            String deviceConfigName = Constants.Device.MOCKED_VALIDATION_DEVICE.equals(prefix) ? Constants.DeviceConfiguration.EXTENDED : Constants.DeviceConfiguration.DEFAULT;
            return dc.getName().equals(deviceConfigName);
        }).findFirst();
        if (!deviceConfiguration.isPresent()){
            throw new UnableToCreate("Unable to find corresponding device configuration with name: " + deviceTypeName);
        }
        String mrid = prefix + serialNumber;
        Device device = deviceService.findByUniqueMrid(mrid);
        if (device != null){
            throw new UnableToCreate("Device with mrid: " + mrid + ", already exists");
        }
        injector.getInstance(DeviceFactory.class)
                .withMrid(mrid)
                .withDeviceConfiguration(deviceConfiguration.get())
                .withSerialNumber(serialNumber)
                .get();
    }

    private void activateValidation(String mrid, String ruleSetName){
        Optional<ValidationRuleSet> existingRuleSet = validationService.getValidationRuleSet(ruleSetName);
        if (!existingRuleSet.isPresent()){
            throw new UnableToCreate("Unable to find validation ruleset with name" + ruleSetName);
        }
        Optional<Meter> meter = meteringService.findMeter(mrid);
        if (!meter.isPresent()){
            throw new UnableToCreate("Unable to find meter with mrid" + mrid);
        }
        validationService.activateValidation(meter.get());
    }

    private void createNtaConfigImpl(){
        injector.getInstance(NTASimToolFactory.class).get();
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
        this.issueAssignmentService = issueService.getIssueAssignmentService();
        this.issueCreationService = issueService.getIssueCreationService();
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

    @Reference
    @SuppressWarnings("unused")
    public final void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setFavoritesService(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
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