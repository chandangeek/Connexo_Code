package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.users.Privilege;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.TranslationKeys;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementConnectionMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementPriorityMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ComTaskEnablementStatusMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiCalculatorHandlerFactory;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiServiceImpl;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.CommunicationTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DeviceDataModelService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-30 (17:33)
 */
@Component(name = "com.energyict.mdc.device.data", service = {DeviceDataModelService.class, ReferencePropertySpecFinderProvider.class,
        InstallService.class, TranslationKeyProvider.class, MessageSeedProvider.class, PrivilegesProvider.class}, property = {"name=" + DeviceDataServices.COMPONENT_NAME, "osgi.command.scope=mdc.service.testing", "osgi.command.function=testSearch",}, immediate = true)
public class DeviceDataModelServiceImpl implements DeviceDataModelService, ReferencePropertySpecFinderProvider, InstallService, TranslationKeyProvider, MessageSeedProvider, PrivilegesProvider {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile MessageService messagingService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile com.elster.jupiter.tasks.TaskService taskService;
    private volatile Clock clock;
    private volatile KpiService kpiService;
    private volatile com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private volatile PropertySpecService propertySpecService;

    private volatile RelationService relationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile SchedulingService schedulingService;
    private volatile SecurityPropertyService securityPropertyService;
    private volatile QueryService queryService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TaskService mdcTaskService;
    private volatile MasterDataService masterDataService;

    private ServerConnectionTaskService connectionTaskService;
    private ServerCommunicationTaskService communicationTaskService;
    private ServerDeviceService deviceService;
    private ServerLoadProfileService loadProfileService;
    private ServerLogBookService logBookService;
    private DataCollectionKpiService dataCollectionKpiService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private BatchService batchService;
    private DeviceMessageService deviceMessageService;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();


    // For OSGi purposes only
    public DeviceDataModelServiceImpl() {
        super();
    }

