/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.demo.impl.commands.AddLocationInfoToDevicesCommand;
import com.elster.jupiter.demo.impl.commands.CreateA3DeviceCommand;
import com.elster.jupiter.demo.impl.commands.CreateAlarmCreationRuleCommand;
import com.elster.jupiter.demo.impl.commands.CreateApplicationServerCommand;
import com.elster.jupiter.demo.impl.commands.CreateCollectRemoteDataSetupCommand;
import com.elster.jupiter.demo.impl.commands.CreateDataLoggerSetupCommand;
import com.elster.jupiter.demo.impl.commands.CreateDefaultDeviceLifeCycleCommand;
import com.elster.jupiter.demo.impl.commands.CreateDeliverDataSetupCommand;
import com.elster.jupiter.demo.impl.commands.CreateDemoDataCommand;
import com.elster.jupiter.demo.impl.commands.CreateDemoUserCommand;
import com.elster.jupiter.demo.impl.commands.CreateDeviceTypeCommand;
import com.elster.jupiter.demo.impl.commands.CreateDevicesCommand;
import com.elster.jupiter.demo.impl.commands.CreateEstimationSetupCommand;
import com.elster.jupiter.demo.impl.commands.CreateG3DemoBoardCommand;
import com.elster.jupiter.demo.impl.commands.CreateImporterDirectoriesCommand;
import com.elster.jupiter.demo.impl.commands.CreateImportersCommand;
import com.elster.jupiter.demo.impl.commands.CreateMetrologyConfigurationsCommand;
import com.elster.jupiter.demo.impl.commands.CreateMultiElementDeviceSetupCommand;
import com.elster.jupiter.demo.impl.commands.CreateNetworkManagementCommand;
import com.elster.jupiter.demo.impl.commands.CreateNetworkTopologyCommand;
import com.elster.jupiter.demo.impl.commands.CreateNtaConfigCommand;
import com.elster.jupiter.demo.impl.commands.CreatePowerUserCommand;
import com.elster.jupiter.demo.impl.commands.CreateRegisterDeviceCommand;
import com.elster.jupiter.demo.impl.commands.CreateUserManagementCommand;
import com.elster.jupiter.demo.impl.commands.CreateValidationSetupCommand;
import com.elster.jupiter.demo.impl.commands.FileImportCommand;
import com.elster.jupiter.demo.impl.commands.SetupFirmwareManagementCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateDeviceCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateG3GatewayCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateG3SlaveCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateSPEDeviceCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateValidationDeviceCommand;
import com.elster.jupiter.demo.impl.commands.tou.CreateBelgianMarketTimeOfUseDataCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddIntervalChannelReadingsCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddNoneIntervalChannelReadingsCommand;
import com.elster.jupiter.demo.impl.commands.upload.AddRegisterReadingsCommand;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.PassphraseFactory;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.validation.ValidationService;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.Principal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component(name = "com.elster.jupiter.demo", service = {DemoServiceImpl.class}, property = {
        "osgi.command.scope=demo",
        "osgi.command.function=createDemoData",
        "osgi.command.function=createUserManagement",
        "osgi.command.function=createApplicationServer",
        "osgi.command.function=createA3Device",
        "osgi.command.function=createBelgianMarketTimeOfUseData",
        "osgi.command.function=createNtaConfig",
        "osgi.command.function=createMockedDataDevice",
        "osgi.command.function=createValidationDevice",
        "osgi.command.function=createDeviceType",
        "osgi.command.function=createDeliverDataSetup",
        "osgi.command.function=createCollectRemoteDataSetup",
        "osgi.command.function=createValidationSetup",
        "osgi.command.function=createEstimationSetup",
        "osgi.command.function=createAssignmentRules",
        "osgi.command.function=addIntervalChannelReadings",
        "osgi.command.function=addNoneIntervalChannelReadings",
        "osgi.command.function=addRegisterReadings",
        "osgi.command.function=createG3DemoBoardDevices",
        "osgi.command.function=createG3Gateway",
        "osgi.command.function=createG3SlaveAS3000",
        "osgi.command.function=createG3SlaveAS220",
        "osgi.command.function=createDefaultDeviceLifeCycle",
        "osgi.command.function=setUpFirmwareManagement",
        "osgi.command.function=createImporters",
        "osgi.command.function=createImportDirectories",
        "osgi.command.function=createDemoUser",
        "osgi.command.function=createDataLogger",
        "osgi.command.function=createMultiElementDevice",
        "osgi.command.function=importCalendar",
        "osgi.command.function=setDeviceLocations",
        "osgi.command.function=createSPEDevice",
        "osgi.command.function=createDefaultAlarmRule",
        "osgi.command.function=createMetrologyConfigurations",
        "osgi.command.function=createPowerUser",
        "osgi.command.function=createRegisterDevice",
        "osgi.command.function=createNetworkTopology",
        "osgi.command.function=createNetworkManagement",
        "osgi.command.function=createDevices"
}, immediate = true)
public class DemoServiceImpl {
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile UserService userService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile MasterDataService masterDataService;
    private volatile MeteringService meteringService;
    private volatile TaskService taskService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile SchedulingService schedulingService;
    private volatile LicenseService licenseService;
    private volatile DataModel dataModel;
    private volatile IssueService issueService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile KpiService kpiService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile IssueDataValidationService issueDataValidationService;
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
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile FileImportService fileImportService;
    private volatile SearchService searchService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile CalendarService calendarService;
    private volatile com.elster.jupiter.tasks.TaskService platformTaskService;
    private volatile DataQualityKpiService dataQualityKpiService;
    private volatile RegisteredDevicesKpiService registeredDevicesKpiService;
    private volatile CommandRuleService commandRuleService;
    private volatile UsagePointLifeCycleService usagePointLifeCycleService;
    private volatile UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private volatile SecurityManagementService securityManagementService;
    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile PassphraseFactory passphraseFactory;
    private volatile NlsService nlsService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile TopologyService topologyService;
    private volatile Thesaurus thesaurus;

