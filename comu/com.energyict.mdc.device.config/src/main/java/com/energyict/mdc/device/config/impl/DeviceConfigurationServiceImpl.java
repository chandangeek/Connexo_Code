package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
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
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:38)
 */
@Component(name="com.energyict.mdc.device.config", service = {DeviceConfigurationService.class, ServerDeviceConfigurationService.class, InstallService.class}, property = "name=" + DeviceConfigurationService.COMPONENTNAME, immediate = true)
public class DeviceConfigurationServiceImpl implements ServerDeviceConfigurationService, InstallService {

    private volatile ProtocolPluggableService protocolPluggableService;

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile MdcReadingTypeUtilService readingTypeUtilService;
    private volatile EngineModelService engineModelService;
    private volatile MasterDataService masterDataService;
    private volatile SchedulingService schedulingService;
    private volatile UserService userService;
    private volatile TaskService taskService;
    private volatile PluggableService pluggableService;
    private volatile ValidationService validationService;
    private volatile QueryService queryService;

    private final Map<DeviceSecurityUserAction, Privilege> privileges = new EnumMap<>(DeviceSecurityUserAction.class);

    public DeviceConfigurationServiceImpl() {
        super();
    }

    @Inject
    public DeviceConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, UserService userService, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService, MasterDataService masterDataService, SchedulingService schedulingService, ValidationService validationService) {
        this(ormService, eventService, nlsService, meteringService, mdcReadingTypeUtilService, protocolPluggableService, userService, engineModelService, masterDataService, false, schedulingService, validationService);
    }

    public DeviceConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, ProtocolPluggableService protocolPluggableService, UserService userService, EngineModelService engineModelService, MasterDataService masterDataService, boolean createMasterData, SchedulingService schedulingService, ValidationService validationService) {
        this();
        this.setOrmService(ormService);
        this.setUserService(userService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setReadingTypeUtilService(mdcReadingTypeUtilService);
        this.setEngineModelService(engineModelService);
        this.setMasterDataService(this.masterDataService);
        this.setSchedulingService(schedulingService);
        this.setValidationService(validationService);
        this.activate();
        this.install();
    }

    @Override
    public Finder<DeviceType> findAllDeviceTypes() {
        return DefaultFinder.of(DeviceType.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public DeviceType newDeviceType(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return DeviceTypeImpl.from(this.getDataModel(), name, deviceProtocolPluggableClass);
    }

    @Override
    public DeviceType findDeviceType(long deviceTypeId) {
        return this.getDataModel().mapper((DeviceType.class)).getUnique("id", deviceTypeId).orNull();
    }

    @Override
    public DeviceType findDeviceTypeByName(String name) {
        return this.getDataModel().mapper((DeviceType.class)).getUnique("name", name).orNull();
    }

    @Override
    public DeviceConfiguration findDeviceConfiguration(long deviceConfigId) {
        return this.getDataModel().mapper((DeviceConfiguration.class)).getUnique("id", deviceConfigId).orNull();
    }

    @Override
    public ChannelSpec findChannelSpec(long channelSpecId) {
        return this.getDataModel().mapper((ChannelSpec.class)).getUnique("id", channelSpecId).orNull();
    }

    @Override
    public RegisterSpec findRegisterSpec(long id) {
        return this.getDataModel().mapper((RegisterSpec.class)).getUnique("id", id).orNull();
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
    public LoadProfileSpec findLoadProfileSpec(long loadProfileSpecId) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("id", loadProfileSpecId).orNull();
    }

    @Override
    public LoadProfileSpec findLoadProfileSpecsByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("deviceConfiguration", deviceConfig, "loadProfileType", loadProfileType).orNull();
    }

    @Override
    public List<LoadProfileSpec> findLoadProfileSpecsByLoadProfileType(LoadProfileType loadProfileType) {
        return this.getDataModel().mapper(LoadProfileSpec.class).find("loadProfileType", loadProfileType);
    }

    @Override
    public LogBookSpec findLogBookSpec(long logBookSpecId) {
        return this.getDataModel().mapper(LogBookSpec.class).getUnique("id", logBookSpecId).orNull();
    }

    @Override
    public ChannelSpec findChannelSpecForLoadProfileSpecAndChannelType(LoadProfileSpec loadProfileSpec, ChannelType channelType) {
        return this.getDataModel().mapper(ChannelSpec.class).getUnique("loadProfileSpec", loadProfileSpec, "channelType", channelType).orNull();
    }

    @Override
    public ChannelSpec findChannelSpecByDeviceConfigurationAndName(DeviceConfiguration deviceConfiguration, String name) {
        return this.getDataModel().mapper(ChannelSpec.class).getUnique("deviceConfiguration", deviceConfiguration, "name", name).orNull();
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsByDeviceType(DeviceType deviceType) {
        return this.getDataModel().mapper(DeviceConfiguration.class).find("deviceType", deviceType);
    }

    @Override
    public List<DeviceType> findDeviceTypesWithDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return this.getDataModel().mapper(DeviceType.class).find("deviceProtocolPluggableClass", deviceProtocolPluggableClass);
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
    public List<DeviceConfiguration> findDeviceConfigurationsUsingLogBookType(LogBookType logBookType) {
        return this.getDataModel().
                query(DeviceConfiguration.class, LogBookSpec.class).
                select(where("logBookSpecs.logBookType").isEqualTo(logBookType));
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsUsingMeasurementType(MeasurementType measurementType) {
        return this.getDataModel().
                query(DeviceConfiguration.class, ChannelSpec.class, RegisterSpec.class).
                select(   where("channelSpecs.channelType").isEqualTo(measurementType).
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
    public DeviceCommunicationConfiguration findDeviceCommunicationConfiguration(long id) {
        return dataModel.mapper(DeviceCommunicationConfiguration.class).getOptional(id).orNull();
    }

    @Override
    public DeviceCommunicationConfiguration findDeviceCommunicationConfigurationFor(DeviceConfiguration deviceConfiguration) {
        List<DeviceCommunicationConfiguration> configurations = DefaultFinder.of(DeviceCommunicationConfiguration.class, where("deviceConfiguration").isEqualTo(deviceConfiguration), dataModel).find();
        return configurations.isEmpty() ? null : configurations.get(0);
    }

    @Override
    public DeviceCommunicationConfiguration newDeviceCommunicationConfiguration(DeviceConfiguration deviceConfiguration) {
        return DeviceCommunicationConfigurationImpl.from(dataModel, deviceConfiguration);
    }

    @Override
    public Optional<PartialConnectionTask> getPartialConnectionTask(long id) {
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
    public boolean isPhenomenonInUse(Phenomenon phenomenon) {
        return !this.getDataModel().mapper(ChannelSpec.class).find("phenomenon", phenomenon).isEmpty();
    }

    @Override
    public Optional<ComTaskEnablement> findComTaskEnablement(long id) {
        return dataModel.mapper(ComTaskEnablement.class).getUnique("id", id);
    }

    @Override
    public Optional<ComTaskEnablement> findComTaskEnablement(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        return dataModel.
                mapper(ComTaskEnablement.class).
                getUnique(
                        ComTaskEnablementImpl.Fields.COM_TASK.fieldName(), comTask,
                        ComTaskEnablementImpl.Fields.CONFIGURATION.fieldName(), deviceConfiguration.getCommunicationConfiguration());
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

    Thesaurus getThesaurus() {
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
    public void setUserService(UserService userService) {
        this.userService = userService;
        initPrivileges();
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    Optional<Privilege> findPrivilege(DeviceSecurityUserAction userAction) {
        return Optional.fromNullable(privileges.get(userAction));
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceConfigurationService.class).toInstance(DeviceConfigurationServiceImpl.this);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MdcReadingTypeUtilService.class).toInstance(readingTypeUtilService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(EngineModelService.class).toInstance(engineModelService);
                bind(UserService.class).toInstance(userService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(ValidationService.class).toInstance(validationService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService, this.thesaurus, userService).install(true);
        initPrivileges();
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
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
    public void setMasterDataService(MasterDataService masterDataService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setPluggableService(PluggableService pluggableService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.pluggableService = pluggableService;
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
    public List<SecurityPropertySet> findAllSecurityPropertySets() {
        return dataModel.mapper(SecurityPropertySet.class).find();
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
            if (comTasks.count(comTask)!=deviceConfigurations.size()) {
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
                while(resultSet.next()) {
                    deviceConfigurations.add(this.findDeviceConfiguration(resultSet.getLong(1)));
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
    public List<ReadingType> getReadingTypesRelatedToConfiguration(DeviceConfiguration configuration) {
        List<ReadingType> readingTypes = new ArrayList<>();
        for (LoadProfileSpec spec : configuration.getLoadProfileSpecs()) {
            for (ChannelType channelType : spec.getLoadProfileType().getChannelTypes()) {
                readingTypes.add(channelType.getReadingType());
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
        List<Resource> resources = userService.getResources(COMPONENTNAME);
        for(Resource resource : resources){
            for(Privilege privilege : resource.getPrivileges()){
                Optional<DeviceSecurityUserAction> found = DeviceSecurityUserAction.forName(privilege.getName());
                if (found.isPresent()) {
                    privileges.put(found.get(), privilege);
                }
            }
        }
    }

}