    // For unit testing purposes only
    @Inject
    public DeviceDataModelServiceImpl(BundleContext bundleContext,
                                      OrmService ormService, EventService eventService, NlsService nlsService, Clock clock, KpiService kpiService, com.elster.jupiter.tasks.TaskService taskService, IssueService issueService,
                                      PropertySpecService propertySpecService, com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService,
                                      RelationService relationService, ProtocolPluggableService protocolPluggableService,
                                      EngineConfigurationService engineConfigurationService, DeviceConfigurationService deviceConfigurationService,
                                      MeteringService meteringService, ValidationService validationService, EstimationService estimationService,
                                      SchedulingService schedulingService, MessageService messageService,
                                      SecurityPropertyService securityPropertyService, UserService userService, DeviceMessageSpecificationService deviceMessageSpecificationService, MeteringGroupsService meteringGroupsService,
                                      QueryService queryService, TaskService mdcTaskService, MasterDataService masterDataService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setRelationService(relationService);
        this.setClock(clock);
        this.setKpiService(kpiService);
        this.setTaskService(taskService);
        this.setIssueService(issueService);
        this.setPropertySpecService(propertySpecService);
        this.setJupiterPropertySpecService(jupiterPropertySpecService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setEngineConfigurationService(engineConfigurationService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setMeteringService(meteringService);
        this.setValidationService(validationService);
        this.setEstimationService(estimationService);
        this.setSchedulingService(schedulingService);
        this.setMessagingService(messageService);
        this.setSecurityPropertyService(securityPropertyService);
        this.setUserService(userService);
        this.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        this.setMeteringGroupsService(meteringGroupsService);
        this.setQueryService(queryService);
        this.setMdcTaskService(mdcTaskService);
        this.setMasterDataService(masterDataService);
        this.activate(bundleContext);
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(
                DeviceConfigurationService.COMPONENTNAME,
                PluggableService.COMPONENTNAME,
                EngineConfigurationService.COMPONENT_NAME,
                SchedulingService.COMPONENT_NAME,
                TaskService.COMPONENT_NAME,
                MeteringGroupsService.COMPONENTNAME,
                KpiService.COMPONENT_NAME,
                com.elster.jupiter.tasks.TaskService.COMPONENTNAME,
                UserService.COMPONENTNAME,
                OrmService.COMPONENTNAME,
                EventService.COMPONENTNAME,
                NlsService.COMPONENTNAME,
                MessageService.COMPONENTNAME,
                EstimationService.COMPONENTNAME
        );
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        return Stream.of(this.connectionTaskService, this.deviceService, this.logBookService, this.loadProfileService).
                flatMap(p -> p.finders().stream()).
                collect(Collectors.toList());
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(DeviceDataServices.COMPONENT_NAME, "Device data");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public DataModel dataModel() {
        return dataModel;
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
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setJupiterPropertySpecService(com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService) {
        this.jupiterPropertySpecService = jupiterPropertySpecService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public Thesaurus thesaurus() {
        return thesaurus;
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

    @Override
    public Clock clock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }

    public ProtocolPluggableService protocolPluggableService() {
        return protocolPluggableService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public DeviceConfigurationService deviceConfigurationService() {
        return deviceConfigurationService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public SchedulingService schedulingService() {
        return this.schedulingService;
    }

    @Override
    public EngineConfigurationService engineConfigurationService() {
        return this.engineConfigurationService;
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
    public void setSecurityPropertyService(SecurityPropertyService securityPropertyService) {
        this.securityPropertyService = securityPropertyService;
    }

    @Override
    public ServerConnectionTaskService connectionTaskService() {
        return this.connectionTaskService;
    }

    @Override
    public ServerCommunicationTaskService communicationTaskService() {
        return this.communicationTaskService;
    }

    @Override
    public DataCollectionKpiService dataCollectionKpiService() {
        return this.dataCollectionKpiService;
    }

    @Override
    public ServerDeviceService deviceService() {
        return this.deviceService;
    }

    @Override
    public BatchService batchService() {
        return this.batchService;
    }

    @Override
    public KpiService kpiService() {
        return kpiService;
    }

    @Reference
    public void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public com.elster.jupiter.tasks.TaskService taskService() {
        return taskService;
    }

    @Reference
    public void setTaskService(com.elster.jupiter.tasks.TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMdcTaskService(TaskService taskService) {
        this.mdcTaskService = taskService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceDataModelService.class).toInstance(DeviceDataModelServiceImpl.this);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(SecurityPropertyService.class).toInstance(securityPropertyService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(RelationService.class).toInstance(relationService);
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
                bind(com.elster.jupiter.tasks.TaskService.class).toInstance(taskService);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(ServerConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(ServerCommunicationTaskService.class).toInstance(communicationTaskService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(ServerDeviceService.class).toInstance(deviceService);
                bind(LoadProfileService.class).toInstance(loadProfileService);
                bind(LogBookService.class).toInstance(logBookService);
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                bind(DataCollectionKpiService.class).toInstance(dataCollectionKpiService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(BatchService.class).toInstance(batchService);
                bind(TaskService.class).toInstance(mdcTaskService);
                bind(MasterDataService.class).toInstance(masterDataService);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.createRealServices();
        this.dataModel.register(this.getModule());
        this.registerRealServices(bundleContext);
    }

    private void createRealServices() {
        this.connectionTaskService = new ConnectionTaskServiceImpl(this, eventService, meteringService, protocolPluggableService, clock);
        this.communicationTaskService = new CommunicationTaskServiceImpl(this, meteringService, clock);
        this.deviceService = new DeviceServiceImpl(this, protocolPluggableService, queryService, thesaurus, meteringGroupsService, meteringService);
        this.loadProfileService = new LoadProfileServiceImpl(this);
        this.logBookService = new LogBookServiceImpl(this);
        this.dataCollectionKpiService = new DataCollectionKpiServiceImpl(this);
        this.batchService = new BatchServiceImpl(this);
        this.deviceMessageService = new DeviceMessageServiceImpl(this);
    }

    private void registerRealServices(BundleContext bundleContext) {
        this.registerConnectionTaskService(bundleContext);
        this.registerCommunicationTaskService(bundleContext);
        this.registerDeviceService(bundleContext);
        this.registerLoadProfileService(bundleContext);
        this.registerLogBookService(bundleContext);
        this.registerDataCollectionKpiService(bundleContext);
        this.registerBatchService(bundleContext);
        this.registerDeviceMessageService(bundleContext);
    }

    private void registerConnectionTaskService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(ConnectionTaskService.class, this.connectionTaskService, null));
        this.serviceRegistrations.add(bundleContext.registerService(ServerConnectionTaskService.class, this.connectionTaskService, null));
    }

    private void registerCommunicationTaskService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(CommunicationTaskService.class, this.communicationTaskService, null));
        this.serviceRegistrations.add(bundleContext.registerService(ServerCommunicationTaskService.class, this.communicationTaskService, null));
    }

    private void registerDeviceService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(DeviceService.class, deviceService, null));
        this.serviceRegistrations.add(bundleContext.registerService(ServerDeviceService.class, deviceService, null));
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

    @Deactivate
    public void stop() throws Exception {
        this.serviceRegistrations.forEach(ServiceRegistration::unregister);
    }

    @Override
    public void install() {
        this.install(true);
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
        keys.addAll(Arrays.asList((DevicePropertyTranslationKeys.values())));
        keys.addAll(Arrays.asList(
                new SimpleTranslationKey(DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER, DataCollectionKpiCalculatorHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME),
                new SimpleTranslationKey(ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_SUBSCRIBER, ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_SUBSCRIBER_DISPLAY_NAME),
                new SimpleTranslationKey(Installer.COMSCHEDULE_RECALCULATOR_MESSAGING_NAME, Installer.COMSCHEDULE_RECALCULATOR_MESSAGING_DISPLAYNAME),
                new SimpleTranslationKey(Installer.COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME, Installer.COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_DISPLAYNAME),
                new SimpleTranslationKey(ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementConnectionMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME),
                new SimpleTranslationKey(ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementPriorityMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME),
                new SimpleTranslationKey(ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_NAME, ComTaskEnablementStatusMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME),
                new SimpleTranslationKey(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_SUBSCRIBER, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DISPLAYNAME),
                new SimpleTranslationKey(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME),
                new SimpleTranslationKey(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_SUBSCRIBER, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DISPLAY_NAME),
                new SimpleTranslationKey(ConnectionTaskService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME),
                new SimpleTranslationKey(ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_SUBSCRIBER, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DISPLAY_NAME),
                new SimpleTranslationKey(ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_SUBSCRIBER, ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DISPLAY_NAME),
                new SimpleTranslationKey(SchedulingService.FILTER_ITEMIZER_QUEUE_SUBSCRIBER, SchedulingService.FILTER_ITEMIZER_QUEUE_DISPLAYNAME),
                new SimpleTranslationKey(SchedulingService.COM_SCHEDULER_QUEUE_SUBSCRIBER, SchedulingService.COM_SCHEDULER_QUEUE_DISPLAYNAME)));
        keys.addAll(Arrays.asList(Privileges.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    private void install(boolean executeDdl) {
        new Installer(this.dataModel, this.eventService, messagingService).install(executeDdl);
    }

    @Override
    public void executeUpdate(SqlBuilder sqlBuilder) {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                statement.executeUpdate();
                // Don't care about how many rows were updated and if that matches the expected number of updates
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public Map<TaskStatus, Long> fetchTaskStatusCounters(PreparedStatementProvider preparedStatementProvider) {
        Map<TaskStatus, Long> counters = new HashMap<>();
        try (PreparedStatement statement = preparedStatementProvider.prepare(this.dataModel.getConnection(true))) {
            this.fetchTaskStatusCounters(statement, counters);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
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

    @Override
    public Map<Long, Map<TaskStatus, Long>> fetchTaskStatusBreakdown(PreparedStatementProvider builder) {
        Map<Long, Map<TaskStatus, Long>> counters = new HashMap<>();
        try (PreparedStatement statement = builder.prepare(this.dataModel.getConnection(true))) {
            this.fetchTaskStatusBreakdown(statement, counters);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
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

    @Override
    public Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        for (TaskStatus missing : this.taskStatusComplement(counters.keySet())) {
            counters.put(missing, 0L);
        }
        return counters;
    }

    private EnumSet<TaskStatus> taskStatusComplement(Set<TaskStatus> taskStatuses) {
        if (taskStatuses.isEmpty()) {
            return EnumSet.allOf(TaskStatus.class);
        }
        else {
            return EnumSet.complementOf(EnumSet.copyOf(taskStatuses));
        }
    }

    @Override
    public String getModuleName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {

        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICES.getKey(), Privileges.RESOURCE_DEVICES_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADD_DEVICE, Privileges.Constants.VIEW_DEVICE, Privileges.Constants.REMOVE_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_ATTRIBUTE)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_DATA.getKey(), Privileges.RESOURCE_DEVICE_DATA_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_COMMUNICATIONS.getKey(), Privileges.RESOURCE_DEVICE_COMMUNICATIONS_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_GROUPS.getKey(), Privileges.RESOURCE_DEVICE_GROUPS_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL)),
                this.userService.createModuleResourceWithPrivileges(DeviceDataServices.COMPONENT_NAME, Privileges.RESOURCE_INVENTORY_MANAGEMENT.getKey(), Privileges.RESOURCE_INVENTORY_MANAGEMENT_DESCRIPTION.getKey(), Arrays.asList(Privileges.Constants.IMPORT_INVENTORY_MANAGEMENT, Privileges.Constants.REVOKE_INVENTORY_MANAGEMENT))
        );

    }

}