    private Injector injector;

    private boolean reThrowEx = false;

    public DemoServiceImpl() {
    }

    @Inject
    public DemoServiceImpl(
            EngineConfigurationService engineConfigurationService,
            UserService userService,
            ValidationService validationService,
            EstimationService estimationService,
            TransactionService transactionService,
            ThreadPrincipalService threadPrincipalService,
            ProtocolPluggableService protocolPluggableService,
            MasterDataService masterDataService,
            MeteringService meteringService,
            TaskService taskService,
            DeviceConfigurationService deviceConfigurationService,
            DeviceService deviceService,
            ConnectionTaskService connectionTaskService,
            CommunicationTaskService communicationTaskService,
            SchedulingService schedulingService,
            LicenseService licenseService,
            OrmService ormService,
            IssueService issueService,
            MeteringGroupsService meteringGroupsService,
            KpiService kpiService,
            IssueDataCollectionService issueDataCollectionService,
            IssueDataValidationService issueDataValidationService,
            DataCollectionKpiService dataCollectionKpiService,
            AppService appService,
            MessageService messageService,
            DataExportService dataExportService,
            CronExpressionParser cronExpressionParser,
            TimeService timeService,
            FavoritesService favoritesService,
            Clock clock,
            IdsService idsService,
            FirmwareService firmwareService,
            FiniteStateMachineService finiteStateMachineService,
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            DeviceLifeCycleService deviceLifeCycleService,
            FileImportService fileImportService,
            SearchService searchService,
            MetrologyConfigurationService metrologyConfigurationService,
            CustomPropertySetService customPropertySetService,
            DeviceMessageSpecificationService deviceMessageSpecificationService,
            CalendarService calendarService,
            com.elster.jupiter.tasks.TaskService platformTaskService,
            CommandRuleService commandRuleService,
            DataQualityKpiService dataQualityKpiService,
            RegisteredDevicesKpiService registeredDevicesKpiService,
            UsagePointLifeCycleService usagePointLifeCycleService,
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
            SecurityManagementService securityManagementService,
            PassphraseFactory passphraseFactory,
            TopologyService topologyService,
            NlsService nlsService,
            DeviceAlarmService deviceAlarmService,
            UsagePointConfigurationService usagePointConfigurationService,
            UsagePointIssueDataValidationService usagePointIssueDataValidationService,
            MeteringTranslationService meteringTranslationService) {
        this();
        setEngineConfigurationService(engineConfigurationService);
        setUserService(userService);
        setValidationService(validationService);
        setEstimationService(estimationService);
        setTransactionService(transactionService);
        setThreadPrincipalService(threadPrincipalService);
        setProtocolPluggableService(protocolPluggableService);
        setMasterDataService(masterDataService);
        setMeteringService(meteringService);
        setTaskService(taskService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceService(deviceService);
        setConnectionTaskService(connectionTaskService);
        setCommunicationTaskService(communicationTaskService);
        setSchedulingService(schedulingService);
        setLicenseService(licenseService);
        setOrmService(ormService);
        setIssueService(issueService);
        setMeteringGroupsService(meteringGroupsService);
        setKpiService(kpiService);
        setIssueDataCollectionService(issueDataCollectionService);
        setIssueDataValidationService(issueDataValidationService);
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
        setFiniteStateMachineService(finiteStateMachineService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setDeviceLifeCycleService(deviceLifeCycleService);
        setFileImportService(fileImportService);
        setSearchService(searchService);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setCustomPropertySetService(customPropertySetService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setCalendarService(calendarService);
        setPlatformTaskService(platformTaskService);
        setDataCollectionKpiService(dataCollectionKpiService);
        setTopologyService(topologyService);
        setCommandRuleService(commandRuleService);
        setDataQualityKpiService(dataQualityKpiService);
        setRegisteredDevicesKpiService(registeredDevicesKpiService);
        setUsagePointLifeCycleService(usagePointLifeCycleService);
        setUsagePointLifeCycleConfigurationService(usagePointLifeCycleConfigurationService);
        setSecurityManagementService(securityManagementService);
        setPassphraseFactory(passphraseFactory);
        setTopologyService(topologyService);
        setNlsService(nlsService);
        setDeviceAlarmService(deviceAlarmService);
        setUsagePointConfigurationService(usagePointConfigurationService);
        setUsagePointIssueDataValidationService(usagePointIssueDataValidationService);
        setMeteringTranslationService(meteringTranslationService);
        activate();
        reThrowEx = true;
    }

    @Activate
    public void activate() {
        this.injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
                bind(UserService.class).toInstance(userService);
                bind(ValidationService.class).toInstance(validationService);
                bind(EstimationService.class).toInstance(estimationService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(MasterDataService.class).toInstance(masterDataService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(TaskService.class).toInstance(taskService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(LicenseService.class).toInstance(licenseService);
                bind(DataModel.class).toInstance(dataModel);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueAssignmentService.class).toInstance(issueAssignmentService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(KpiService.class).toInstance(kpiService);
                bind(IssueDataCollectionService.class).toInstance(issueDataCollectionService);
                bind(IssueDataValidationService.class).toInstance(issueDataValidationService);
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
                bind(FiniteStateMachineService.class).toInstance(finiteStateMachineService);
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
                bind(DeviceLifeCycleService.class).toInstance(deviceLifeCycleService);
                bind(FileSystem.class).toInstance(FileSystems.getDefault());
                bind(FileImportService.class).toInstance(fileImportService);
                bind(EstimationService.class).toInstance(estimationService);
                bind(SearchService.class).toInstance(searchService);
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                bind(CalendarService.class).toInstance(calendarService);
                bind(com.elster.jupiter.tasks.TaskService.class).toInstance(platformTaskService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(CommandRuleService.class).toInstance(commandRuleService);
                bind(DataQualityKpiService.class).toInstance(dataQualityKpiService);
                bind(RegisteredDevicesKpiService.class).toInstance(registeredDevicesKpiService);
                bind(UsagePointLifeCycleService.class).toInstance(usagePointLifeCycleService);
                bind(UsagePointLifeCycleConfigurationService.class).toInstance(usagePointLifeCycleConfigurationService);
                bind(SecurityManagementService.class).toInstance(securityManagementService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(NlsService.class).toInstance(nlsService);
                bind(DeviceAlarmService.class).toInstance(deviceAlarmService);
                bind(UsagePointIssueDataValidationService.class).toInstance(usagePointIssueDataValidationService);
                bind(UsagePointConfigurationService.class).toInstance(usagePointConfigurationService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MeteringTranslationService.class).toInstance(meteringTranslationService);
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
    public final void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
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
    public final void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
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
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setIssueDataValidationService(IssueDataValidationService issueDataValidationService) {
        this.issueDataValidationService = issueDataValidationService;
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

    @Reference
    @SuppressWarnings("unused")
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }


    @Reference
    @SuppressWarnings("unused")
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setPlatformTaskService(com.elster.jupiter.tasks.TaskService platformTaskService) {
        this.platformTaskService = platformTaskService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataQualityKpiService(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setRegisteredDevicesKpiService(RegisteredDevicesKpiService registeredDevicesKpiService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
    }


    @Reference
    public void setCommandRuleService(CommandRuleService commandRuleService) {
        this.commandRuleService = commandRuleService;
    }

    @Reference
    public void setUsagePointLifeCycleService(UsagePointLifeCycleService usagePointLifeCycleService) {
        this.usagePointLifeCycleService = usagePointLifeCycleService;
    }

    @Reference
    public void setUsagePointLifeCycleConfigurationService(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setPassphraseFactory(PassphraseFactory passphraseFactory) {
        this.passphraseFactory = passphraseFactory;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus("DLD", Layer.DOMAIN);
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setUsagePointIssueDataValidationService(UsagePointIssueDataValidationService usagePointIssueDataValidationService) {
        this.usagePointIssueDataValidationService = usagePointIssueDataValidationService;
    }

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    private void executeTransaction(Runnable toRunInsideTransaction) {
        setPrincipal();
        try {
            System.out.println("Starting execution");
            transactionService.execute(() -> {
                toRunInsideTransaction.run();
                return null;
            });
            System.out.println("Transaction completed successfully");
        } catch (Exception ex) {
            System.out.println("Transaction failed!");
            ex.printStackTrace();
            if (reThrowEx) {
                throw ex;
            }
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
    public void createA3Device() {
        executeTransaction(() -> {
            CreateA3DeviceCommand command = injector.getInstance(CreateA3DeviceCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDataLogger(String dataLoggerMrid, String dataLoggerSerial, int numberOfSlaves) {
        executeTransaction(() -> {
            CreateDataLoggerSetupCommand command = injector.getInstance(CreateDataLoggerSetupCommand.class);
            if (!Strings.isNullOrEmpty(dataLoggerMrid)) {
                command.setDataLoggerMrid(dataLoggerMrid);
            }
            if (!Strings.isNullOrEmpty(dataLoggerSerial)) {
                command.setDataLoggerSerial(dataLoggerSerial);
            }
            command.setNumberOfSlaves(numberOfSlaves);

            command.run();
        });
    }


    @SuppressWarnings("unused")
    public void createMultiElementDevice(String name, String serial) {
        executeTransaction(() -> {
            CreateMultiElementDeviceSetupCommand command = injector.getInstance(CreateMultiElementDeviceSetupCommand.class);
            if (!Strings.isNullOrEmpty(name)) {
                command.setName(name);
            }
            if (!Strings.isNullOrEmpty(serial)) {
                command.setSerial(serial);
            }
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createNetworkTopology(String rootDeviceName, int childcount, int levelcount) {
        CreateNetworkTopologyCommand command = injector.getInstance(CreateNetworkTopologyCommand.class);
        if (!Strings.isNullOrEmpty(rootDeviceName)) {
            command.setGatewayMrid(rootDeviceName);
        }
        command.setDeviceCount(childcount);
        command.setLevelCount(levelcount);
        command.runInTransaction();
    }

    @SuppressWarnings("unused")
    public void createNetworkManagement(int rootCount, int childcount, int levelcount) {
        CreateNetworkManagementCommand command = injector.getInstance(CreateNetworkManagementCommand.class);
        command.setMasterDeviceCount(rootCount);
        command.setSlaveDeviceCount(childcount);
        command.setLevelCount(levelcount);
        command.runInTransaction();
    }


    @SuppressWarnings("unused")
    public void createG3DemoBoardDevices() {
        executeTransaction(() -> {
            CreateG3DemoBoardCommand command = injector.getInstance(CreateG3DemoBoardCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createG3Gateway(String name) {
        executeTransaction(() -> {
            CreateG3GatewayCommand command = injector.getInstance(CreateG3GatewayCommand.class);
            if (name != null) { //Otherwise default name is Used
                command.setGatewayName(name);
            }
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createG3SlaveAS3000(String name) {
        executeTransaction(() -> {
            CreateG3SlaveCommand command = injector.getInstance(CreateG3SlaveCommand.class);
            command.setConfig("AS3000");
            if (name != null) { //Otherwise default name is Used
                command.setName(name);
            }
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createG3SlaveAS220(String name) {
        executeTransaction(() -> {
            CreateG3SlaveCommand command = injector.getInstance(CreateG3SlaveCommand.class);
            command.setConfig("AS220");
            if (name != null) { //Otherwise default name is Used
                command.setName(name);
            }
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void addIntervalChannelReadings(String name, String startDate, String path) {
        executeTransaction(() -> {
            AddIntervalChannelReadingsCommand command = injector.getInstance(AddIntervalChannelReadingsCommand.class);
            command.setMeter(name);
            command.setStartDate(startDate);
            command.setSource(path);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void addNoneIntervalChannelReadings(String name, String startDate, String path) {
        executeTransaction(() -> {
            AddNoneIntervalChannelReadingsCommand command = injector.getInstance(AddNoneIntervalChannelReadingsCommand.class);
            command.setMeter(name);
            command.setStartDate(startDate);
            command.setSource(path);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void addRegisterReadings(String name, String startDate, String path) {
        executeTransaction(() -> {
            AddRegisterReadingsCommand command = injector.getInstance(AddRegisterReadingsCommand.class);
            command.setMeter(name);
            command.setStartDate(startDate);
            command.setSource(path);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createBelgianMarketTimeOfUseData() {
        executeTransaction(() -> {
            CreateBelgianMarketTimeOfUseDataCommand command = injector.getInstance(CreateBelgianMarketTimeOfUseDataCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createApplicationServer(String name) {
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
    public void createPowerUser() {
        CreatePowerUserCommand command = injector.getInstance(CreatePowerUserCommand.class);
        //creates a user with user & pass: root root
        command.run();
    }

    @SuppressWarnings("unused")
    public void createDemoData() {
        System.err.println("Usage: createDemoData <comServerName> <host> [<numberOfDevicesPerType>]");
    }

    @SuppressWarnings("unused")
    public void createDemoData(String comServerName, String host) {
        this.createDemoData(comServerName, host, null);
    }

    @SuppressWarnings("unused")
    public void createDemoData(String comServerName, String host, String numberOfDevicesPerType) {
        this.createDemoData(comServerName, host, numberOfDevicesPerType, false);
    }

    /**
     * @param comServerName
     * @param host
     * @param numberOfDevicesPerType
     * @param skipFirmwareManagementData in case you don't want the firmware management data is created
     */
    @SuppressWarnings("unused")
    public void createDemoData(String comServerName, String host, String numberOfDevicesPerType, boolean skipFirmwareManagementData) {
        CreateDemoDataCommand command = injector.getInstance(CreateDemoDataCommand.class);
        command.setComServerName(comServerName);
        command.setHost(host);
        if (numberOfDevicesPerType != null) {
            command.setDevicesPerType(Integer.valueOf(numberOfDevicesPerType));
        }
        command.setSkipFirmwareManagementData(skipFirmwareManagementData);
        command.run();
    }

    @SuppressWarnings("unused")
    public void createDevices() {
        System.err.println("Usage: createDevices <number> <deviceType> <deviceConfig> [<-authenticationKey=\"\"> <-encryptionKey=\"\"> <-serialNumberPrefix=\"\"> <-activationDate=yyyy-mm-dd>]");
        System.err.println("Timezone for activationDate is fetched from the provided configuration");
    }

    /**
     * @param deviceNumber
     * @param deviceType
     * @param deviceConfig
     */
    @SuppressWarnings("unused")
    public void createDevices(int deviceNumber, String deviceType, String deviceConfig, String... otherOptions) {
        List<String> optionalParameters = Arrays.asList(otherOptions);
        Map<String, String> optionalParametersMap = new HashMap<>();
        optionalParameters.forEach(option -> mapIt.apply(option, optionalParametersMap));
        final String authenticationKey = "-authenticationKey";
        final String encryptionKey = "-encryptionKey";
        final String serialNumberPrefix = "-serialNumberPrefix";
        final String activationDate = "-activationDate";

        for (int i = 0; i < deviceNumber; i++) {
            String serialNumber = optionalParametersMap.getOrDefault(serialNumberPrefix, "ELS301") + String.format("%010d", i);
            CreateDevicesCommand command = injector.getInstance(CreateDevicesCommand.class);

            Optional<DeviceType> deviceTypeOptional = this.deviceConfigurationService.findDeviceTypeByName(deviceType);
            if (!deviceTypeOptional.isPresent()) {
                throw new UnableToCreate("Provided device type " + deviceType + " is not defined");
            }
            command.setDeviceType(deviceTypeOptional.get());
            command.setDeviceConfiguration(deviceTypeOptional.get().getConfigurations().stream().filter(config -> config.getName().equals(deviceConfig)).findFirst()
                    .orElseThrow(() -> new UnableToCreate("Provided device configuration " + deviceConfig + " is not defined on the device type " + deviceType)));
            command.setSerialNumber(serialNumber);
            command.withAuthenticationKey(optionalParametersMap.getOrDefault(authenticationKey, "00000000000000000000000000000001"));
            command.withEncryptionKey(optionalParametersMap.getOrDefault(encryptionKey, "00000000000000000000000000000001"));
            command.withActivationDate(optionalParametersMap.get(activationDate));
            command.runInTransaction();
        }
    }

    private final BiFunction<String, Map<String, String>, String> mapIt = (option, map) -> {
        String[] parameter = option.split("=");
        if (parameter.length == 2) {
            map.put(parameter[0].trim(), parameter[1].trim());
        } else {
            createDevices(); // prints usage
        }
        return option;
    };

    @SuppressWarnings("unused")
    public void createMetrologyConfigurations() {
        executeTransaction(() -> {
            CreateMetrologyConfigurationsCommand command = injector.getInstance(CreateMetrologyConfigurationsCommand.class);
            if (licenseService.getLicenseForApplication("INS").isPresent()) {
                command.createMetrologyConfigurations();
            } else {
                command.createMultisenseMetrologyConfigurations();
            }
        });
    }


    @SuppressWarnings("unused")
    public void createCollectRemoteDataSetup(String comServerName, String host) {
        this.createCollectRemoteDataSetup(comServerName, host, null);
    }

    @SuppressWarnings("unused")
    public void createCollectRemoteDataSetup(String comServerName, String host, String numberOfDevicesPerType) {
        CreateCollectRemoteDataSetupCommand command = injector.getInstance(CreateCollectRemoteDataSetupCommand.class);
        command.setComServerName(comServerName);
        command.setHost(host);
        if (numberOfDevicesPerType != null) {
            command.setDevicesPerType(Integer.valueOf(numberOfDevicesPerType));
        } else {
            command.setDevicesPerType(null);
        }
        command.run();
    }

    @SuppressWarnings("unused")
    public void createValidationSetup() {
        executeTransaction(() -> {
            CreateValidationSetupCommand command = injector.getInstance(CreateValidationSetupCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createEstimationSetup() {
        executeTransaction(() -> {
            CreateEstimationSetupCommand command = injector.getInstance(CreateEstimationSetupCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createNtaConfig() {
        executeTransaction(() -> {
            CreateNtaConfigCommand command = injector.getInstance(CreateNtaConfigCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createValidationDevice(String serialNumber) {
        executeTransaction(() -> {
            CreateValidationDeviceCommand command = injector.getInstance(CreateValidationDeviceCommand.class);
            command.setSerialNumber(serialNumber);
            command.setDeviceNamePrefix(Constants.Device.MOCKED_VALIDATION_DEVICE);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createMockedDataDevice(String serialNumber) {
        executeTransaction(() -> {
            CreateDeviceCommand command = injector.getInstance(CreateDeviceCommand.class);
            command.setSerialNumber(serialNumber);
            command.setDeviceNamePrefix(Constants.Device.MOCKED_REALISTIC_DEVICE);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDeviceType(String deviceTypeName, String host) {
        executeTransaction(() -> {
            CreateDeviceTypeCommand command = injector.getInstance(CreateDeviceTypeCommand.class);
            command.setDeviceTypeName(deviceTypeName);
            command.setHost(host);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDeliverDataSetup() {
        executeTransaction(() -> {
            CreateDeliverDataSetupCommand command = injector.getInstance(CreateDeliverDataSetupCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDefaultDeviceLifeCycle() {
        System.err.println("Usage: createDefaultDeviceLifeCycle <startDate, e.g. 2015-01-01>");
    }

    public void createDefaultDeviceLifeCycle(String lastCheckedDate) {
        executeTransaction(() -> {
            CreateDefaultDeviceLifeCycleCommand command = injector.getInstance(CreateDefaultDeviceLifeCycleCommand.class);
            command.setLastCheckedDate(lastCheckedDate);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void setUpFirmwareManagement() {
        executeTransaction(() -> {
            SetupFirmwareManagementCommand command = injector.getInstance(SetupFirmwareManagementCommand.class);
            command.run();
        });
    }

    public void createImporters() {
        executeTransaction(() -> {
            CreateImportersCommand command = injector.getInstance(CreateImportersCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createImporters(String appServerName) {
        executeTransaction(() -> {
            CreateImportersCommand command = injector.getInstance(CreateImportersCommand.class);
            command.setAppServerName(appServerName);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createImportDirectories() {
        System.err.println("Usage: createImportDirectories [<basePath>, if not specified then the base path from the active appserver will be used]");
        executeTransaction(() -> {
            CreateImporterDirectoriesCommand command = injector.getInstance(CreateImporterDirectoriesCommand.class);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createImportDirectories(String basePath) {
        executeTransaction(() -> {
            CreateImporterDirectoriesCommand command = injector.getInstance(CreateImporterDirectoriesCommand.class);
            command.setBaseImportPath(basePath);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void createDemoUser() {
        System.err.println("Usage: createDemoUser <user name>");
    }

    public void createDemoUser(String name) {
        executeTransaction(() -> {
            CreateDemoUserCommand command = injector.getInstance(CreateDemoUserCommand.class);
            command.setUserName(name);
            command.run();
        });
    }

    @SuppressWarnings("unused")
    public void importCalendar() {
        System.err.println("Usage: importCalendar <calendar xml location>");
    }

    @SuppressWarnings("unused")
    public void importCalendar(String fileName) {
        executeTransaction(() -> {
            try {
                this.injector.getInstance(FileImportCommand.class)
                        .useImporter("CalendarImporterFactory")
                        .content(new FileInputStream(fileName))
                        .onSuccess(msg -> System.out.println("Import result: " + msg))
                        .run();
            } catch (IOException e) {
                throw new UnableToCreate("Import failed with error: " + e.getLocalizedMessage());
            }
        });
    }

    @SuppressWarnings("unused")
    public void createRegisterDevice() {
        System.err.println("Usage: createRegisterDevice <name>");
    }


    @SuppressWarnings("unused")
    public void createRegisterDevice(String name) {
        CreateRegisterDeviceCommand command = injector.getInstance(CreateRegisterDeviceCommand.class);
        command.setDeviceName(name);
        command.run();
    }

    @SuppressWarnings("unused")
    public void setDeviceLocations() {
        executeTransaction(() -> this.injector.getInstance(AddLocationInfoToDevicesCommand.class).run());
    }

    @SuppressWarnings("unused")
    public void createSPEDevice() {
        System.err.println("Usage: createSPEDevice <serialNumber, without SPE prefix> [<deviceTypeName>, <deviceConfigurationName] [<host>]");
    }

    public void createSPEDevice(String serialNumber) {
        createSPEDevice(serialNumber, null, null, null);
    }

    public void createSPEDevice(String serialNumber, String deviceTypeName, String deviceConfigurationName) {
        createSPEDevice(serialNumber, deviceTypeName, deviceConfigurationName, null);
    }

    public void createSPEDevice(String serialNumber, String deviceTypeName, String deviceConfigurationName, String host) {
        executeTransaction(() -> {
            CreateSPEDeviceCommand deviceCommand = this.injector.getInstance(CreateSPEDeviceCommand.class);
            deviceCommand.setSerialNumber(serialNumber);
            deviceCommand.setDeviceTypeTpl(deviceTypeName);
            deviceCommand.setDeviceConfiguration(deviceConfigurationName);
            if (host != null) {
                deviceCommand.setHost(host);
            }
            deviceCommand.withLocation();
            deviceCommand.withUsagePoint();
            deviceCommand.deviceShouldBeActive();
            deviceCommand.run();
        });
    }


    @SuppressWarnings("unused")
    public void createDefaultAlarmRule() {
        executeTransaction(() -> {
            CreateAlarmCreationRuleCommand command = injector.getInstance(CreateAlarmCreationRuleCommand.class);
            command.run();
        });
    }
}
