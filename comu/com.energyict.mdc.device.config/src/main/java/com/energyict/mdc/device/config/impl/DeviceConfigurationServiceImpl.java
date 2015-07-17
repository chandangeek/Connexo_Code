package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.DeviceConfigurationEstimationRuleSetUsage;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:38)
 */
@Component(name = "com.energyict.mdc.device.config", service = {DeviceConfigurationService.class, ServerDeviceConfigurationService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + DeviceConfigurationService.COMPONENTNAME, immediate = true)
public class DeviceConfigurationServiceImpl implements ServerDeviceConfigurationService, InstallService, TranslationKeyProvider {

    private volatile ProtocolPluggableService protocolPluggableService;

    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile MdcReadingTypeUtilService readingTypeUtilService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile MasterDataService masterDataService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile SchedulingService schedulingService;
    private volatile UserService userService;
    private volatile TaskService taskService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile QueryService queryService;

    private final Set<Privilege> privileges = new HashSet<>();

    public DeviceConfigurationServiceImpl() {
        super();
    }

    @Inject
    public DeviceConfigurationServiceImpl(OrmService ormService, Clock clock, ThreadPrincipalService threadPrincipalService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, UserService userService, ProtocolPluggableService protocolPluggableService, EngineConfigurationService engineConfigurationService, SchedulingService schedulingService, ValidationService validationService, EstimationService estimationService, MasterDataService masterDataService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        this.setOrmService(ormService);
        this.setClock(clock);
        this.setThreadPrincipalService(threadPrincipalService);
        this.setUserService(userService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setReadingTypeUtilService(mdcReadingTypeUtilService);
        this.setEngineConfigurationService(engineConfigurationService);
        this.setMasterDataService(masterDataService);
        this.setSchedulingService(schedulingService);
        this.setValidationService(validationService);
        this.setEstimationService(estimationService);
        this.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        this.activate();
        this.install();
    }

    @Override
    public Finder<DeviceType> findAllDeviceTypes() {
        return DefaultFinder.of(DeviceType.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public DeviceType newDeviceType(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return this.newDeviceType(name, deviceProtocolPluggableClass, this.deviceLifeCycleConfigurationService.findDefaultDeviceLifeCycle().get());
    }

    @Override
    public DeviceType newDeviceType(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceLifeCycle deviceLifeCycle) {
        return DeviceTypeImpl.from(this.getDataModel(), name, deviceProtocolPluggableClass, deviceLifeCycle);
    }

    @Override
    public Optional<DeviceType> findDeviceType(long deviceTypeId) {
        return this.getDataModel().mapper((DeviceType.class)).getUnique("id", deviceTypeId);
    }

    @Override
    public Optional<DeviceType> findAndLockDeviceType(long id, long version) {
        return this.getDataModel().mapper(DeviceType.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<DeviceType> findDeviceTypeByName(String name) {
        return this.getDataModel().mapper((DeviceType.class)).getUnique("name", name);
    }

    @Override
    public String changeDeviceLifeCycleTopicName() {
        return EventType.DEVICELIFECYCLE_UPDATED.topic();
    }

    @Override
    public void changeDeviceLifeCycle(DeviceType deviceType, DeviceLifeCycle deviceLifeCycle) throws IncompatibleDeviceLifeCycleChangeException {
        if (deviceType.getDeviceLifeCycle().getId() != deviceLifeCycle.getId()) {
            this.changeDeviceLifeCycle((ServerDeviceType) deviceType, deviceLifeCycle);
        }
    }

    private void changeDeviceLifeCycle(ServerDeviceType deviceType, DeviceLifeCycle deviceLifeCycle) throws IncompatibleDeviceLifeCycleChangeException {
        DeviceLifeCycleChangeEventSimpleImpl event = new DeviceLifeCycleChangeEventSimpleImpl(this.clock.instant(), deviceType, deviceLifeCycle, this.getCurrentUser());
        this.eventService.postEvent(this.changeDeviceLifeCycleTopicName(), event);
        deviceType.updateDeviceLifeCycle(deviceLifeCycle);
    }

    private Optional<User> getCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return Optional.empty();
        }
        return Optional.of((User) principal);
    }

    @Override
    public Optional<DeviceConfiguration> findDeviceConfiguration(long id) {
        return this.getDataModel().mapper((DeviceConfiguration.class)).getUnique("id", id);
    }

    @Override
    public Optional<DeviceConfiguration> findAndLockDeviceConfigurationByIdAndVersion(long id, long version) {
        return this.getDataModel().mapper(DeviceConfiguration.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<ChannelSpec> findChannelSpec(long channelSpecId) {
        return this.getDataModel().mapper((ChannelSpec.class)).getUnique("id", channelSpecId);
    }

    @Override
    public Optional<RegisterSpec> findRegisterSpec(long id) {
        return this.getDataModel().mapper((RegisterSpec.class)).getUnique("id", id);
    }

    @Override
    public List<RegisterSpec> findActiveRegisterSpecsByDeviceTypeAndRegisterType(DeviceType deviceType, RegisterType registerType) {
        Condition condition = where("deviceConfig.deviceType").isEqualTo(deviceType).
                and(where("registerType").isEqualTo(registerType)).
                and(where("deviceConfig.active").isEqualTo(Boolean.TRUE));
        return this.getDataModel().query(RegisterSpec.class, DeviceConfiguration.class).select(condition);
    }

    @Override
    public List<RegisterSpec> findInactiveRegisterSpecsByDeviceTypeAndRegisterType(DeviceType deviceType, RegisterType registerType) {
        Condition condition = where("deviceConfig.deviceType").isEqualTo(deviceType).
                and(where("registerType").isEqualTo(registerType)).
                and(where("deviceConfig.active").isEqualTo(Boolean.FALSE));
        return this.getDataModel().query(RegisterSpec.class, DeviceConfiguration.class).select(condition);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByMeasurementType(MeasurementType measurementType) {
        return this.getDataModel().mapper(RegisterSpec.class).find("registerType", measurementType);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByChannelSpecAndLinkType(ChannelSpec channelSpec, ChannelSpecLinkType linkType) {
        return this.getDataModel().mapper(RegisterSpec.class).find("linkedChannelSpec", channelSpec, "channelSpecLinkType", linkType);
    }

    @Override
    public List<ChannelSpec> findChannelSpecsForLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        return this.getDataModel().mapper(ChannelSpec.class).find("loadProfileSpec", loadProfileSpec);
    }

    @Override
    public Optional<LoadProfileSpec> findLoadProfileSpec(long loadProfileSpecId) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("id", loadProfileSpecId);
    }

    @Override
    public Optional<LoadProfileSpec> findLoadProfileSpecByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("deviceConfiguration", deviceConfig, "loadProfileType", loadProfileType);
    }

    @Override
    public List<LoadProfileSpec> findLoadProfileSpecsByLoadProfileType(LoadProfileType loadProfileType) {
        return this.getDataModel().mapper(LoadProfileSpec.class).find("loadProfileType", loadProfileType);
    }

    @Override
    public Optional<LogBookSpec> findLogBookSpec(long logBookSpecId) {
        return this.getDataModel().mapper(LogBookSpec.class).getUnique("id", logBookSpecId);
    }

    @Override
    public Optional<ChannelSpec> findChannelSpecForLoadProfileSpecAndChannelType(LoadProfileSpec loadProfileSpec, ChannelType channelType) {
        return this.getDataModel().mapper(ChannelSpec.class).getUnique("loadProfileSpec", loadProfileSpec, "channelType", channelType);
    }

    @Override
    public ChannelSpec findChannelSpecByDeviceConfigurationAndName(DeviceConfiguration deviceConfiguration, String name) {
        return this.getDataModel().mapper(ChannelSpec.class).getUnique("deviceConfiguration", deviceConfiguration, "name", name).orElse(null);
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsByDeviceType(DeviceType deviceType) {
        return this.getDataModel().mapper(DeviceConfiguration.class).find("deviceType", deviceType);
    }

    @Override
    public List<DeviceType> findDeviceTypesWithDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return this.getDataModel().mapper(DeviceType.class).find(DeviceTypeFields.DEVICE_PROTOCOL_PLUGGABLE_CLASS.fieldName(), deviceProtocolPluggableClass.getId());
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsUsingLoadProfileType(LoadProfileType loadProfileType) {
        return this.getDataModel().
                query(DeviceConfiguration.class, LoadProfileSpec.class).
                select(where("loadProfileSpecs.loadProfileType").isEqualTo(loadProfileType));
    }

    @Override
    public List<ChannelSpec> findChannelSpecsForMeasurementType(MeasurementType measurementType) {
        return this.getDataModel().mapper(ChannelSpec.class).find("channelType", measurementType);
    }

    @Override
    public List<ChannelSpec> findChannelSpecsForChannelTypeInLoadProfileType(ChannelType channelType, LoadProfileType loadProfileType) {
        return this.getDataModel().
                query(ChannelSpec.class, LoadProfileSpec.class).
                select(where("channelType").isEqualTo(channelType).
                                and(where("loadProfileSpec.loadProfileType").isEqualTo(loadProfileType))
                );
    }

    @Override
    public List<DeviceType> findDeviceTypesUsingLogBookType(LogBookType logBookType) {
        return this.getDataModel().
                query(DeviceType.class, DeviceTypeLogBookTypeUsage.class).
                select(where("logBookTypeUsages.logBookType").isEqualTo(logBookType));
    }

    @Override
    public List<DeviceType> findDeviceTypesUsingRegisterType(MeasurementType measurementType) {
        return this.getDataModel().
                query(DeviceType.class, DeviceTypeRegisterTypeUsage.class).
                select(where("registerTypeUsages.registerType").isEqualTo(measurementType));
    }

    @Override
    public List<DeviceType> findDeviceTypesUsingLoadProfileType(LoadProfileType loadProfileType) {
        return this.getDataModel().
                query(DeviceType.class, DeviceTypeLoadProfileTypeUsage.class).
                select(where("loadProfileTypeUsages.loadProfileType").isEqualTo(loadProfileType));
    }

    @Override
    public List<DeviceType> findDeviceTypesUsingDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle) {
        return this.getDataModel().
                query(DeviceType.class, DeviceLifeCycleInDeviceType.class, DeviceLifeCycle.class).
                select(where("deviceLifeCycle.deviceLifeCycle").isEqualTo(deviceLifeCycle)
                        .and(where("deviceLifeCycle.interval").isEffective()));
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsUsingLogBookType(LogBookType logBookType) {
        return this.getDataModel().
                query(DeviceConfiguration.class, LogBookSpec.class).
                select(where("logBookSpecs.logBookType").isEqualTo(logBookType));
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsUsingMeasurementType(MeasurementType measurementType) {
        return this.getDataModel().
                query(DeviceConfiguration.class, ChannelSpec.class, RegisterSpec.class).
                select(where("channelSpecs.channelType").isEqualTo(measurementType).
                        or(where("registerSpecs.registerType").isEqualTo(measurementType)));
    }

    @Override
    public boolean isRegisterTypeUsedByDeviceType(RegisterType registerType) {
        return !this.getDataModel().
                query(DeviceTypeRegisterTypeUsage.class).select(where("registerType").isEqualTo(registerType)).isEmpty();
    }

    @Override
    public Finder<DeviceConfiguration> findDeviceConfigurationsUsingDeviceType(DeviceType deviceType) {
        return DefaultFinder.of(DeviceConfiguration.class, where("deviceType").isEqualTo(deviceType), this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public Optional<PartialConnectionTask> findPartialConnectionTask(long id) {
        return dataModel.mapper(PartialConnectionTask.class).getOptional(id);
    }

    @Override
    public List<PartialConnectionTask> findByConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass) {
        return dataModel.query(PartialConnectionTask.class).select(where("pluggableClass").isEqualTo(connectionTypePluggableClass));
    }

    @Override
    public List<PartialConnectionTask> findByComPortPool(ComPortPool comPortPool) {
        return dataModel.query(PartialConnectionTask.class).select(where("comPortPool").isEqualTo(comPortPool));
    }

    @Override
    public Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(long id) {
        return dataModel.mapper(ProtocolDialectConfigurationProperties.class).getOptional(id);
    }

    @Override
    public Optional<ComTaskEnablement> findComTaskEnablement(long id) {
        return dataModel.mapper(ComTaskEnablement.class).getUnique("id", id);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "DeviceType and configurations");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setReadingTypeUtilService(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
        initPrivileges();
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    Optional<Privilege> findPrivilege(String userActionPrivilege) {
        return this.privileges
                .stream()
                .filter(privilege -> privilege.getName().equals(userActionPrivilege))
                .findAny();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceConfigurationService.class).toInstance(DeviceConfigurationServiceImpl.this);
                bind(ServerDeviceConfigurationService.class).toInstance(DeviceConfigurationServiceImpl.this);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MdcReadingTypeUtilService.class).toInstance(readingTypeUtilService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(EngineConfigurationService.class).toInstance(engineConfigurationService);
                bind(UserService.class).toInstance(userService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(ValidationService.class).toInstance(validationService);
                bind(EstimationService.class).toInstance(estimationService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public String getComponentName() {
        return DeviceConfigurationService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService, userService).install(true);
        initPrivileges();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "NLS", "USR", "MDS", "CPC", "MDC", "SCH", "CTS", "VAL", "EST", "DLD", "DLC");
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.taskService = taskService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.estimationService = estimationService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setPluggableService(PluggableService pluggableService) {
        // Not actively used but required for foreign keys in TableSpecs
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public Optional<SecurityPropertySet> findSecurityPropertySet(long id) {
        return dataModel.mapper(SecurityPropertySet.class).getOptional(id);
    }

    @Override
    public List<ComTask> findAvailableComTasks(ComSchedule comSchedule) {
        try (Connection connection = dataModel.getConnection(false)) {
            List<DeviceConfiguration> deviceConfigurations = getDeviceConfigurationsFromComSchedule(comSchedule, connection);
            Collection<ComTask> comTasks;
            if (deviceConfigurations.isEmpty()) {
                comTasks = taskService.findAllComTasks();
            } else {
                comTasks = getComTasksEnabledOnAllDeviceConfigurations(deviceConfigurations);
            }
            return new ArrayList<>(comTasks);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Collection<ComTask> getComTasksEnabledOnAllDeviceConfigurations(List<DeviceConfiguration> deviceConfigurations) {
        Multiset<Long> comTasks = HashMultiset.create();
        Map<Long, ComTask> comTaskMap = new HashMap<>();
        for (DeviceConfiguration deviceConfiguration : deviceConfigurations) {
            for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
                ComTask comTask = comTaskEnablement.getComTask();
                comTasks.add(comTask.getId());
                comTaskMap.put(comTask.getId(), comTask);
            }
        }
        for (Long comTask : comTasks.elementSet()) { // filter comTasks not present in all device configs
            if (comTasks.count(comTask) != deviceConfigurations.size()) {
                comTaskMap.remove(comTask);
            }
        }
        return comTaskMap.values();
    }

    private List<DeviceConfiguration> getDeviceConfigurationsFromComSchedule(ComSchedule comSchedule, Connection connection) throws SQLException {
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
/* Todo: Check with Karel to figure out how to get select distinct activated in code below
        DataMapper<DeviceConfiguration> mapper = this.dataModel.mapper(DeviceConfiguration.class);
        SqlBuilder sqlBuilder = mapper.builder("DC");
        sqlBuilder.append(", DDC_DEVICE DEV, DDC_COMTASKEXEC CTE WHERE DEV.deviceConfigId = DC.ID AND CTE.DEVICEID = DEV.ID AND CTE.COMSCHEDULE = ?");
        try (Fetcher<DeviceConfiguration> fetcher = mapper.fetcher(sqlBuilder)) {
            Iterator<DeviceConfiguration> deviceConfigurationIterator = fetcher.iterator();
            while (deviceConfigurationIterator.hasNext()) {
                deviceConfigurations.add(deviceConfigurationIterator.next());
            }
        }
*/
        try (PreparedStatement preparedStatement = connection.prepareStatement("select distinct deviceConfigId from DDC_DEVICE inner join DDC_COMTASKEXEC on DDC_COMTASKEXEC.DEVICE = DDC_DEVICE.ID where DDC_COMTASKEXEC.COMSCHEDULE = ?")) {
            preparedStatement.setLong(1, comSchedule.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    deviceConfigurations.add(this.findDeviceConfiguration(resultSet.getLong(1)).get());
                }
            }
        }
        return deviceConfigurations;
    }

    @Override
    public Finder<DeviceConfiguration> findActiveDeviceConfigurationsForDeviceType(DeviceType deviceType) {
        return DefaultFinder.of(DeviceConfiguration.class, where("deviceType").isEqualTo(deviceType).and(where("active").isEqualTo(true)), this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsForValidationRuleSet(long validationRuleSetId) {
        return this.getDataModel().
                query(DeviceConfiguration.class, DeviceConfValidationRuleSetUsage.class, DeviceType.class).
                select(where("deviceConfValidationRuleSetUsages.validationRuleSetId").isEqualTo(validationRuleSetId), Order.ascending("name"));
    }

    @Override
    public Finder<DeviceConfiguration> findDeviceConfigurationsForEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        Condition condition = where(DeviceConfigurationImpl.Fields.DEVICECONF_ESTIMATIONRULESET_USAGES.fieldName() + "." +
                                    DeviceConfigurationEstimationRuleSetUsageImpl.Fields.ESTIMATIONRULESET.fieldName()).isEqualTo(estimationRuleSet);
        return DefaultFinder.of(DeviceConfiguration.class, condition, dataModel, DeviceConfigurationEstimationRuleSetUsage.class).sorted("name", true);
    }

    @Override
    public List<ReadingType> getReadingTypesRelatedToConfiguration(DeviceConfiguration configuration) {
        List<ReadingType> readingTypes = new ArrayList<>();
        for (LoadProfileSpec spec : configuration.getLoadProfileSpecs()) {
            for (ChannelType channelType : spec.getLoadProfileType().getChannelTypes()) {
                ReadingType readingType = channelType.getReadingType();
                readingTypes.add(readingType);
                if (readingType.isCumulative()) {
                    Optional<ReadingType> delta = readingType.getCalculatedReadingType();
                    if (delta.isPresent()) {
                        readingTypes.add(delta.get());
                    }
                }
            }
        }
        for (RegisterSpec spec : configuration.getRegisterSpecs()) {
            readingTypes.add(spec.getRegisterType().getReadingType());
        }
        return readingTypes;
    }

    @Override
    public List<DeviceConfiguration> getLinkableDeviceConfigurations(ValidationRuleSet validationRuleSet) {
        return new LinkableConfigResolverBySql(queryService.wrap(dataModel.query(DeviceConfiguration.class, DeviceType.class))).getLinkableDeviceConfigurations(validationRuleSet);
    }

    private void initPrivileges() {
        privileges.clear();
        List<Resource> resources = userService.getResources("MDC");
        for (Resource resource : resources) {
            for (Privilege privilege : resource.getPrivileges()) {
                Optional<DeviceSecurityUserAction> found = DeviceSecurityUserAction.forPrivilege(privilege.getName());
                if (found.isPresent()) {
                    privileges.add(privilege);
                }
                Optional<DeviceMessageUserAction> deviceMessageUserAction = DeviceMessageUserAction.forPrivilege(privilege.getName());
                if(deviceMessageUserAction.isPresent()){
                    privileges.add(privilege);
                }
            }
        }
    }

    @Override
    public List<SecurityPropertySet> findUniqueSecurityPropertySets() {
        List<SecurityPropertySet> securityPropertySets = dataModel.mapper(SecurityPropertySet.class).find();
        return securityPropertySets
                .stream()
                .filter(s -> s.getId() ==
                        securityPropertySets.stream()
                                .filter(s2 -> s2.getName().equals(s.getName()))
                                .sorted((s3, s4) -> s4.getAuthenticationDeviceAccessLevel().getId() - s3.getAuthenticationDeviceAccessLevel().getId())
                                .sorted((s3, s4) -> s4.getEncryptionDeviceAccessLevel().getId() - s3.getEncryptionDeviceAccessLevel().getId())
                                .findFirst().get().getId())
                .collect(Collectors.toList());
    }

    @Override
    public boolean usedByDeviceConfigurations(ComTask comTask) {
        return !this.dataModel
                    .mapper(ComTaskEnablement.class)
                    .find(ComTaskEnablementImpl.Fields.COM_TASK.fieldName(), comTask)
                    .isEmpty();
    }

    @Override
    public DeviceConfiguration cloneDeviceConfiguration(DeviceConfiguration templateDeviceConfiguration, String name) {
        return ((ServerDeviceConfiguration) templateDeviceConfiguration).clone(name);
    }
}