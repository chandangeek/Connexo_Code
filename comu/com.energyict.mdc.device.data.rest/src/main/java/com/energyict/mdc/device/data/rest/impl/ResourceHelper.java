package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.kpi.rest.DataCollectionKpiInfo;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.impl.DeviceLifeCycleConfigApplication;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.not;

public class ResourceHelper {

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final LoadProfileService loadProfileService;
    private final CommunicationTaskService communicationTaskService;
    private final MeteringGroupsService meteringGroupsService;
    private final ConnectionTaskService connectionTaskService;
    private final DeviceMessageService deviceMessageService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DataCollectionKpiService dataCollectionKpiService;
    private final EstimationService estimationService;
    private final MasterDataService masterDataService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final CustomPropertySetService customPropertySetService;
    private final TopologyService topologyService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public ResourceHelper(DeviceService deviceService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, DeviceConfigurationService deviceConfigurationService, LoadProfileService loadProfileService, CommunicationTaskService communicationTaskService, MeteringGroupsService meteringGroupsService, ConnectionTaskService connectionTaskService, DeviceMessageService deviceMessageService, ProtocolPluggableService protocolPluggableService, DataCollectionKpiService dataCollectionKpiService, EstimationService estimationService, MdcPropertyUtils mdcPropertyUtils, CustomPropertySetService customPropertySetService, Clock clock, MasterDataService masterDataService, TopologyService topologyService, NlsService nlsService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super();
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.loadProfileService = loadProfileService;
        this.communicationTaskService = communicationTaskService;
        this.meteringGroupsService = meteringGroupsService;
        this.connectionTaskService = connectionTaskService;
        this.deviceMessageService = deviceMessageService;
        this.protocolPluggableService = protocolPluggableService;
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.estimationService = estimationService;
        this.masterDataService = masterDataService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.customPropertySetService = customPropertySetService;
        this.topologyService = topologyService;
        this.clock = clock;

        this.thesaurus = nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(DeviceLifeCycleConfigApplication.DEVICE_CONFIG_LIFECYCLE_COMPONENT, Layer.REST));
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public Long getCurrentDeviceConfigurationVersion(long id) {
        return deviceConfigurationService.findDeviceConfiguration(id).map(DeviceConfiguration::getVersion).orElse(null);
    }

