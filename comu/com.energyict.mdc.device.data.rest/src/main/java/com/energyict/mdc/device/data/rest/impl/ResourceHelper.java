package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
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
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

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
    private final MdcPropertyUtils mdcPropertyUtils;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public ResourceHelper(DeviceService deviceService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, DeviceConfigurationService deviceConfigurationService, LoadProfileService loadProfileService, CommunicationTaskService communicationTaskService, MeteringGroupsService meteringGroupsService, ConnectionTaskService connectionTaskService, DeviceMessageService deviceMessageService, ProtocolPluggableService protocolPluggableService, DataCollectionKpiService dataCollectionKpiService, EstimationService estimationService, MdcPropertyUtils mdcPropertyUtils, CustomPropertySetService customPropertySetService) {
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
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.customPropertySetService = customPropertySetService;
    }

    public DeviceConfiguration findDeviceConfigurationByIdOrThrowException(long id) {
        return deviceConfigurationService.findDeviceConfiguration(id)
                .orElseThrow(() -> new WebApplicationException("No DeviceConfiguration with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentDeviceConfigurationVersion(long id) {
        return deviceConfigurationService.findDeviceConfiguration(id).map(DeviceConfiguration::getVersion).orElse(null);
    }

    public Optional<DeviceConfiguration> getLockedDeviceConfiguration(long id, long version) {
        return deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, version);
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        return deviceService.findByUniqueMrid(mRID).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, mRID));
    }

    public Long getCurrentDeviceVersion(String mRID) {
        return deviceService.findByUniqueMrid(mRID).map(Device::getVersion).orElse(null);
    }

    public Optional<Device> getLockedDevice(String mRID, long version) {
        return deviceService.findAndLockDeviceBymRIDAndVersion(mRID, version);
    }

    public Device lockDeviceOrThrowException(DeviceVersionInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedDevice(info.mRID, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.mRID)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentDeviceVersion(info.mRID))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.mRID)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.version)
                .withActualVersion(() -> getCurrentDeviceVersion(info.mRID))
                .build();
    }

    public LoadProfile findLoadProfileOrThrowException(long id) {
        return loadProfileService.findById(id)
                .orElseThrow(() -> new WebApplicationException("No LoadProfile with id " + id, Response.Status.NOT_FOUND));
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
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentLoadProfileVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                .withActualVersion(() -> getCurrentLoadProfileVersion(info.id))
                .build();
    }

    public ComTaskExecution findComTaskExecutionOrThrowException(long id) {
        return communicationTaskService.findComTaskExecution(id)
                .orElseThrow(() -> new WebApplicationException("No ComTaskExecution with id " + id, Response.Status.NOT_FOUND));
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

    public ComTaskExecution lockComTaskExecutionOrThrowException(DeviceSchedulesInfo info) {
        Optional<Device> device = getLockedDevice(info.parent.id, info.parent.version);
        if (device.isPresent()) {
            return getLockedComTaskExecution(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentComTaskExecutionVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                .withActualVersion(() -> getCurrentComTaskExecutionVersion(info.id))
                .build();
    }

    public SecurityPropertySet findSecurityPropertySetOrThrowException(long id) {
        return deviceConfigurationService.findSecurityPropertySet(id)
                .orElseThrow(() -> new WebApplicationException("No SecurityPropertySet with id " + id, Response.Status.NOT_FOUND));
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
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentSecurityPropertySetVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                .withActualVersion(() -> getCurrentSecurityPropertySetVersion(info.id))
                .build();
    }

    public ConnectionTask findConnectionTaskOrThrowException(long id) {
        return connectionTaskService.findConnectionTask(id)
                .filter(candidate -> !candidate.isObsolete())
                .orElseThrow(() -> new WebApplicationException("No ConnectionTask with id " + id, Response.Status.NOT_FOUND));
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
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentConnectionTaskVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
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
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentDeviceMessageVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.messageSpecification.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
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
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, device.getmRID(), loadProfileId));
    }

    public Channel findChannelOnDeviceOrThrowException(String mRID, long channelId) {
        Device device = this.findDeviceByMrIdOrThrowException(mRID);
        return this.findChannelOnDeviceOrThrowException(device, channelId);
    }

    public Channel findChannelOnDeviceOrThrowException(Device device, long channelId) {
        return device.getLoadProfiles().stream()
                .flatMap(lp -> lp.getChannels().stream())
                .filter(c -> c.getId() == channelId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CHANNEL_ON_DEVICE, device.getmRID(), channelId));
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

    public DeviceProtocolPluggableClass findDeviceProtocolPluggableClassOrThrowException(long id) {
        return protocolPluggableService.findDeviceProtocolPluggableClass(id)
                .orElseThrow(() -> new WebApplicationException("No DeviceProtocolPluggableClass with id " + id, Response.Status.NOT_FOUND));
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
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentDeviceProtocolPluggableClassVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
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


    public EstimationRuleSet findEstimationRuleSetOrThrowException(long id) {
        return estimationService.getEstimationRuleSet(id)
                .filter(candidate -> candidate.getObsoleteDate() != null)
                .orElseThrow(() -> new WebApplicationException("No DeviceMessage with id " + id, Response.Status.NOT_FOUND));
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
                            .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                            .withActualVersion(() -> getCurrentEstimationRuleSetVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceVersion(info.parent.id), info.parent.version)
                .withActualVersion(() -> getCurrentEstimationRuleSetVersion(info.id))
                .build();
    }

    public Condition getQueryConditionForDevice(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if (params.getQueryParameters().size() > 0) {
            condition = condition.and(addDeviceQueryCondition(params));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(StandardParametersBean params) {
        Condition conditionDevice = Condition.TRUE;
        String mRID = params.getFirst("mRID");
        if (mRID != null) {
            conditionDevice = conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
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
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_METHOD, device.getmRID(), connectionMethodId));
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
        String mRID = filter.getString("mRID");
        if (mRID != null) {
            conditionDevice = conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
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
        List<CustomPropertySetInfo> customPropertySetInfos = new ArrayList<>();
        List<RegisteredCustomPropertySet> registeredCustomPropertySets = device.getDeviceType().getDeviceTypeCustomPropertySetUsage()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .collect(Collectors.toList());
        registeredCustomPropertySets.forEach(registeredCustomPropertySet -> customPropertySetInfos.add(
                new CustomPropertySetInfo(registeredCustomPropertySet, mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                        registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                        getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device)))
                )
        ));
        return customPropertySetInfos;
    }

    public void setDeviceCustomPropertySetInfo(Device device, long cpsId, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = device.getDeviceType().getDeviceTypeCustomPropertySetUsage().stream()
                .filter(f -> f.getId() == cpsId && f.isEditableByCurrentUser())
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId));
        customPropertySetService.setValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, getCustomPropertySetValues(info));
    }

    public CustomPropertySetInfo getRegisterCustomPropertySetInfo(Register register) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = register.getDevice().getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isViewableByCurrentUser()) {
            return new CustomPropertySetInfo(registeredCustomPropertySet.get(), mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                    registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                    getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), register.getRegisterSpec())))
            );
        } else {
            return null;
        }
    }

    public void setRegisterCustomPropertySet(Register register, CustomPropertySetInfo info) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = register.getDevice().getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isEditableByCurrentUser()) {
            customPropertySetService.setValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), register.getRegisterSpec(), getCustomPropertySetValues(info));
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, info.id);
        }
    }

    public CustomPropertySetInfo getChannelCustomPropertySetInfo(Channel channel) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = channel.getDevice().getDeviceType().getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isViewableByCurrentUser()) {
            return new CustomPropertySetInfo(registeredCustomPropertySet.get(), mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                    registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                    getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), channel.getChannelSpec())))
            );
        } else {
            return null;
        }
    }

    public void setChannelCustomPropertySet(Channel channel, CustomPropertySetInfo info) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = channel.getDevice().getDeviceType().getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isEditableByCurrentUser()) {
            customPropertySetService.setValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), channel.getChannelSpec(), getCustomPropertySetValues(info));
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

    private CustomPropertySetValues getCustomPropertySetValues(CustomPropertySetInfo info) {
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.empty();
        info.properties.forEach(property -> {
            if (property.getPropertyValueInfo() != null && property.getPropertyValueInfo().getValue() != null) {
                customPropertySetValues.setProperty(property.key, property.getPropertyValueInfo().getValue());
            } else {
                if (property.required) {
                    throw exceptionFactory.newException(MessageSeeds.NO_SUCH_REQUIRED_PROPERTY);
                }
            }
        });
        return customPropertySetValues;
    }
}