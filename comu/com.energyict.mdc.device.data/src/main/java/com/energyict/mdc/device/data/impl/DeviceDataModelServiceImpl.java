package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiServiceImpl;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.CommunicationTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskServiceImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;
import java.time.Clock;import com.elster.jupiter.validation.ValidationService;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@Component(name="com.energyict.mdc.device.data", service = {DeviceDataModelService.class, ReferencePropertySpecFinderProvider.class, InstallService.class}, property = "name=" + DeviceDataServices.COMPONENT_NAME, immediate = true)
public class DeviceDataModelServiceImpl implements DeviceDataModelService, ReferencePropertySpecFinderProvider, InstallService {

    private volatile BundleContext bundleContext;
    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile MessageService messagingService;
    private volatile UserService userService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;
    private volatile TaskService taskService;
    private volatile Clock clock;
    private volatile KpiService kpiService;

    private volatile RelationService relationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile EngineModelService engineModelService;
    private volatile SchedulingService schedulingService;
    private volatile SecurityPropertyService securityPropertyService;

    private ServerConnectionTaskService connectionTaskService;
    private ServerCommunicationTaskService communicationTaskService;
    private ServerDeviceService deviceService;
    private ServerLoadProfileService loadProfileService;
    private ServerLogBookService logBookService;
    private DataCollectionKpiService dataCollectionKpiService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public DeviceDataModelServiceImpl() {}

    @Inject
    public DeviceDataModelServiceImpl(BundleContext bundleContext,
                                      OrmService ormService, EventService eventService, NlsService nlsService, Clock clock, KpiService kpiService, TaskService taskService,
                                      RelationService relationService, ProtocolPluggableService protocolPluggableService,
                                      EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService,
                                      MeteringService meteringService, ValidationService validationService,
                                      SchedulingService schedulingService, MessageService messageService,
                                      SecurityPropertyService securityPropertyService, UserService userService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setRelationService(relationService);
        this.setClock(clock);
        this.setKpiService(kpiService);
        this.setTaskService(taskService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setEngineModelService(engineModelService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setMeteringService(meteringService);
        this.setValidationService(validationService);
        this.setSchedulingService(schedulingService);
        this.setMessagingService(messageService);
        this.setSecurityPropertyService(securityPropertyService);
        this.setUserService(userService);
        this.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        this.activate(bundleContext);
        this.install(true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(
                OrmService.COMPONENTNAME,
                EventService.COMPONENTNAME,
                NlsService.COMPONENTNAME,
                MessageService.COMPONENTNAME,
                UserService.COMPONENTNAME,
                DeviceMessageSpecificationService.COMPONENT_NAME,
                DeviceConfigurationService.COMPONENTNAME,
                PluggableService.COMPONENTNAME,
                EngineModelService.COMPONENT_NAME,
                SchedulingService.COMPONENT_NAME,
                com.energyict.mdc.tasks.TaskService.COMPONENT_NAME,
                KpiService.COMPONENT_NAME,
                TaskService.COMPONENTNAME,
                DeviceMessageSpecificationService.COMPONENT_NAME,
                MeteringGroupsService.COMPONENTNAME);
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

    @Override
    public DataModel dataModel() {
        return dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
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
    public EngineModelService engineModelService() {
        return this.engineModelService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
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
    public KpiService kpiService() {
        return kpiService;
    }

    @Reference
    public void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Override
    public TaskService taskService() {
        return taskService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
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
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
                bind(MeteringService.class).toInstance(meteringService);
                bind(ValidationService.class).toInstance(validationService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(MessageService.class).toInstance(messagingService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(EngineModelService.class).toInstance(engineModelService);
                bind(KpiService.class).toInstance(kpiService);
                bind(TaskService.class).toInstance(taskService);
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(ServerConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                bind(ServerCommunicationTaskService.class).toInstance(communicationTaskService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(ServerDeviceService.class).toInstance(deviceService);
                bind(LoadProfileService.class).toInstance(loadProfileService);
                bind(LogBookService.class).toInstance(logBookService);
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.createRealServices();
        this.dataModel.register(this.getModule());
        this.registerRealServices(bundleContext);
    }

    private void createRealServices() {
        this.connectionTaskService = new ConnectionTaskServiceImpl(this);
        this.communicationTaskService = new CommunicationTaskServiceImpl(this);
        this.deviceService = new DeviceServiceImpl(this);
        this.loadProfileService = new LoadProfileServiceImpl(this);
        this.logBookService = new LogBookServiceImpl(this);
        this.dataCollectionKpiService = new DataCollectionKpiServiceImpl(this);
    }

    private void registerRealServices(BundleContext bundleContext) {
        this.registerConnectionTaskService(bundleContext);
        this.registerCommunicationTaskService(bundleContext);
        this.registerDeviceService(bundleContext);
        this.registerLoadProfileService(bundleContext);
        this.registerLogBookService(bundleContext);
        this.registerDataCollectionKpiService(bundleContext);
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

    @Deactivate
    public void stop(BundleContext bundleContext) throws Exception {
        for (ServiceRegistration serviceRegistration : this.serviceRegistrations) {
            serviceRegistration.unregister();
        }
    }

    @Override
    public void install() {
        this.install(true);
    }

    private void install(boolean exeuteDdl) {
        new Installer(this.dataModel, this.eventService, this.thesaurus, messagingService, this.userService).install(exeuteDdl);
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
    public void setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(Device device, ConnectionTask connectionTask) {
        this.communicationTaskService.setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(device, connectionTask);
    }

    @Override
    public Map<TaskStatus, Long> fetchTaskStatusCounters(ClauseAwareSqlBuilder builder) {
        Map<TaskStatus, Long> counters = new HashMap<>();
        try (PreparedStatement stmnt = builder.prepare(this.dataModel.getConnection(true))) {
            this.fetchTaskStatusCounters(stmnt, counters);
        }
        catch (SQLException ex) {
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
    public Map<Long, Map<TaskStatus, Long>> fetchTaskStatusBreakdown(ClauseAwareSqlBuilder builder) {
        Map<Long, Map<TaskStatus, Long>> counters = new HashMap<>();
        try (PreparedStatement stmnt = builder.prepare(this.dataModel.getConnection(true))) {
            this.fetchTaskStatusBreakdown(stmnt, counters);
        }
        catch (SQLException ex) {
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

}