    public Optional<DeviceConfiguration> getLockedDeviceConfiguration(long id, long version) {
        return deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, version);
    }

    public Device findDeviceByNameOrThrowException(String deviceName) {
        return deviceService.findDeviceByName(deviceName).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, deviceName));
    }

    public Long getCurrentDeviceVersion(String deviceName) {
        return deviceService.findDeviceByName(deviceName).map(Device::getVersion).orElse(null);
    }

    public Optional<Device> getLockedDevice(String deviceName, long version) {
        return deviceService.findAndLockDeviceByNameAndVersion(deviceName, version);
    }

    public Device lockDeviceOrThrowException(DeviceVersionInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedDevice(info.name, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentDeviceVersion(info.name))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentDeviceVersion(info.name))
                .build();
    }

    public Device lockDeviceOrThrowException(long deviceId, String name, long deviceVersion) {
        return deviceService.findAndLockDeviceByIdAndVersion(deviceId, deviceVersion)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> getCurrentDeviceVersion(name))
                        .supplier());
    }

    public void lockDeviceTypeOrThrowException(long id, long version) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_TYPE, id));
        deviceConfigurationService
                .findAndLockDeviceType(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(deviceType.getName())
                        .withActualVersion(deviceType::getVersion)
                        .supplier());
    }

    public void lockChannelSpecOrThrowException(long channelSpecId, long channelSpecVersion, Channel channel) {
        deviceConfigurationService.findAndLockChannelSpecByIdAndVersion(channelSpecId, channelSpecVersion)
                .orElseThrow(conflictFactory.contextDependentConflictOn("Channel")
                        .withActualVersion(() -> channel.getChannelSpec().getVersion())
                        .supplier());
    }

    public void lockLoadProfileTypeOrThrowException(long id, long version) {
        LoadProfileType loadProfileType = masterDataService.findLoadProfileType(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_LOAD_PROFILE_TYPE, id));
        masterDataService
                .findAndLockLoadProfileTypeByIdAndVersion(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(loadProfileType.getName())
                        .withActualVersion(loadProfileType::getVersion)
                        .supplier());
    }

    public void lockRegisterSpecOrThrowException(long registerSpecId, long registerSpecVersion, Register register) {
        deviceConfigurationService.findAndLockRegisterSpecByIdAndVersion(registerSpecId, registerSpecVersion)
                .orElseThrow(conflictFactory.contextDependentConflictOn("Register")
                        .withActualVersion(() -> register.getRegisterSpec().getVersion())
                        .supplier());
    }

    public void lockRegisterTypeOrThrowException(long id, long version) {
        RegisterType registerType = masterDataService.findRegisterType(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_REGISTER_TYPE, id));
        masterDataService
                .findAndLockRegisterTypeByIdAndVersion(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(registerType.getDescription())
                        .withActualVersion(registerType::getVersion)
                        .supplier());
    }

    public Long getCurrentLoadProfileVersion(long id) {
        return loadProfileService.findById(id).map(LoadProfile::getVersion).orElse(null);
    }

    public Optional<LoadProfile> getLockedLoadProfile(long id, long version) {
        return loadProfileService.findAndLockLoadProfileByIdAndVersion(id, version);
    }

    public LoadProfile lockLoadProfileOrThrowException(LoadProfileTriggerValidationInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedLoadProfile(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentLoadProfileVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentLoadProfileVersion(info.id))
                .build();
    }

    public Long getCurrentComTaskExecutionVersion(long id) {
        return communicationTaskService.findComTaskExecution(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ComTaskExecution::getVersion)
                .orElse(null);
    }

    public Optional<ComTaskExecution> getLockedComTaskExecution(long id, long version) {
        return communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public ComTaskExecution findComTaskExecutionOrThrowException(long id) {
        return communicationTaskService.findComTaskExecution(id)
                .orElseThrow(() -> new WebApplicationException("No ComTaskExecution with id " + id, Response.Status.NOT_FOUND));
    }

    public ComTaskExecution lockComTaskExecutionOrThrowException(DeviceSchedulesInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedComTaskExecution(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentComTaskExecutionVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentComTaskExecutionVersion(info.id))
                .build();
    }

    public Long getCurrentSecurityPropertySetVersion(long id) {
        return deviceConfigurationService.findSecurityPropertySet(id).map(SecurityPropertySet::getVersion).orElse(null);
    }

    public Optional<SecurityPropertySet> getLockedSecurityPropertySet(long id, long version) {
        return deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(id, version);
    }

    public SecurityPropertySet lockSecurityPropertySetOrThrowException(SecurityPropertySetInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedSecurityPropertySet(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentSecurityPropertySetVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentSecurityPropertySetVersion(info.id))
                .build();
    }

    public Long getCurrentConnectionTaskVersion(long id) {
        return connectionTaskService.findConnectionTask(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ConnectionTask::getVersion)
                .orElse(null);
    }

    public Optional<ConnectionTask> getLockedConnectionTask(long id, long version) {
        return connectionTaskService.findAndLockConnectionTaskByIdAndVersion(id, version);
    }

    public ConnectionTask lockConnectionTaskOrThrowException(ConnectionTaskVersionInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedConnectionTask(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentConnectionTaskVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentConnectionTaskVersion(info.id))
                .build();
    }

    public DeviceMessage findDeviceMessageOrThrowException(long id) {
        return deviceMessageService.findDeviceMessageById(id)
                .orElseThrow(() -> new WebApplicationException("No DeviceMessage with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentDeviceMessageVersion(long id) {
        return deviceMessageService.findDeviceMessageById(id).map(DeviceMessage::getVersion).orElse(null);
    }

    public Optional<DeviceMessage> getLockedDeviceMessage(long id, long version) {
        return deviceMessageService.findAndLockDeviceMessageByIdAndVersion(id, version);
    }

    public DeviceMessage lockDeviceMessageOrThrowException(DeviceMessageInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedDeviceMessage(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.messageSpecification.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentDeviceMessageVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.messageSpecification.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentDeviceMessageVersion(info.id))
                .build();
    }

    public Register findRegisterOrThrowException(Device device, long registerSpecId) {
        return device.getRegisters()
                .stream()
                .filter(r -> r.getRegisterSpec().getId() == registerSpecId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_REGISTER, registerSpecId));
    }

    public LoadProfile findLoadProfileOrThrowException(Device device, long loadProfileId) {
        return device.getLoadProfiles()
                .stream()
                .filter(lp -> lp.getId() == loadProfileId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, device.getName(), loadProfileId));
    }

    public Channel findChannelOnDeviceOrThrowException(String deviceName, long channelId) {
        Device device = this.findDeviceByNameOrThrowException(deviceName);
        return this.findChannelOnDeviceOrThrowException(device, channelId);
    }

    public Channel findChannelOnDeviceOrThrowException(Device device, long channelId) {
        return device.getChannels().stream().filter(c -> c.getId() == channelId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CHANNEL_ON_DEVICE, device.getName(), channelId));
    }

    public Register findRegisterOnDeviceOrThrowException(String deviceName, long registerId) {
        Device device = this.findDeviceByNameOrThrowException(deviceName);
        return this.findRegisterOnDeviceOrThrowException(device, registerId);
    }

    public Register findRegisterOnDeviceOrThrowException(Device device, long registerId) {
        return device.getRegisters().stream().filter(r -> r.getRegisterSpecId() == registerId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_REGISTER_ON_DEVICE, device.getName(), registerId));
    }


    public EndDeviceGroup findEndDeviceGroupOrThrowException(long id) {
        return meteringGroupsService.findEndDeviceGroup(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    public Long getCurrentEndDeviceGroupVersion(long id) {
        return meteringGroupsService.findEndDeviceGroup(id).map(EndDeviceGroup::getVersion).orElse(null);
    }

    public Optional<EndDeviceGroup> getLockedEndDeviceGroup(long id, long version) {
        return meteringGroupsService.findAndLockEndDeviceGroupByIdAndVersion(id, version);
    }

    public EndDeviceGroup lockEndDeviceGroupOrThrowException(DeviceGroupInfo info) {
        return getLockedEndDeviceGroup(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentEndDeviceGroupVersion(info.id))
                        .supplier());
    }

    public Long getCurrentDeviceProtocolPluggableClassVersion(long id) {
        return protocolPluggableService.findDeviceProtocolPluggableClass(id).map(DeviceProtocolPluggableClass::getEntityVersion).orElse(null);
    }

    public Optional<DeviceProtocolPluggableClass> getLockedDeviceProtocolPluggableClass(long id, long version) {
        return protocolPluggableService.findAndLockDeviceProtocolPluggableClassByIdAndVersion(id, version);
    }

    public DeviceProtocolPluggableClass lockDeviceProtocolPluggableClassOrThrowException(DeviceProtocolInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedDeviceProtocolPluggableClass(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentDeviceProtocolPluggableClassVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentDeviceProtocolPluggableClassVersion(info.id))
                .build();
    }

    public DataCollectionKpi findDataCollectionKpiByIdOrThrowException(long id) {
        return dataCollectionKpiService.findDataCollectionKpi(id)
                .orElseThrow(() -> new WebApplicationException("No DeviceProtocolPluggableClass with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentDataCollectionKpiVersion(long id) {
        return dataCollectionKpiService.findDataCollectionKpi(id).map(DataCollectionKpi::getVersion).orElse(null);
    }

    public Optional<DataCollectionKpi> getLockedDataCollectionKpi(long id, long version) {
        return dataCollectionKpiService.findAndLockDataCollectionKpiByIdAndVersion(id, version);
    }

    public DataCollectionKpi lockDataCollectionKpiOrThrowException(DataCollectionKpiInfo info) {
        return getLockedDataCollectionKpi(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.deviceGroup.name)
                        .withActualVersion(() -> getCurrentDataCollectionKpiVersion(info.id))
                        .supplier());
    }

    public Long getCurrentEstimationRuleSetVersion(long id) {
        return estimationService.getEstimationRuleSet(id)
                .filter(candidate -> candidate.getObsoleteDate() != null)
                .map(EstimationRuleSet::getVersion)
                .orElse(null);
    }

    public Optional<? extends EstimationRuleSet> getLockedEstimationRuleSet(long id, long version) {
        return estimationService.findAndLockEstimationRuleSet(id, version);
    }

    public EstimationRuleSet lockEstimationRuleSetOrThrowException(DeviceEstimationRuleSetRefInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedEstimationRuleSet(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentEstimationRuleSetVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentEstimationRuleSetVersion(info.id))
                .build();
    }

    public Condition getQueryConditionForDevice(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if (!params.getQueryParameters().isEmpty()) {
            condition = condition.and(addDeviceQueryCondition(params));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(StandardParametersBean params) {
        Condition conditionDevice = Condition.TRUE;
        String name = params.getFirst("name");
        if (name != null) {
            conditionDevice = conditionDevice.and(where("name").likeIgnoreCase(name));
        }
        String serialNumber = params.getFirst("serialNumber");
        if (serialNumber != null) {
            conditionDevice = conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        String deviceType = params.getFirst("deviceTypeName");
        if (deviceType != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceType, "deviceConfiguration.deviceType.name"));
        }
        String deviceConfiguration = params.getFirst("deviceConfigurationName");
        if (deviceConfiguration != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfiguration, "deviceConfiguration.name"));
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(String params, String conditionField) {
        Condition condition = Condition.FALSE;
        String[] values = params.split(",");
        for (String value : values) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }

    public ConnectionTask<?, ?> findConnectionTaskOrThrowException(Device device, long connectionMethodId) {
        return device.getConnectionTasks()
                .stream()
                .filter(ct -> ct.getId() == connectionMethodId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_METHOD, device.getName(), connectionMethodId));
    }

    public Condition getQueryConditionForDevice(MultivaluedMap<String, String> uriParams) {
        Condition condition = Condition.TRUE;
        if (uriParams.containsKey("filter")) {
            condition = condition.and(addDeviceQueryCondition(uriParams));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(MultivaluedMap<String, String> uriParams) {
        Condition conditionDevice = Condition.TRUE;
        JsonQueryFilter filter = new JsonQueryFilter(uriParams.getFirst("filter"));
        String name = filter.getString("name");
        if (name != null) {
            conditionDevice = conditionDevice.and(where("name").likeIgnoreCase(name));
        }
        String serialNumber = filter.getString("serialNumber");
        if (serialNumber != null) {
            conditionDevice = conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        if (filter.hasProperty("deviceTypes")) {
            List<Integer> deviceTypes = filter.getIntegerList("deviceTypes");
            if (!deviceTypes.isEmpty()) {
                conditionDevice = conditionDevice.and(createMultipleConditions(deviceTypes, "deviceConfiguration.deviceType.id"));
            }
        }
        if (filter.hasProperty("deviceConfigurations")) {
            List<Integer> deviceConfigurations = filter.getIntegerList("deviceConfigurations");
            if (!deviceConfigurations.isEmpty()) {
                conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfigurations, "deviceConfiguration.id"));
            }
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(List<Integer> params, String conditionField) {
        Condition condition = Condition.FALSE;
        for (int value : params) {
            condition = condition.or(where(conditionField).isEqualTo(value));
        }
        return condition;
    }

    public List<CustomPropertySetInfo> getDeviceCustomPropertySetInfos(Device device) {
        return device.getDeviceType().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(registeredCustomPropertySet -> this.getDeviceCustomPropertySetInfo(registeredCustomPropertySet, device))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private CustomPropertySetInfo getDeviceCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, Device device) {
        if (!registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            return new CustomPropertySetInfo(
                    registeredCustomPropertySet,
                    mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                            registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                            getCustomProperties(
                                    customPropertySetService.getUniqueValuesFor(
                                            registeredCustomPropertySet.getCustomPropertySet(),
                                            device))),
                    device.getId(),
                    device.getVersion(),
                    device.getDeviceType().getId(),
                    device.getDeviceType().getVersion());
        } else {
            CustomPropertySetValues customPropertySetValues =
                    customPropertySetService.getUniqueValuesFor(
                            registeredCustomPropertySet.getCustomPropertySet(),
                            device,
                            this.clock.instant());
            return new CustomPropertySetInfo(
                    registeredCustomPropertySet,
                    mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                            registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                            getCustomProperties(
                                    customPropertySetValues)),
                    device.getId(),
                    device.getVersion(),
                    device.getDeviceType().getId(),
                    device.getDeviceType().getVersion(),
                    customPropertySetValues.getEffectiveRange());
        }
    }

    public List<CustomPropertySetInfo> getDeviceCustomPropertySetInfos(Device device, Instant instant) {
        return device.getDeviceType().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(cps -> cps.getCustomPropertySet().isVersioned())
                .map(each -> this.getDeviceCustomPropertySetInfo(each, device, instant))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private CustomPropertySetInfo getDeviceCustomPropertySetInfo(RegisteredCustomPropertySet registeredCustomPropertySet, Device device, Instant instant) {
        CustomPropertySetValues customPropertySetValues =
                customPropertySetService.getUniqueValuesFor(
                        registeredCustomPropertySet.getCustomPropertySet(),
                        device,
                        instant);
        return new CustomPropertySetInfo(
                registeredCustomPropertySet,
                mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                        registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                        getCustomProperties(customPropertySetValues)),
                device.getId(),
                device.getVersion(),
                device.getDeviceType().getId(),
                device.getDeviceType().getVersion(),
                customPropertySetValues.getEffectiveRange());
    }

    @SuppressWarnings("unchecked")
    public CustomPropertySetInfo getDeviceCustomPropertySetInfoWithDefaultValues(Device device, long cpsId) {
        RegisteredCustomPropertySet registeredCustomPropertySet = device.getDeviceType().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(cps -> cps.getId() == cpsId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));

        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        return new CustomPropertySetInfo(
                registeredCustomPropertySet,
                mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                        registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                        getCustomProperties(customPropertySetValues)),
                device.getId(),
                device.getVersion(),
                device.getDeviceType().getId(),
                device.getDeviceType().getVersion(),
                customPropertySetValues.getEffectiveRange());
    }

    public List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(Device device, long cpsId) {
        return getVersionedCustomPropertySetHistoryInfos(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), cpsId, Optional.empty(), device.getDeviceType()
                .getId(), device.getDeviceType().getVersion());
    }

    public List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(Channel channel, long cpsId) {
        return getVersionedCustomPropertySetHistoryInfos(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec()
                .getVersion(), cpsId, Optional.of(channel.getDevice().getId()), channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getId(), channel.getChannelSpec()
                .getLoadProfileSpec()
                .getLoadProfileType()
                .getVersion());
    }

    public List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(Register register, long cpsId) {
        return getVersionedCustomPropertySetHistoryInfos(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec()
                .getVersion(), cpsId, Optional.of(register.getDevice().getId()), register.getRegisterSpec().getRegisterType().getId(), register.getRegisterSpec().getRegisterType().getVersion());
    }

    public <D> List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, long cpsId, Optional<Object> object, long objectTypeId, long objectTypeVersion) {
        return Stream.of(registeredCustomPropertySet)
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(cps -> cps.getCustomPropertySet().isVersioned())
                .filter(cps -> cps.getId() == cpsId)
                .flatMap(cps -> getHistoryInfo(cps, businessObject, businessObjectId, businessObjectVersion, object, objectTypeId, objectTypeVersion))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <D> Stream<CustomPropertySetInfo> getHistoryInfo(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, Optional<Object> object, long objectTypeId, long objectTypeVersion) {
        List<CustomPropertySetValues> values;
        if (object.isPresent()) {
            values = customPropertySetService.getAllVersionedValuesFor(registeredCustomPropertySet.getCustomPropertySet(), businessObject, object.get());
        } else {
            values = customPropertySetService.getAllVersionedValuesFor(registeredCustomPropertySet.getCustomPropertySet(), businessObject);
        }
        return values.stream()
                .map(v -> new CustomPropertySetInfo(
                        registeredCustomPropertySet,
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(v)),
                        businessObjectId,
                        businessObjectVersion,
                        objectTypeId,
                        objectTypeVersion,
                        v.getEffectiveRange()));
    }

    @SuppressWarnings("unchecked")
    public void addDeviceCustomPropertySetVersioned(Device device, long cpsId, CustomPropertySetInfo info) {
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(device, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), device, getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                .getPropertySpecs(), info), newRange);
        device.save();
    }

    @SuppressWarnings("unchecked")
    public void setDeviceCustomPropertySetInfo(Device device, long cpsId, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(device, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        customPropertySetService.setValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                .getPropertySpecs(), info));
        device.save();
    }

    @SuppressWarnings("unchecked")
    public void setDeviceCustomPropertySetVersioned(Device device, long cpsId, CustomPropertySetInfo info, Instant effectiveTimestamp) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(device, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), device, getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                .getPropertySpecs(), info), newRange, effectiveTimestamp);
        device.save();
    }

    @SuppressWarnings("unchecked")
    public void addChannelCustomPropertySetVersioned(Channel channel, long cpsId, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(channel, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_CHANNEL, cpsId, channel.getId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), channel.getChannelSpec(), getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                .getPropertySpecs(), info), newRange, channel.getDevice()
                .getId());
        channel.getChannelSpec().save();
    }

    @SuppressWarnings("unchecked")
    public void setChannelCustomPropertySetVersioned(Channel channel, long cpsId, CustomPropertySetInfo info, Instant effectiveTimestamp) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(channel, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_CHANNEL, cpsId, channel.getId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), channel.getChannelSpec(), getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                .getPropertySpecs(), info), newRange, effectiveTimestamp, channel
                .getDevice()
                .getId());
        channel.getChannelSpec().save();
    }

    @SuppressWarnings("unchecked")
    public void addRegisterCustomPropertySetVersioned(Register register, long cpsId, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(register, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_REGISTER, cpsId, register.getRegisterSpecId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), register.getRegisterSpec(), getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                .getPropertySpecs(), info), newRange, register.getDevice()
                .getId());
        register.getRegisterSpec().save();
    }

    @SuppressWarnings("unchecked")
    public void setRegisterCustomPropertySetVersioned(Register register, long cpsId, CustomPropertySetInfo info, Instant effectiveTimestamp) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(register, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_REGISTER, cpsId, register.getRegisterSpecId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), register.getRegisterSpec(), getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                .getPropertySpecs(), info), newRange, effectiveTimestamp, register
                .getDevice()
                .getId());
        register.getRegisterSpec().save();
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenUpdate(Device device, long cpsId, Range<Instant> range, Instant effectiveTimestamp) {
        return getOverlaps(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), range, device.getDeviceType().getId(), device.getDeviceType()
                .getVersion(), effectiveTimestamp);
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenCreate(Device device, long cpsId, Range<Instant> range) {
        return getOverlaps(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), range, device.getDeviceType().getId(), device.getDeviceType().getVersion());
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenUpdate(Channel channel, long cpsId, Range<Instant> range, Instant effectiveTimestamp) {
        return getOverlaps(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec()
                .getVersion(), range, channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getId(), channel.getChannelSpec()
                .getLoadProfileSpec()
                .getLoadProfileType()
                .getVersion(), effectiveTimestamp, channel.getDevice().getId());
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenCreate(Channel channel, long cpsId, Range<Instant> range) {
        return getOverlaps(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec()
                .getVersion(), range, channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getId(), channel.getChannelSpec()
                .getLoadProfileSpec()
                .getLoadProfileType()
                .getVersion(), channel.getDevice().getId());
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenUpdate(Register register, long cpsId, Range<Instant> range, Instant effectiveTimestamp) {
        return getOverlaps(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec()
                .getVersion(), range, register.getRegisterSpec().getRegisterType().getId(), register.getRegisterSpec().getRegisterType().getVersion(), effectiveTimestamp, register.getDevice()
                .getId());
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenCreate(Register register, long cpsId, Range<Instant> range) {
        return getOverlaps(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec()
                .getVersion(), range, register.getRegisterSpec().getRegisterType().getId(), register.getRegisterSpec().getRegisterType().getVersion(), register.getDevice().getId());
    }

    @SuppressWarnings("unchecked")
    public <D> List<CustomPropertySetIntervalConflictInfo> getOverlaps(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, Range<Instant> newRange, long objectTypeId, long objectTypeVersion, Object... additionalPrimaryKeyValues) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(registeredCustomPropertySet.getCustomPropertySet(), businessObject, additionalPrimaryKeyValues);
        return overlapCalculatorBuilder.whenCreating(newRange).stream().map(e -> new CustomPropertySetIntervalConflictInfo(
                new CustomPropertySetInfo(
                        registeredCustomPropertySet,
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(e.getValues())),
                        businessObjectId,
                        businessObjectVersion,
                        objectTypeId,
                        objectTypeVersion,
                        e.getValues().getEffectiveRange()),
                e)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <D> List<CustomPropertySetIntervalConflictInfo> getOverlaps(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, Range<Instant> newRange, long objectTypeId, long objectTypeVersion, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(registeredCustomPropertySet.getCustomPropertySet(), businessObject, additionalPrimaryKeyValues);
        return overlapCalculatorBuilder.whenUpdating(effectiveTimestamp, newRange).stream().map(e -> new CustomPropertySetIntervalConflictInfo(
                new CustomPropertySetInfo(
                        registeredCustomPropertySet,
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(e.getValues())),
                        businessObjectId,
                        businessObjectVersion,
                        objectTypeId,
                        objectTypeVersion,
                        e.getValues().getEffectiveRange()),
                e)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public CustomPropertySetInfo getRegisterCustomPropertySetInfo(Register register, Instant effectiveTimestamp) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getRegisteredCustomPropertySet(register);
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isViewableByCurrentUser()) {
            if (!registeredCustomPropertySet.get().getCustomPropertySet().isVersioned()) {
                return new CustomPropertySetInfo(
                        registeredCustomPropertySet.get(),
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(customPropertySetService.getUniqueValuesFor(
                                        registeredCustomPropertySet.get().getCustomPropertySet(),
                                        register.getRegisterSpec(),
                                        register.getDevice().getId()))),
                        register.getRegisterSpec().getId(),
                        register.getRegisterSpec().getVersion(),
                        register.getRegisterSpec().getRegisterType().getId(),
                        register.getRegisterSpec().getRegisterType().getVersion());
            } else {
                CustomPropertySetValues customPropertySetValues =
                        customPropertySetService.getUniqueValuesFor(
                                registeredCustomPropertySet.get().getCustomPropertySet(),
                                register.getRegisterSpec(),
                                effectiveTimestamp,
                                register.getDevice().getId());
                return new CustomPropertySetInfo(
                        registeredCustomPropertySet.get(),
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(customPropertySetValues)),
                        register.getRegisterSpec().getId(),
                        register.getRegisterSpec().getVersion(),
                        register.getRegisterSpec().getRegisterType().getId(),
                        register.getRegisterSpec().getRegisterType().getVersion(),
                        customPropertySetValues.getEffectiveRange());
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public void setRegisterCustomPropertySet(Register register, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(register)
                .orElseThrow(conflictException(info));

        if (registeredCustomPropertySet.isEditableByCurrentUser() && matches(info, registeredCustomPropertySet)) {
            customPropertySetService.setValuesFor(registeredCustomPropertySet.getCustomPropertySet(), register.getRegisterSpec(), getCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet()
                    .getPropertySpecs(), info), register.getDevice().getId());
            register.getRegisterSpec().save();
        } else {
            throw conflictException(info).get();
        }
    }

    private Supplier<LocalizedException> conflictException(CustomPropertySetInfo info) {
        return () -> exceptionFactory.newException(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, info.id);
    }

    private boolean matches(CustomPropertySetInfo info, RegisteredCustomPropertySet registeredCustomPropertySet) {
        return registeredCustomPropertySet.getCustomPropertySet().getId().equals(info.customPropertySetId);
    }

    private Optional<RegisteredCustomPropertySet> getRegisteredCustomPropertySet(Register register) {
        return register.getDevice().getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType());
    }

    @SuppressWarnings("unchecked")
    public CustomPropertySetInfo getChannelCustomPropertySetInfo(Channel channel, Instant effectiveTimestamp) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = channel.getDevice()
                .getDeviceType()
                .getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isViewableByCurrentUser()) {
            if (!registeredCustomPropertySet.get().getCustomPropertySet().isVersioned()) {
                return new CustomPropertySetInfo(
                        registeredCustomPropertySet.get(),
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(customPropertySetService.getUniqueValuesFor(
                                        registeredCustomPropertySet.get().getCustomPropertySet(),
                                        channel.getChannelSpec(),
                                        channel.getDevice().getId()))),
                        channel.getChannelSpec().getId(),
                        channel.getChannelSpec().getVersion(),
                        channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getId(),
                        channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getVersion());
            } else {
                CustomPropertySetValues customPropertySetValues =
                        customPropertySetService.getUniqueValuesFor(
                                registeredCustomPropertySet.get().getCustomPropertySet(),
                                channel.getChannelSpec(),
                                effectiveTimestamp,
                                channel.getDevice().getId());
                return new CustomPropertySetInfo(
                        registeredCustomPropertySet.get(),
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(customPropertySetValues)),
                        channel.getChannelSpec().getId(),
                        channel.getChannelSpec().getVersion(),
                        channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getId(),
                        channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getVersion(),
                        customPropertySetValues.getEffectiveRange());
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public void setChannelCustomPropertySet(Channel channel, CustomPropertySetInfo info) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = channel.getDevice()
                .getDeviceType()
                .getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isEditableByCurrentUser()) {
            customPropertySetService.setValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), channel.getChannelSpec(), getCustomPropertySetValues(registeredCustomPropertySet.get()
                    .getCustomPropertySet()
                    .getPropertySpecs(), info), channel.getDevice().getId());
            channel.getChannelSpec().save();
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, info.id);
        }
    }

    private TypedProperties getCustomProperties(CustomPropertySetValues customPropertySetValues) {
        TypedProperties typedProperties = TypedProperties.empty();
        customPropertySetValues.propertyNames().forEach(propertyName ->
                typedProperties.setProperty(propertyName, customPropertySetValues.getProperty(propertyName)));
        return typedProperties;
    }

    private CustomPropertySetValues getCustomPropertySetValues(List<PropertySpec> propertySpecs, CustomPropertySetInfo info) {
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        propertySpecs.forEach(propertySpec ->
                customPropertySetValues.setProperty(propertySpec.getName(), this.mdcPropertyUtils.findPropertyValue(propertySpec, info.properties)));
        return customPropertySetValues;
    }

    public Range<Instant> getCurrentTimeInterval(Device device, long cpsId) {
        return getCurrentTimeInterval(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), cpsId, Optional.empty(), device.getDeviceType()
                .getId(), device.getDeviceType().getVersion());
    }

    public Range<Instant> getCurrentTimeInterval(Channel channel, long cpsId) {
        return getCurrentTimeInterval(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec().getVersion(), cpsId, Optional
                .of(channel.getDevice().getId()), channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType().getId(), channel.getChannelSpec()
                .getLoadProfileSpec()
                .getLoadProfileType()
                .getVersion());
    }

    public Range<Instant> getCurrentTimeInterval(Register register, long cpsId) {
        return getCurrentTimeInterval(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec()
                .getVersion(), cpsId, Optional.of(register.getDevice().getId()), register.getRegisterSpec().getRegisterType().getId(), register.getRegisterSpec().getRegisterType().getVersion());
    }

    public <D> Range<Instant> getCurrentTimeInterval(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, long cpsId, Optional<Object> object, long objectTypeId, long objectTypeVersion) {
        List<CustomPropertySetInfo> customPropertySetInfo = this.getVersionedCustomPropertySetHistoryInfos(registeredCustomPropertySet, businessObject, businessObjectId, businessObjectVersion, cpsId, object, objectTypeId, objectTypeVersion)
                .stream().filter(e -> e.id == cpsId).collect(Collectors.toList());
        Instant now = this.clock.instant();
        Optional<CustomPropertySetInfo> curentInterval = customPropertySetInfo.stream().filter(e -> getTimeRange(e.startTime, e.endTime).contains(this.clock.instant())).findFirst();
        if (curentInterval.isPresent()) {
            return getTimeRange(now.toEpochMilli(), curentInterval.get().endTime);
        }
        OptionalLong lastEndpoint = customPropertySetInfo.stream().map(e -> getTimeRange(e.startTime, e.endTime))
                .mapToLong(i -> i.hasUpperBound() ? i.upperEndpoint().toEpochMilli() : Long.MAX_VALUE)
                .max();
        if (lastEndpoint.isPresent() && lastEndpoint.getAsLong() < Long.MAX_VALUE) {
            return getTimeRange(lastEndpoint.getAsLong(), null);
        } else {
            return Range.all();
        }
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet(Device device, long cpsId) {
        return device.getDeviceType().getCustomPropertySets().stream()
                .filter(cps -> cps.getId() == cpsId && cps.isViewableByCurrentUser())
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet(Channel channel, long cpsId) {
        return channel.getDevice().getDeviceType().getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType())
                .filter(f -> f.getId() == cpsId && f.isViewableByCurrentUser())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_CHANNEL, cpsId, channel.getId()));
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet(Register register, long cpsId) {
        return register.getDevice().getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType())
                .filter(f -> f.getId() == cpsId && f.isViewableByCurrentUser())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_REGISTER, cpsId, register.getRegisterSpecId()));
    }

    public Optional<IntervalErrorInfos> verifyTimeRange(Long startTime, Long endTime) {
        long startTimeConverted = startTime != null && startTime > 0 ? startTime.longValue() : 0;
        long endTimeConverted = endTime != null && endTime > 0 ? endTime.longValue() : Long.MAX_VALUE;
        return endTimeConverted <= startTimeConverted ? Optional.of(new IntervalErrorInfos()) : Optional.empty();
    }

    public Range<Instant> getTimeRange(Long startTime, Long endTime) {
        Range<Instant> range;
        try {
            if (startTime == null || startTime <= 0) {
                if (endTime == null || endTime == 0) {
                    range = Range.all();
                } else {
                    range = Range.lessThan(Instant.ofEpochMilli(endTime));
                }
            } else if (endTime == null || endTime == 0) {
                range = Range.atLeast(Instant.ofEpochMilli(startTime));
            } else {
                range = Range.closedOpen(Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime));
            }
        } catch (IllegalArgumentException e) {
            throw exceptionFactory.newException(MessageSeeds.INTERVAL_INVALID, Instant.ofEpochMilli(startTime).toString(), Instant.ofEpochMilli(endTime).toString());
        }
        if (range.isEmpty()) {
            throw exceptionFactory.newException(MessageSeeds.INTERVAL_EMPTY);
        }
        return range;
    }

    public Comparator<CustomPropertySetIntervalConflictInfo> getConflictInfosComparator() {
        return (first, second) -> {
            Range<Instant> firstRange = getTimeRange(first.customPropertySet.startTime, first.customPropertySet.endTime);
            Range<Instant> secondRange = getTimeRange(second.customPropertySet.startTime, second.customPropertySet.endTime);
            long firstLowerEndpoint = firstRange.hasLowerBound() ? firstRange.lowerEndpoint().toEpochMilli() : 0;
            long firstUpperEndpoint = firstRange.hasUpperBound() ? firstRange.upperEndpoint().toEpochMilli() : Long.MAX_VALUE;
            long secondLowerEndpoint = secondRange.hasLowerBound() ? secondRange.lowerEndpoint().toEpochMilli() : 0;
            long secondUpperEndpoint = secondRange.hasUpperBound() ? secondRange.upperEndpoint().toEpochMilli() : Long.MAX_VALUE;
            return Long.compare(firstLowerEndpoint, secondLowerEndpoint) != 0 ? Long.compare(firstLowerEndpoint, secondLowerEndpoint) : Long.compare(firstUpperEndpoint, secondUpperEndpoint);
        };
    }

    public Predicate<CustomPropertySetIntervalConflictInfo> filterGaps(Boolean filterGaps) {
        return filterGaps ?
                e -> e.conflictType.equals(ValuesRangeConflictType.RANGE_GAP_AFTER.name()) || e.conflictType.equals(ValuesRangeConflictType.RANGE_GAP_BEFORE.name()) :
                e -> true;
    }

    public <T> Predicate<T> getSuspectsFilter(JsonQueryFilter filter, Predicate<T> hasSuspects) {
        ImmutableList.Builder<Predicate<T>> list = ImmutableList.builder();
        if (filter.hasProperty("suspect")) {
            List<String> suspectFilters = filter.getStringList("suspect");
            if (suspectFilters.size() == 1) {
                if ("suspect".equals(suspectFilters.get(0))) {
                    list.add(hasSuspects);
                } else {
                    list.add(not(hasSuspects));
                }
            }
        }
        return info -> list.build().stream().allMatch(p -> p.test(info));
    }

    public IdWithNameInfo getApplicationInfo(QualityCodeSystem system) {
        switch (system) {
            case MDC:
                return new IdWithNameInfo(system.name(), "MultiSense");
            case MDM:
                return new IdWithNameInfo(system.name(), "Insight");
            default:
                return new IdWithNameInfo(system.name(), system.name());
        }
    }

    public List<DeviceTopologyInfo> getDataLoggerSlaves(Device device) {
        List<Device> slaves = topologyService.findDataLoggerSlaves(device);
        return slaves
                .stream()
                .map(slave -> DeviceTopologyInfo.from(slave, getLinkingDate(slave), deviceLifeCycleConfigurationService))
                .collect(Collectors.toList());
    }

    private Optional<Instant> getLinkingDate(Device slave) {
        return topologyService.findDataloggerReference(slave, clock.instant())
                .map(dataLoggerReference -> dataLoggerReference.getRange().lowerEndpoint());
    }

    /**
     * Returns a combined thesaurus
     */
    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}
