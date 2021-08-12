/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ConfigPropertiesService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.upgrade.V10_4_21SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_24SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_8_11SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_9_3SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_24SimpleUpgrader;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.StopWatch;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.Constants;
import com.energyict.mdc.common.device.data.KeyAccessorStatus;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.SecurityAccessorDAO;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CustomPropertySetsTranslationKeys;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.audit.AuditTranslationKeys;
import com.energyict.mdc.device.data.impl.cps.CustomPropertyTranslationKeys;
import com.energyict.mdc.device.data.impl.crlrequest.CrlRequestTaskPropertiesServiceImpl;
import com.energyict.mdc.device.data.impl.events.ComTaskExecutionCreatorEventHandler;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiServiceImpl;
import com.energyict.mdc.device.data.impl.pki.SecurityAccessorDAOImpl;
import com.energyict.mdc.device.data.impl.search.DeviceSearchDomain;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.data.impl.tasks.CommunicationTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.PriorityComTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.impl.tasks.report.CommunicationTaskReportServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.report.ConnectionTaskReportServiceImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.PriorityComTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;

/**
 * Provides an implementation for the {@link DeviceDataModelService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-30 (17:33)
 */
@Component(name = "com.energyict.mdc.device.data", service = {DeviceDataModelService.class, TranslationKeyProvider.class, MessageSeedProvider.class}, property = {"name=" + DeviceDataServices.COMPONENT_NAME, "osgi.command.scope=mdc.service.testing", "osgi.command.function=testSearch",}, immediate = true)
@LiteralSql
public class DeviceDataModelServiceImpl implements DeviceDataModelService, TranslationKeyProvider, MessageSeedProvider {
    private static final Logger LOGGER = Logger.getLogger(DeviceDataModelServiceImpl.class.getName());// just for time measurement
    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile MessageService messagingService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile com.energyict.mdc.issues.IssueService mdcIssueService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile com.elster.jupiter.tasks.TaskService jupiterTaskService;
    private volatile Clock clock;
    private volatile KpiService kpiService;
    private volatile com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private volatile PropertySpecService propertySpecService;

    private volatile CustomPropertySetService customPropertySetService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile SchedulingService schedulingService;
    private volatile TaskService mdcTaskService;
    private volatile QueryService queryService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MdcReadingTypeUtilService readingTypeUtilService;
    private volatile MasterDataService masterDataService;
    private volatile TransactionService transactionService;
    private volatile JsonService jsonService;
    private volatile UpgradeService upgradeService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile ServiceCallService serviceCallService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile LockService lockService;
    private volatile DataVaultService dataVaultService;
    private volatile SecurityManagementService securityManagementService;
    private volatile MeteringZoneService meteringZoneService;
    private volatile CalendarService calendarService;
    private volatile FiniteStateMachineService finiteStateMachineService;

    private ServerConnectionTaskService connectionTaskService;
    private ConnectionTaskReportService connectionTaskReportService;
    private ServerCommunicationTaskService communicationTaskService;
    private PriorityComTaskService priorityComTaskService;
    private CommunicationTaskReportService communicationTaskReportService;
    private ServerDeviceService deviceService;
    private RegisterService registerService;
    private ServerLoadProfileService loadProfileService;
    private ServerLogBookService logBookService;
    private DataCollectionKpiService dataCollectionKpiService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private BatchService batchService;
    private DeviceMessageService deviceMessageService;
    private List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<>();
    private CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    private BundleContext bundleContext;
    private ConfigPropertiesService configPropertiesService;
    private ComTaskExecutionCreatorEventHandler comTaskExecutionCreatorEventHandler;
    private DeviceSearchDomain deviceSearchDomain;
    private SecurityAccessorDAO securityAccessorDAO;
    private DeviceProcessAssociationProvider deviceProcessAssociationProvider;

    // For OSGi purposes only
    public DeviceDataModelServiceImpl() {
        super();
    }

