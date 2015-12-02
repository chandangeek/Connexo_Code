package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflictType;
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

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final Clock clock;

    @Inject
    public ResourceHelper(DeviceService deviceService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, DeviceConfigurationService deviceConfigurationService, LoadProfileService loadProfileService, CommunicationTaskService communicationTaskService, MeteringGroupsService meteringGroupsService, ConnectionTaskService connectionTaskService, DeviceMessageService deviceMessageService, ProtocolPluggableService protocolPluggableService, DataCollectionKpiService dataCollectionKpiService, EstimationService estimationService, MdcPropertyUtils mdcPropertyUtils, CustomPropertySetService customPropertySetService, Clock clock) {
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
        this.clock = clock;
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

    public Device lockDeviceOrThrowException(long deviceId, String mRID, long deviceVersion) {
        return deviceService.findAndLockDeviceByIdAndVersion(deviceId, deviceVersion)
                .orElseThrow(conflictFactory.contextDependentConflictOn(mRID)
                        .withActualVersion(() -> getCurrentDeviceVersion(mRID))
                        .supplier());
    }

    public void lockChannelSpecOrThrowException(long channelSpecId,  long channelSpecVersion, Channel channel) {
        deviceConfigurationService.findAndLockChannelSpecByIdAndVersion(channelSpecId, channelSpecVersion)
                .orElseThrow(conflictFactory.contextDependentConflictOn("Channel")
                        .withActualVersion(() -> channel.getChannelSpec().getVersion())
                        .supplier());
    }

    public void lockRegisterSpecOrThrowException(long registerSpecId,  long registerSpecVersion, Register register) {
        deviceConfigurationService.findAndLockRegisterSpecByIdAndVersion(registerSpecId, registerSpecVersion)
                .orElseThrow(conflictFactory.contextDependentConflictOn("Register")
                        .withActualVersion(() -> register.getRegisterSpec().getVersion())
                        .supplier());
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
                !registeredCustomPropertySet.getCustomPropertySet().isVersioned() ?
                        new CustomPropertySetInfo(registeredCustomPropertySet, mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device))
                        ),
                                device.getId(), device.getVersion()
                        ) :
                        new CustomPropertySetInfo(registeredCustomPropertySet, mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, this.clock.instant()))
                        ),
                                device.getId(), device.getVersion(),
                                customPropertySetService.getValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, this.clock.instant()).getEffectiveRange()
                        )
        ));
        return customPropertySetInfos;
    }

    public List<CustomPropertySetInfo> getDeviceCustomPropertySetInfos(Device device, Instant instant) {
        List<CustomPropertySetInfo> customPropertySetInfos = new ArrayList<>();
        List<RegisteredCustomPropertySet> registeredCustomPropertySets = device.getDeviceType().getDeviceTypeCustomPropertySetUsage()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(cps -> cps.getCustomPropertySet().isVersioned())
                .collect(Collectors.toList());
        registeredCustomPropertySets.forEach(registeredCustomPropertySet -> customPropertySetInfos.add(
                new CustomPropertySetInfo(registeredCustomPropertySet, mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                        registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                        getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, instant))
                ),
                        device.getId(), device.getVersion(),
                        customPropertySetService.getValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, instant).getEffectiveRange()
                )
        ));
        return customPropertySetInfos;
    }

    public List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(Device device, long cpsId) {
        return getVersionedCustomPropertySetHistoryInfos(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), cpsId);
    }

    public List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(Channel channel, long cpsId) {
        return getVersionedCustomPropertySetHistoryInfos(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec().getVersion(), cpsId);
    }

    public List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(Register register, long cpsId) {
        return getVersionedCustomPropertySetHistoryInfos(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec().getVersion(), cpsId);
    }

    public <D> List<CustomPropertySetInfo> getVersionedCustomPropertySetHistoryInfos(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, long cpsId) {
        return Stream.of(registeredCustomPropertySet)
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(cps -> cps.getCustomPropertySet().isVersioned())
                .filter(cps -> cps.getId() == cpsId)
                .flatMap(cps -> getHistoryInfo(cps, businessObject, businessObjectId, businessObjectVersion))
                .collect(Collectors.toList());
    }

    private <D> Stream<CustomPropertySetInfo> getHistoryInfo(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion) {
        List<CustomPropertySetValues> values = customPropertySetService.getAllVersionedValuesFor(registeredCustomPropertySet.getCustomPropertySet(), businessObject);
        return values.stream()
                .map(v -> new CustomPropertySetInfo(
                        registeredCustomPropertySet,
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(v)),
                        businessObjectId,
                        businessObjectVersion,
                        v.getEffectiveRange()));
    }

    public void addDeviceCustomPropertySetVersioned(Device device, long cpsId, CustomPropertySetInfo info) {
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(device, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), device, getCustomPropertySetValues(info), newRange);
        device.save();
    }

    public void setDeviceCustomPropertySetInfo(Device device, long cpsId, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(device, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        customPropertySetService.setValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, getCustomPropertySetValues(info));
        device.save();
    }

    public void setDeviceCustomPropertySetVersioned(Device device, long cpsId, CustomPropertySetInfo info, Instant effectiveTimestamp) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(device,cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), device, getCustomPropertySetValues(info), newRange, effectiveTimestamp);
        device.save();
    }

    public void addChannelCustomPropertySetVersioned(Channel channel, long cpsId, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(channel, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_CHANNEL, cpsId, channel.getId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), channel.getChannelSpec(), getCustomPropertySetValues(info), newRange);
        channel.getChannelSpec().save();
    }

    public void setChannelCustomPropertySetVersioned(Channel channel, long cpsId, CustomPropertySetInfo info, Instant effectiveTimestamp) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(channel, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_CHANNEL, cpsId, channel.getId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), channel.getChannelSpec(), getCustomPropertySetValues(info), newRange, effectiveTimestamp);
        channel.getChannelSpec().save();
    }

    public void addRegisterCustomPropertySetVersioned(Register register, long cpsId, CustomPropertySetInfo info) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(register, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_REGISTER, cpsId, register.getRegisterSpecId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), register.getRegisterSpec(), getCustomPropertySetValues(info), newRange);
        register.getRegisterSpec().save();
    }

    public void setRegisterCustomPropertySetVersioned(Register register, long cpsId, CustomPropertySetInfo info, Instant effectiveTimestamp) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(register, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET_FOR_REGISTER, cpsId, register.getRegisterSpecId());
        }
        Range<Instant> newRange = getTimeRange(info.startTime, info.endTime);
        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(), register.getRegisterSpec(), getCustomPropertySetValues(info), newRange, effectiveTimestamp);
        register.getRegisterSpec().save();
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenUpdate(Device device, long cpsId, long startTime, long endTime, Instant effectiveTimestamp) {
        Range<Instant> range = getTimeRange(startTime, endTime);
        return getOverlaps(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), cpsId, range, effectiveTimestamp);
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenCreate(Device device, long cpsId, long startTime, long endTime) {
        Range<Instant> range = getTimeRange(startTime, endTime);
        return getOverlaps(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), cpsId, range);
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenUpdate(Channel channel, long cpsId, long startTime, long endTime, Instant effectiveTimestamp) {
        Range<Instant> range = getTimeRange(startTime, endTime);
        return getOverlaps(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec().getVersion(), cpsId, range, effectiveTimestamp);
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenCreate(Channel channel, long cpsId, long startTime, long endTime) {
        Range<Instant> range = getTimeRange(startTime, endTime);
        return getOverlaps(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec().getVersion(), cpsId, range);
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenUpdate(Register register, long cpsId, long startTime, long endTime, Instant effectiveTimestamp) {
        Range<Instant> range = getTimeRange(startTime, endTime);
        return getOverlaps(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec().getVersion(), cpsId, range, effectiveTimestamp);
    }

    public List<CustomPropertySetIntervalConflictInfo> getOverlapsWhenCreate(Register register, long cpsId, long startTime, long endTime) {
        Range<Instant> range = getTimeRange(startTime, endTime);
        return getOverlaps(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec().getVersion(), cpsId, range);
    }

    public <D> List<CustomPropertySetIntervalConflictInfo> getOverlaps(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, long cpsId, Range<Instant> newRange) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(registeredCustomPropertySet.getCustomPropertySet(), businessObject);
        return overlapCalculatorBuilder.whenCreating(newRange).stream().map(e -> new CustomPropertySetIntervalConflictInfo(
                new CustomPropertySetInfo(
                        registeredCustomPropertySet,
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(e.getValues())),
                        businessObjectId,
                        businessObjectVersion,
                        e.getValues().getEffectiveRange()),
                e)).collect(Collectors.toList());
    }

    public <D> List<CustomPropertySetIntervalConflictInfo> getOverlaps(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, long cpsId, Range<Instant> newRange, Instant effectiveTimestamp) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(registeredCustomPropertySet.getCustomPropertySet(), businessObject);
        return overlapCalculatorBuilder.whenUpdating(effectiveTimestamp, newRange).stream().map(e -> new CustomPropertySetIntervalConflictInfo(
                new CustomPropertySetInfo(
                        registeredCustomPropertySet,
                        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                                registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs(),
                                getCustomProperties(e.getValues())),
                        businessObjectId,
                        businessObjectVersion,
                        e.getValues().getEffectiveRange()),
                e)).collect(Collectors.toList());
    }

    public CustomPropertySetInfo getRegisterCustomPropertySetInfo(Register register, Instant effectiveTimestamp) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = register.getDevice().getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isViewableByCurrentUser()) {
            return !registeredCustomPropertySet.get().getCustomPropertySet().isVersioned() ?
                    new CustomPropertySetInfo(registeredCustomPropertySet.get(), mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                            registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),

                            getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), register.getRegisterSpec()))),
                            register.getRegisterSpec().getId(), register.getRegisterSpec().getVersion()) :
                    new CustomPropertySetInfo(registeredCustomPropertySet.get(), mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                            registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                            getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), register.getRegisterSpec(), effectiveTimestamp))),
                            register.getRegisterSpec().getId(), register.getRegisterSpec().getVersion(),
                            customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), register.getRegisterSpec(), effectiveTimestamp).getEffectiveRange());
        } else {
            return null;
        }
    }

    public void setRegisterCustomPropertySet(Register register, CustomPropertySetInfo info) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = register.getDevice().getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isEditableByCurrentUser()) {
            customPropertySetService.setValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), register.getRegisterSpec(), getCustomPropertySetValues(info));
            register.getRegisterSpec().save();
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, info.id);
        }
    }

    public CustomPropertySetInfo getChannelCustomPropertySetInfo(Channel channel, Instant effectiveTimestamp) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = channel.getDevice().getDeviceType().getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isViewableByCurrentUser()) {
            return !registeredCustomPropertySet.get().getCustomPropertySet().isVersioned() ?
                    new CustomPropertySetInfo(registeredCustomPropertySet.get(), mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                            registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),

                            getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), channel.getChannelSpec()))),
                            channel.getChannelSpec().getId(), channel.getChannelSpec().getVersion()) :
                    new CustomPropertySetInfo(registeredCustomPropertySet.get(), mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                            registeredCustomPropertySet.get().getCustomPropertySet().getPropertySpecs(),
                            getCustomProperties(customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), channel.getChannelSpec(), effectiveTimestamp))),
                            channel.getChannelSpec().getId(), channel.getChannelSpec().getVersion(),
                            customPropertySetService.getValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), channel.getChannelSpec(), effectiveTimestamp).getEffectiveRange());
        } else {
            return null;
        }
    }

    public void setChannelCustomPropertySet(Channel channel, CustomPropertySetInfo info) {
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = channel.getDevice().getDeviceType().getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType());
        if (registeredCustomPropertySet.isPresent() && registeredCustomPropertySet.get().isEditableByCurrentUser()) {
            customPropertySetService.setValuesFor(registeredCustomPropertySet.get().getCustomPropertySet(), channel.getChannelSpec(), getCustomPropertySetValues(info));
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

    public Range<Instant> getCurrentTimeInterval(Device device, long cpsId) {
        return getCurrentTimeInterval(getRegisteredCustomPropertySet(device, cpsId), device, device.getId(), device.getVersion(), cpsId);
    }

    public Range<Instant> getCurrentTimeInterval(Channel channel, long cpsId) {
        return getCurrentTimeInterval(getRegisteredCustomPropertySet(channel, cpsId), channel.getChannelSpec(), channel.getChannelSpec().getId(), channel.getChannelSpec().getVersion(), cpsId);
    }

    public Range<Instant> getCurrentTimeInterval(Register register, long cpsId) {
        return getCurrentTimeInterval(getRegisteredCustomPropertySet(register, cpsId), register.getRegisterSpec(), register.getRegisterSpec().getId(), register.getRegisterSpec().getVersion(), cpsId);
    }

    public <D> Range<Instant> getCurrentTimeInterval(RegisteredCustomPropertySet registeredCustomPropertySet, D businessObject, long businessObjectId, long businessObjectVersion, long cpsId) {
        List<CustomPropertySetInfo> customPropertySetInfo = this.getVersionedCustomPropertySetHistoryInfos(registeredCustomPropertySet, businessObject, businessObjectId, businessObjectVersion, cpsId)
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
        return device.getDeviceType().getDeviceTypeCustomPropertySetUsage().stream()
                .filter(cps -> cps.getId()==cpsId && cps.isViewableByCurrentUser())
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
        }catch (IllegalArgumentException e){
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
}