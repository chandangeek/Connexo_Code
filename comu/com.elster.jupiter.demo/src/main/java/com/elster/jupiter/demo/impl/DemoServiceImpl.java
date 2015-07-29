package com.elster.jupiter.demo.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.commands.*;
import com.elster.jupiter.demo.impl.commands.devices.CreateDeviceCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateValidationDeviceCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddIntervalChannelReadingsCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddNoneIntervalChannelReadingsCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddRegisterReadingsCommand;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;

@Component(name = "com.elster.jupiter.demo", service = {DemoServiceImpl.class}, property = {
        "osgi.command.scope=demo",
        "osgi.command.function=createDemoData",
        "osgi.command.function=createUserManagement",
        "osgi.command.function=createApplicationServer",
        "osgi.command.function=createA3Device",
        "osgi.command.function=createNtaConfig",
        "osgi.command.function=createMockedDataDevice",
        "osgi.command.function=createValidationDevice",
        "osgi.command.function=createDeviceType",
        "osgi.command.function=createDeliverDataSetup",
        "osgi.command.function=createCollectRemoteDataSetup",
        "osgi.command.function=createValidationSetup",
        "osgi.command.function=createAssignmentRules",
        "osgi.command.function=addIntervalChannelReadings",
        "osgi.command.function=addNoneIntervalChannelReadings",
        "osgi.command.function=addRegisterReadings",
        "osgi.command.function=createG3Gateway",
        "osgi.command.function=createG3SlaveDevice"
}, immediate = true)
public class DemoServiceImpl {
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
    private volatile Clock clock;
    private volatile IdsService idsService;
    private volatile FirmwareService firmwareService;

    private Injector injector;
    private boolean reThrowEx = false;

    public DemoServiceImpl(){

    }

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
            FavoritesService favoritesService,
            Clock clock,
            IdsService idsService,
            FirmwareService firmwareService) {
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
        setClock(clock);
        setIdsService(idsService);
        setFirmwareService(firmwareService);

        activate();
        reThrowEx = true;
    }

    @Activate
    public void activate(){
        this.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
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
                bind(Clock.class).toInstance(clock);
                bind(IdsService.class).toInstance(idsService);
                bind(FirmwareService.class).toInstance(firmwareService);
            }
        });
        Builders.initWith(this.injector);
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
    public final void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
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

    @Reference
    @SuppressWarnings("unused")
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    @SuppressWarnings("unused")
    public final void setIdsService(IdsService idsService) {
        this.idsService = idsService;
    }

    private void executeTransaction(Runnable toRunInsideTransaction) {
        setPrincipal();
        try {
            System.out.println("Starting execution");
            transactionService.execute(() ->{
                toRunInsideTransaction.run();
                return null;
            });
            System.out.println("Transaction completed successfully");
        } catch (Exception ex) {
            System.out.println("Transaction failed!");
            ex.printStackTrace();
            if (reThrowEx) throw ex;
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
        return new ConsoleUser();
    }

    @SuppressWarnings("unused")
    public void createA3Device(){
        executeTransaction(() -> {
            CreateA3DeviceCommand command = injector.getInstance(CreateA3DeviceCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createG3Gateway(String mrid){
        executeTransaction(() -> {
            CreateG3GatewayCommand command = injector.getInstance(CreateG3GatewayCommand.class);
            if (mrid != null){ //Otherwise default mrId is Used
                command.setGatewayMrid(mrid);
            }
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createG3SlaveDevice(){
        executeTransaction(() -> {
            CreateG3SlaveCommand command = injector.getInstance(CreateG3SlaveCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void addIntervalChannelReadings(String mrid, String startDate, String path) {
        executeTransaction(() -> {
            AddIntervalChannelReadingsCommand command = injector.getInstance(AddIntervalChannelReadingsCommand.class);
            command.setMeter(mrid);
            command.setStartDate(startDate);
            command.setSource(path);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void addNoneIntervalChannelReadings(String mrid, String startDate, String path) {
        executeTransaction(() -> {
            AddNoneIntervalChannelReadingsCommand command = injector.getInstance(AddNoneIntervalChannelReadingsCommand.class);
            command.setMeter(mrid);
            command.setStartDate(startDate);
            command.setSource(path);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void addRegisterReadings(String mrid, String startDate, String path) {
        executeTransaction(() -> {
            AddRegisterReadingsCommand command = injector.getInstance(AddRegisterReadingsCommand.class);
            command.setMeter(mrid);
            command.setStartDate(startDate);
            command.setSource(path);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createApplicationServer(String name){
        executeTransaction(() -> {
            CreateApplicationServerCommand command = injector.getInstance(CreateApplicationServerCommand.class);
            command.setName(name);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createUserManagement() {
        executeTransaction(() -> {
            CreateUserManagementCommand command = injector.getInstance(CreateUserManagementCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDemoData(){
        System.err.println("Usage: createDemoData <comServerName> <host> <startDate, e.g. 2015-01-01>");
    }

    @SuppressWarnings("unused")
    public void createDemoData(String comServerName, String host, String startDate){
        executeTransaction(() -> {
            CreateDemoDataCommand command = injector.getInstance(CreateDemoDataCommand.class);
            command.setComServerName(comServerName);
            command.setHost(host);
            command.setStartDate(startDate);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createCollectRemoteDataSetup(String comServerName, String host){
        executeTransaction(() -> {
            CreateCollectRemoteDataSetupCommand command = injector.getInstance(CreateCollectRemoteDataSetupCommand.class);
            command.setComServerName(comServerName);
            command.setHost(host);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createValidationSetup(){
        executeTransaction(() -> {
            CreateValidationSetupCommand command = injector.getInstance(CreateValidationSetupCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createNtaConfig(){
        executeTransaction(() -> {
            CreateNtaConfigCommand command = injector.getInstance(CreateNtaConfigCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createAssignmentRules(){
        executeTransaction(() -> {
            CreateAssignmentRulesCommand command = injector.getInstance(CreateAssignmentRulesCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createValidationDevice(String serialNumber){
        executeTransaction(() -> {
            CreateValidationDeviceCommand command = injector.getInstance(CreateValidationDeviceCommand.class);
            command.setSerialNumber(serialNumber);
            command.setMridPrefix(Constants.Device.MOCKED_VALIDATION_DEVICE);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createMockedDataDevice(String serialNumber){
        executeTransaction(() -> {
            CreateDeviceCommand command = injector.getInstance(CreateDeviceCommand.class);
            command.setSerialNumber(serialNumber);
            command.setMridPrefix(Constants.Device.MOCKED_REALISTIC_DEVICE);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDeviceType(String deviceTypeName, String host){
        executeTransaction(() -> {
            CreateDeviceTypeCommand command = injector.getInstance(CreateDeviceTypeCommand.class);
            command.setDeviceTypeName(deviceTypeName);
            command.setHost(host);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDeliverDataSetup(){
        executeTransaction(() -> {
            CreateDeliverDataSetupCommand command = injector.getInstance(CreateDeliverDataSetupCommand.class);
            command.run();
        });
    }

}