    // For unit testing purposes only
    @Inject
    public DeviceDataModelServiceImpl(
            BundleContext bundleContext,
            OrmService ormService, EventService eventService, NlsService nlsService, Clock clock, KpiService kpiService, com.elster.jupiter.tasks.TaskService jupiterTaskService, IssueService issueService,
            PropertySpecService propertySpecService, com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService,
            CustomPropertySetService customPropertySetService, ProtocolPluggableService protocolPluggableService,
            EngineConfigurationService engineConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, DeviceConfigurationService deviceConfigurationService,
            MeteringService meteringService, ValidationService validationService, EstimationService estimationService,
            SchedulingService schedulingService, MessageService messageService,
            UserService userService, DeviceMessageSpecificationService deviceMessageSpecificationService, MeteringGroupsService meteringGroupsService,
            QueryService queryService, TaskService mdcTaskService, MasterDataService masterDataService,
            TransactionService transactionService, JsonService jsonService, com.energyict.mdc.issues.IssueService mdcIssueService, MdcReadingTypeUtilService mdcReadingTypeUtilService,
            UpgradeService upgradeService, MetrologyConfigurationService metrologyConfigurationService, ServiceCallService serviceCallService, ThreadPrincipalService threadPrincipalService,
            LockService lockService, DataVaultService dataVaultService,
            SecurityManagementService securityManagementService, MeteringZoneService meteringZoneService,
            CalendarService calendarService, MeteringTranslationService meteringTranslationService,
            ConfigPropertiesService configPropertiesService, FiniteStateMachineService finiteStateMachineService) {
        this();
        setOrmService(ormService);
        setEventService(eventService);
        setNlsService(nlsService);
        setClock(clock);
        setKpiService(kpiService);
        setJupiterTaskService(jupiterTaskService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setJupiterPropertySpecService(jupiterPropertySpecService);
        setCustomPropertySetService(customPropertySetService);
        setProtocolPluggableService(protocolPluggableService);
        setEngineConfigurationService(engineConfigurationService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setDeviceConfigurationService(deviceConfigurationService);
        setMeteringService(meteringService);
        setValidationService(validationService);
        setEstimationService(estimationService);
        setSchedulingService(schedulingService);
        setMdcTaskService(mdcTaskService);
        setMessagingService(messageService);
        setUserService(userService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setMeteringGroupsService(meteringGroupsService);
        setQueryService(queryService);
        setMasterDataService(masterDataService);
        setTransactionService(transactionService);
        setJsonService(jsonService);
        setMdcIssueService(mdcIssueService);
        setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
        setUpgradeService(upgradeService);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setServiceCallService(serviceCallService);
        setThreadPrincipalService(threadPrincipalService);
        setLockService(lockService);
        setDataVaultService(dataVaultService);
        setSecurityManagementService(securityManagementService);
        setMeteringZoneService(meteringZoneService);
        setCalendarService(calendarService);
        setMeteringTranslationService(meteringTranslationService);
        setConfigPropertiesService(configPropertiesService);
        setFiniteStateMachineService(finiteStateMachineService);
        activate(bundleContext);
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DeviceDataServices.COMPONENT_NAME, "Device data");
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public DataModel dataModel() {
        return dataModel;
    }

    @Override
    public Thesaurus thesaurus() {
        return thesaurus;
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public SchedulingService schedulingService() {
        return this.schedulingService;
    }

    @Override
    public EngineConfigurationService engineConfigurationService() {
        return this.engineConfigurationService;
    }

    @Override
    public KpiService kpiService() {
        return kpiService;
    }

    @Override
    public com.elster.jupiter.tasks.TaskService taskService() {
        return jupiterTaskService;
    }

    @Override
    public ProtocolPluggableService protocolPluggableService() {
        return protocolPluggableService;
    }

    @Override
    public DeviceConfigurationService deviceConfigurationService() {
        return deviceConfigurationService;
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public ServerConnectionTaskService connectionTaskService() {
        return this.connectionTaskService;
    }

    @Override
    public ConnectionTaskReportService connectionTaskReportService() {
        return this.connectionTaskReportService;
    }

    @Override
    public ServerCommunicationTaskService communicationTaskService() {
        return this.communicationTaskService;
    }

    @Override
    public PriorityComTaskService priorityComTaskService() {
        return priorityComTaskService;
    }

    @Override
    public CommunicationTaskReportService communicationTaskReportService() {
        return communicationTaskReportService;
    }

    @Override
    public ServerDeviceService deviceService() {
        return this.deviceService;
    }

    @Override
    public DataCollectionKpiService dataCollectionKpiService() {
        return this.dataCollectionKpiService;
    }

    @Override
    public BatchService batchService() {
        return this.batchService;
    }

    @Override
    public MessageService messageService() {
        return this.messagingService;
    }

    @Override
    public EventService eventService() {
        return this.eventService;
    }

    @Override
    public DeviceMessageSpecificationService deviceMessageSpecificationService() {
        return this.deviceMessageSpecificationService;
    }

    @Override
    public ValidationService validationService() {
        return this.validationService;
    }

    @Override
    public MeteringZoneService meteringZoneService() {
        return this.meteringZoneService;
    }

    @Override
    public JsonService jsonService() {
        return jsonService;
    }

    @Override
    public void executeUpdate(SqlBuilder sqlBuilder) {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                statement.executeUpdate();
                // Don't care about how many rows were updated and if that matches the expected number of updates
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public Map<TaskStatus, Long> fetchTaskStatusCounters(PreparedStatementProvider preparedStatementProvider) {
        Map<TaskStatus, Long> counters = new HashMap<>();
        StopWatch watch = new StopWatch(true);// just for time measurement
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = preparedStatementProvider.prepare(connection)) {
            watch.stop();// just for time measurement
            LOGGER.log(Level.WARNING, "CONM1163: method: getConnection and Prepare statement(fetchTaskStatusCounters); " + watch.toString());// just for time measurement
            watch.start();// just for time measurement
            this.fetchTaskStatusCounters(statement, counters);
            watch.stop();// just for time measurement
            LOGGER.log(Level.WARNING, "CONM1163: method: fetchTaskStatusCounters; " + watch.toString());// just for time measurement
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
    }

    @Override
    public Map<Long, Map<TaskStatus, Long>> fetchTaskStatusBreakdown(PreparedStatementProvider builder) {
        Map<Long, Map<TaskStatus, Long>> counters = new HashMap<>();
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = builder.prepare(connection)) {
            this.fetchTaskStatusBreakdown(statement, counters);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
    }

    @Override
    public Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        for (TaskStatus missing : this.taskStatusComplement(counters.keySet())) {
            counters.put(missing, 0L);
        }
        return counters;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setJupiterPropertySpecService(com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService) {
        this.jupiterPropertySpecService = jupiterPropertySpecService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        Thesaurus meteringThesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN)
                .join(meteringThesaurus)
                .join(nlsService.getThesaurus(Constants.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + DeviceProcessAssociationProvider.APP_KEY + ")")
    public void setLicense(License license) {
        // explicit dependency on license
    }

    @Reference
    public void setMessagingService(MessageService messagingService) {
        this.messagingService = messagingService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setConfigPropertiesService(ConfigPropertiesService configPropertiesService) {
        this.configPropertiesService = configPropertiesService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference(target = "(name=" + CommandCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setCommandCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference(target = "(name=" + CompletionOptionsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setCompletionOptionsCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference(target = "(name=" + OnDemandReadServiceCallCustomPropertySet.CUSTOM_PROPERTY_SET_NAME + ")")
    public void setOnDemandReadServiceCallCustomPropertySet(CustomPropertySet customPropertySet) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setMdcTaskService(TaskService mdcTaskService) {
        this.mdcTaskService = mdcTaskService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setJupiterTaskService(com.elster.jupiter.tasks.TaskService jupiterTaskService) {
        this.jupiterTaskService = jupiterTaskService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setMdcIssueService(com.energyict.mdc.issues.IssueService mdcIssueService) {
        this.mdcIssueService = mdcIssueService;
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setMeteringZoneService(MeteringZoneService meteringZoneService) {
        this.meteringZoneService = meteringZoneService;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceDataModelService.class).toInstance(DeviceDataModelServiceImpl.this);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(IssueService.class).toInstance(issueService);
                bind(com.elster.jupiter.properties.PropertySpecService.class).toInstance(jupiterPropertySpecService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
                bind(MeteringService.class).toInstance(meteringService);
                bind(ValidationService.class).toInstance(validationService);
                bind(EstimationService.class).toInstance(estimationService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(MessageService.class).toInstance(messagingService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(IssueService.class).toInstance(issueService);
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
                bind(KpiService.class).toInstance(kpiService);
                bind(com.elster.jupiter.tasks.TaskService.class).toInstance(jupiterTaskService);
                bind(TaskService.class).toInstance(mdcTaskService);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(ServerConnectionTaskService.class).toInstance(connectionTaskService);
                bind(ConnectionTaskReportService.class).toInstance(connectionTaskReportService);
                bind(PriorityComTaskService.class).toInstance(priorityComTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(ServerCommunicationTaskService.class).toInstance(communicationTaskService);
                bind(CommunicationTaskReportService.class).toInstance(communicationTaskReportService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(ServerDeviceService.class).toInstance(deviceService);
                bind(LoadProfileService.class).toInstance(loadProfileService);
                bind(LogBookService.class).toInstance(logBookService);
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                bind(DataCollectionKpiService.class).toInstance(dataCollectionKpiService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(MdcReadingTypeUtilService.class).toInstance(readingTypeUtilService);
                bind(BatchService.class).toInstance(batchService);
                bind(TaskService.class).toInstance(mdcTaskService);
                bind(MasterDataService.class).toInstance(masterDataService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(JsonService.class).toInstance(jsonService);
                bind(com.energyict.mdc.issues.IssueService.class).toInstance(mdcIssueService);
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(UserPreferencesService.class).toInstance(userService.getUserPreferencesService());
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(DeviceMessageService.class).toInstance(deviceMessageService);
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
                bind(LockService.class).toInstance(lockService);
                bind(SecurityManagementService.class).toInstance(securityManagementService);
                bind(CrlRequestTaskPropertiesService.class).toInstance(crlRequestTaskPropertiesService);
                bind(MeteringZoneService.class).toInstance(meteringZoneService);
                bind(CalendarService.class).toInstance(calendarService);
                bind(MeteringTranslationService.class).toInstance(meteringTranslationService);
                bind(ConfigPropertiesService.class).toInstance(configPropertiesService);
                bind(DeviceSearchDomain.class).toInstance(deviceSearchDomain);
                bind(SecurityAccessorDAO.class).toInstance(securityAccessorDAO);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.createRealServices();
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel, dataVaultService);
        }
        this.dataModel.register(this.getModule());
        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", DeviceDataServices.COMPONENT_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 2), UpgraderV10_2.class)
                        .put(version(10, 2, 1), UpgraderV10_2_1.class)
                        .put(version(10, 3), UpgraderV10_3.class)
                        .put(version(10, 4), UpgraderV10_4.class)
                        .put(version(10, 4, 1), UpgraderV10_4_1.class)
                        .put(version(10, 4, 2), UpgraderV10_4_2.class)
                        .put(version(10, 4, 3), UpgraderV10_4_3.class)
                        .put(version(10, 4, 5), UpgraderV10_4_5.class)
                        .put(version(10, 4, 7), UpgraderV10_4_7.class)
                        .put(version(10, 4, 9), UpgraderV10_4_9.class)
                        .put(version(10, 4, 10), UpgraderV10_4_10.class)
                        .put(version(10, 4, 21), V10_4_21SimpleUpgrader.class)
                        .put(version(10, 4, 24), V10_4_24SimpleUpgrader.class)
                        .put(version(10, 4, 25), UpgraderV10_4_25.class)
                        .put(version(10, 4, 28), UpgraderV10_4_28.class)
                        .put(version(10, 6), UpgraderV10_6.class)
                        .put(version(10, 6, 1), UpgraderV10_6_1.class)
                        .put(version(10, 7), UpgraderV10_7.class)
                        .put(version(10, 7, 1), UpgraderV10_7_1.class)
                        .put(version(10, 7, 2), UpgraderV10_7_2.class)
                        .put(version(10, 8), UpgraderV10_8.class)
                        .put(version(10, 8, 1), UpgraderV10_8_1.class)
                        .put(version(10, 8, 11), V10_8_11SimpleUpgrader.class)
                        .put(version(10, 8, 15), UpgraderV10_8_15.class)
                        .put(version(10, 8, 18), UpgraderV10_8_18.class)
                        .put(version(10, 8, 19), UpgraderV10_8_19.class)
                        .put(version(10, 8, 30), UpgraderV10_8_30.class)
                        .put(version(10, 8, 31), UpgraderV10_8_31.class)
                        .put(version(10, 9), UpgraderV10_9.class)
                        .put(version(10, 9, 1), UpgraderV10_9_1.class)
                        .put(version(10, 9, 3), V10_9_3SimpleUpgrader.class)
                        .put(version(10, 9, 4), UpgraderV10_9_4.class)
                        .put(version(10, 9, 6), UpgraderV10_9_6.class)
                        .put(version(10, 9, 8), UpgraderV10_9_8.class)
                        .put(version(10, 9, 9), UpgraderV10_9_9.class)
                        .put(version(10, 9, 10), UpgraderV10_9_10.class)
                        .put(version(10, 9, 13), UpgraderV10_9_13.class)
                        .put(version(10, 9, 17), UpgraderV10_9_17.class)
                        .build());
        this.registerRealServices(bundleContext);
    }

    private void createRealServices() {
        connectionTaskService = new ConnectionTaskServiceImpl(this, eventService, protocolPluggableService);
        connectionTaskReportService = new ConnectionTaskReportServiceImpl(this, meteringService);
        priorityComTaskService = new PriorityComTaskServiceImpl(this, engineConfigurationService, connectionTaskService, transactionService, threadPrincipalService);
        communicationTaskService = new CommunicationTaskServiceImpl(this, configPropertiesService, bundleContext, priorityComTaskService);
        communicationTaskReportService = new CommunicationTaskReportServiceImpl(this, meteringService);
        deviceService = new DeviceServiceImpl(this, meteringService, queryService, thesaurus, clock);
        registerService = new RegisterServiceImpl(this);
        loadProfileService = new LoadProfileServiceImpl(this);
        logBookService = new LogBookServiceImpl(this);
        dataCollectionKpiService = new DataCollectionKpiServiceImpl(this);
        batchService = new BatchServiceImpl(this);
        deviceMessageService = new DeviceMessageServiceImpl(this, threadPrincipalService, meteringGroupsService, clock);
        crlRequestTaskPropertiesService = new CrlRequestTaskPropertiesServiceImpl(this);
        comTaskExecutionCreatorEventHandler = new ComTaskExecutionCreatorEventHandler(deviceService);
        deviceSearchDomain = new DeviceSearchDomain(this, clock, protocolPluggableService);
        securityAccessorDAO = new SecurityAccessorDAOImpl(dataModel);
        deviceProcessAssociationProvider = new DeviceProcessAssociationProvider(thesaurus, propertySpecService, finiteStateMachineService, deviceLifeCycleConfigurationService, meteringTranslationService);
    }

    private void registerRealServices(BundleContext bundleContext) {
        serviceRegistrations.add(bundleContext.registerService(Subscriber.class, this.comTaskExecutionCreatorEventHandler, null));
        registerConnectionTaskService(bundleContext);
        registerConnectionTaskReportService(bundleContext);
        registerPriorityComTaskService(bundleContext);
        registerCommunicationTaskService(bundleContext);
        registerCommunicationTaskReportService(bundleContext);
        registerDeviceService(bundleContext);
        registerRegisterService(bundleContext);
        registerLoadProfileService(bundleContext);
        registerLogBookService(bundleContext);
        registerDataCollectionKpiService(bundleContext);
        registerBatchService(bundleContext);
        registerDeviceMessageService(bundleContext);
        registerCrlRequestTaskPropertiesService(bundleContext);
        registerDeviceSearchDomainService(bundleContext);
        registerPKIService(bundleContext);
        registerDeviceProcessAssociationProvider(bundleContext);
    }

    private void registerDeviceSearchDomainService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(SearchDomain.class, this.deviceSearchDomain, null));
    }

    private void registerPKIService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(SecurityAccessorDAO.class, this.securityAccessorDAO, null));
    }

    private void registerConnectionTaskService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(ConnectionTaskService.class, this.connectionTaskService, null));
        this.serviceRegistrations.add(bundleContext.registerService(ServerConnectionTaskService.class, this.connectionTaskService, null));
    }

    private void registerConnectionTaskReportService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(ConnectionTaskReportService.class, this.connectionTaskReportService, null));
    }

    private void registerPriorityComTaskService(BundleContext bundleContext) {
        serviceRegistrations.add(bundleContext.registerService(PriorityComTaskService.class, priorityComTaskService, null));
    }

    private void registerCommunicationTaskService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(CommunicationTaskService.class, this.communicationTaskService, null));
        this.serviceRegistrations.add(bundleContext.registerService(ServerCommunicationTaskService.class, this.communicationTaskService, null));
    }

    private void registerCommunicationTaskReportService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(CommunicationTaskReportService.class, this.communicationTaskReportService, null));
    }

    private void registerDeviceService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(DeviceService.class, deviceService, null));
        this.serviceRegistrations.add(bundleContext.registerService(ServerDeviceService.class, deviceService, null));
    }

    private void registerRegisterService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(RegisterService.class, registerService, null));
    }

    private void registerLogBookService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(LogBookService.class, this.logBookService, null));
    }

    private void registerLoadProfileService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(LoadProfileService.class, this.loadProfileService, null));
    }

    private void registerDataCollectionKpiService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(DataCollectionKpiService.class, this.dataCollectionKpiService, null));
    }

    private void registerBatchService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(BatchService.class, this.batchService, null));
    }

    private void registerDeviceMessageService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(DeviceMessageService.class, this.deviceMessageService, null));
    }

    private void registerCrlRequestTaskPropertiesService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(CrlRequestTaskPropertiesService.class, this.crlRequestTaskPropertiesService, null));
    }

    private void registerDeviceProcessAssociationProvider(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(ProcessAssociationProvider.class, deviceProcessAssociationProvider, new Hashtable<>(ImmutableMap.of("name", DeviceProcessAssociationProvider.NAME))));
    }

    @Deactivate
    public void stop() throws Exception {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        serviceRegistrations.clear();
    }

    @Override
    public String getComponentName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(PropertyTranslationKeys.values()));
        keys.addAll(Arrays.asList(DevicePropertyTranslationKeys.values()));
        keys.addAll(Arrays.asList(SubscriberTranslationKeys.values()));
        keys.addAll(Arrays.asList(Privileges.values()));
        keys.addAll(Arrays.asList(ConnectionTaskSuccessIndicatorTranslationKeys.values()));
        keys.addAll(Arrays.asList(ComSessionSuccessIndicatorTranslationKeys.values()));
        keys.addAll(Arrays.asList(TaskStatusTranslationKeys.values()));
        keys.addAll(Arrays.asList(CompletionCodeTranslationKeys.values()));
        keys.addAll(Arrays.asList(CustomPropertySetsTranslationKeys.values()));
        keys.addAll(Arrays.asList(KeyAccessorStatus.values()));
        keys.addAll(Arrays.asList(CustomPropertyTranslationKeys.values()));
        keys.addAll(Arrays.asList(AuditTranslationKeys.values()));
        keys.addAll(Arrays.asList(DeviceProcessAssociationProviderTranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    private void fetchTaskStatusCounters(PreparedStatement statement, Map<TaskStatus, Long> counters) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String taskStatusName = resultSet.getString(1);
                long counter = resultSet.getLong(2);
                counters.put(TaskStatus.valueOf(taskStatusName), counter);
            }
        }
    }

    private void fetchTaskStatusBreakdown(PreparedStatement statement, Map<Long, Map<TaskStatus, Long>> breakdown) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String taskStatusName = resultSet.getString(1);
                long breakdownId = resultSet.getLong(2);
                long counter = resultSet.getLong(3);
                Map<TaskStatus, Long> counters = breakdown.get(breakdownId);
                if (counters == null) {
                    counters = new HashMap<>();
                    this.addMissingTaskStatusCounters(counters);
                    breakdown.put(breakdownId, counters);
                }
                counters.put(TaskStatus.valueOf(taskStatusName), counter);
            }
        }
    }

    private EnumSet<TaskStatus> taskStatusComplement(Set<TaskStatus> taskStatuses) {
        if (taskStatuses.isEmpty()) {
            return EnumSet.allOf(TaskStatus.class);
        } else {
            return EnumSet.complementOf(EnumSet.copyOf(taskStatuses));
        }
    }
}
