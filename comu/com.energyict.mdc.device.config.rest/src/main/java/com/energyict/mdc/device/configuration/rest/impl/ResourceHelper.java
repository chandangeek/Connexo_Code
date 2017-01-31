/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionBuilder;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.configuration.rest.EstimationRuleSetRefInfo;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResourceHelper {

    private final ExceptionFactory exceptionFactory;
    private final MasterDataService masterDataService;
    private final CustomPropertySetService customPropertySetService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final MeteringService meteringService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory,
                          MasterDataService masterDataService,
                          CustomPropertySetService customPropertySetService,
                          DeviceConfigurationService deviceConfigurationService,
                          DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          ValidationService validationService,
                          EstimationService estimationService, MeteringService meteringService) {
        super();
        this.exceptionFactory = exceptionFactory;
        this.masterDataService = masterDataService;
        this.customPropertySetService = customPropertySetService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.conflictFactory = conflictFactory;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.meteringService = meteringService;
    }

    public ChannelType findChannelTypeByIdOrThrowException(long id) {
        return masterDataService
                .findChannelTypeById(id)
                .orElseThrow(() -> new WebApplicationException("No channel type with id " + id, Response.Status.NOT_FOUND));
    }

    public Optional<DeviceLifeCycle> findDeviceLifeCycleById(long id) {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(id);
    }

    public DeviceLifeCycle findDeviceLifeCycleByIdOrThrowException(long id) {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_LIFE_CYCLE, id));
    }

    // ===============================================
    public DeviceType findDeviceTypeByIdOrThrowException(long id) {
        return deviceConfigurationService.findDeviceType(id)
                .orElseThrow(() -> new WebApplicationException("No device type with id " + id, Response.Status.NOT_FOUND));
    }

    public LoadProfileType findLoadProfileTypeByIdOrThrowException(long id) {
        return masterDataService
                .findLoadProfileType(id)
                .orElseThrow(() -> new WebApplicationException("Load profile with id " + id + Response.Status.NOT_FOUND));
    }

    public RegisterType findRegisterTypeByIdOrThrowException(long id) {
        return masterDataService
                .findRegisterType(id)
                .orElseThrow(() -> new WebApplicationException("No register type with id " + id, Response.Status.NOT_FOUND));
    }

    public RegisteredCustomPropertySet findDeviceTypeCustomPropertySetByIdOrThrowException(long id, Class domain) {
        return findAllCustomPropertySetsByDomain(domain)
                .stream()
                .filter(f -> f.getId() == id)
                .findAny()
                .orElseThrow(() -> new WebApplicationException("No custom property set with id " + id, Response.Status.NOT_FOUND));
    }

    public List<RegisteredCustomPropertySet> findAllCustomPropertySetsByDomain(Class domain) {
        return customPropertySetService.findActiveCustomPropertySets()
                .stream()
                .filter(f -> f.getCustomPropertySet().getDomainClass().getName().equals(domain.getName()))
                .collect(Collectors.toList());
    }

    public Long getCurrentDeviceTypeVersion(long id){
        return deviceConfigurationService.findDeviceType(id).map(DeviceType::getVersion).orElse(null);
    }

    public Optional<DeviceType> getLockedDeviceType(long id, long version){
        return deviceConfigurationService.findAndLockDeviceType(id, version);
    }

    public DeviceType lockDeviceTypeOrThrowException(DeviceTypeInfo info) {
        return lockDeviceTypeOrThrowException(info.id, info.version, info.name);
    }

    public DeviceType lockDeviceTypeOrThrowException(long id, long version, String name){
        return getLockedDeviceType(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> getCurrentDeviceTypeVersion(id))
                        .supplier());
    }

    public DeviceConfiguration findDeviceConfigurationByIdOrThrowException(long id) {
        return deviceConfigurationService.findDeviceConfiguration(id)
                .orElseThrow(() -> new WebApplicationException("No DeviceConfiguration with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentDeviceConfigurationVersion(long id){
        return deviceConfigurationService.findDeviceConfiguration(id).map(DeviceConfiguration::getVersion).orElse(null);
    }

    public Optional<DeviceConfiguration> getLockedDeviceConfiguration(long id, long version){
        return deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, version);
    }

    public DeviceConfiguration lockDeviceConfigurationOrThrowException(DeviceConfigurationInfo info) {
        return lockDeviceConfigurationOrThrowException(info, Function.identity());
    }

    public DeviceConfiguration lockDeviceConfigurationOrThrowException(DeviceConfigurationInfo info, Function<ConcurrentModificationExceptionBuilder, ConcurrentModificationExceptionBuilder> customMessageProvider) {
        Optional<DeviceType> deviceType = getLockedDeviceType(info.parent.id, info.parent.version);
        if (deviceType.isPresent()) {
            // if the deviceType lock succeeds, then we are 'sure' that the config is the same as well
            return deviceType.get().getConfigurations().stream()
                    .filter(deviceConfiguration -> (deviceConfiguration.getId() == info.id) && (deviceConfiguration.getVersion() == info.version)).findAny().orElseThrow(customMessageProvider.apply(conflictFactory.contextDependentConflictOn(info.name))
                    .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                    .withActualVersion(() -> getCurrentDeviceConfigurationVersion(info.id))
                    .supplier());
        }
        throw customMessageProvider.apply(conflictFactory.contextDependentConflictOn(info.name))
                .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentDeviceConfigurationVersion(info.id))
                .build();
    }

    public LogBookSpec findLogBookSpecByIdOrThrowException(long id) {
        return deviceConfigurationService.findLogBookSpec(id)
                .orElseThrow(() -> new WebApplicationException("No LogBookSpec with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentLogBookSpecVersion(long id){
        return deviceConfigurationService.findLogBookSpec(id).map(LogBookSpec::getVersion).orElse(null);
    }

    public Optional<LogBookSpec> getLockedLogBookSpec(long id, long version){
        return deviceConfigurationService.findAndLockLogBookSpecByIdAndVersion(id, version);
    }

    public LogBookSpec lockLogBookSpecOrThrowException(LogBookSpecInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedLogBookSpec(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentLogBookSpecVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentLogBookSpecVersion(info.id))
                .build();
    }

    public LogBookType lockDeviceTypeLogBookOrThrowException(LogBookTypeInfo info){
        Optional<DeviceType> deviceType = getLockedDeviceType(info.parent.id, info.parent.version);
        if (deviceType.isPresent()) {
            return masterDataService.findLogBookType(info.id)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> masterDataService.findLogBookType(info.id).map(LogBookType::getVersion).orElse(null))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> info.version)
                .build();
    }

    public DeviceConfigConflictMapping findDeviceConfigConflictMappingById(long id) {
        return deviceConfigurationService.findDeviceConfigConflictMapping(id)
                .orElseThrow(() -> new WebApplicationException("No device configuration conflict with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentDeviceConfigConflictMappingVersion(long id){
        return deviceConfigurationService.findDeviceConfigConflictMapping(id).map(DeviceConfigConflictMapping::getVersion).orElse(null);
    }

    public Optional<DeviceConfigConflictMapping> getLockedDeviceConfigConflictMapping(long id, long version){
        return deviceConfigurationService.findAndLockDeviceConfigConflictMappingByIdAndVersion(id, version);
    }

    public DeviceConfigConflictMapping lockDeviceConfigConflictMappingOrThrowException(DeviceConfigSolutionMappingInfo info) {
        Optional<DeviceType> deviceType = getLockedDeviceType(info.parent.id, info.parent.version);
        if (deviceType.isPresent()) {
            return getLockedDeviceConfigConflictMapping(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.fromConfiguration.name)
                            .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentDeviceConfigConflictMappingVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.fromConfiguration.name)
                .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentDeviceConfigConflictMappingVersion(info.id))
                .build();
    }

    public RegisterSpec findRegisterSpecByIdOrThrowException(long id) {
        return deviceConfigurationService.findRegisterSpec(id)
                .orElseThrow(() -> new WebApplicationException("No RegisterSpec with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentRegisterSpecVersion(long id){
        return deviceConfigurationService.findRegisterSpec(id).map(RegisterSpec::getVersion).orElse(null);
    }

    public Optional<RegisterSpec> getLockedRegisterSpec(long id, long version){
        return deviceConfigurationService.findAndLockRegisterSpecByIdAndVersion(id, version);
    }

    public RegisterSpec lockRegisterSpecOrThrowException(RegisterConfigInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedRegisterSpec(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentRegisterSpecVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentRegisterSpecVersion(info.id))
                .build();
    }

    public RegisterType lockDeviceTypeRegisterTypeOrThrowException(RegisterTypeInfo info){
        Optional<DeviceType> deviceType = getLockedDeviceType(info.parent.id, info.parent.version);
        if (deviceType.isPresent()) {
            return masterDataService.findRegisterType(info.id)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.readingType.aliasName)
                            .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> masterDataService.findRegisterType(info.id).map(RegisterType::getVersion).orElse(null))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.readingType.aliasName)
                .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> info.version)
                .build();
    }

    public ProtocolDialectConfigurationProperties findProtocolDialectConfigurationPropertiesByIdOrThrowException(long id) {
        return deviceConfigurationService.getProtocolDialectConfigurationProperties(id)
                .orElseThrow(() -> new WebApplicationException("No ProtocolDialectConfigurationProperties with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentProtocolDialectConfigurationPropertiesVersion(long id){
        return deviceConfigurationService.getProtocolDialectConfigurationProperties(id).map(ProtocolDialectConfigurationProperties::getVersion).orElse(null);
    }

    public Optional<ProtocolDialectConfigurationProperties> getLockedProtocolDialectConfigurationProperties(long id, long version){
        return deviceConfigurationService.findAndLockProtocolDialectConfigurationPropertiesByIdAndVersion(id, version);
    }

    public ProtocolDialectConfigurationProperties lockProtocolDialectConfigurationPropertiesOrThrowException(ProtocolDialectInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedProtocolDialectConfigurationProperties(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentProtocolDialectConfigurationPropertiesVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentProtocolDialectConfigurationPropertiesVersion(info.id))
                .build();
    }

    public PartialConnectionTask findPartialConnectionTaskByIdOrThrowException(long id) {
        return deviceConfigurationService.findPartialConnectionTask(id)
                .orElseThrow(() -> new WebApplicationException("No PartialConnectionTask with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentPartialConnectionTaskVersion(long id){
        return deviceConfigurationService.findPartialConnectionTask(id).map(PartialConnectionTask::getVersion).orElse(null);
    }

    public Optional<PartialConnectionTask> getLockedPartialConnectionTask(long id, long version){
        return deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, version);
    }

    public PartialConnectionTask lockPartialConnectionTaskOrThrowException(ConnectionMethodInfo<?> info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedPartialConnectionTask(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentPartialConnectionTaskVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentPartialConnectionTaskVersion(info.id))
                .build();
    }

    public RegisterGroup findRegisterGroupByIdOrThrowException(long id) {
        return masterDataService
                .findRegisterGroup(id)
                .orElseThrow(() -> new WebApplicationException("No register group with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentRegisterGroupVersion(long id){
        return masterDataService.findRegisterGroup(id).map(RegisterGroup::getVersion).orElse(null);
    }

    public Optional<RegisterGroup> getLockedRegisterGroup(long id, long version){
        return masterDataService.findAndLockRegisterGroupByIdAndVersion(id, version);
    }

    public RegisterGroup lockRegisterGroupOrThrowException(RegisterGroupInfo info) {
        return getLockedRegisterGroup(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentRegisterGroupVersion(info.id))
                        .supplier());
    }

    public SecurityPropertySet findSecurityPropertySetByIdOrThrowException(long securityPropertySetId) {
        return deviceConfigurationService.findSecurityPropertySet(securityPropertySetId)
                .orElseThrow(() -> new WebApplicationException("Required security set is missing", Response.Status.NOT_FOUND));
    }

    public Long getCurrentSecurityPropertySetVersion(long id){
        return deviceConfigurationService.findSecurityPropertySet(id).map(SecurityPropertySet::getVersion).orElse(null);
    }

    public Optional<SecurityPropertySet> getLockedSecurityPropertySet(long id, long version){
        return deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(id, version);
    }

    public SecurityPropertySet lockSecurityPropertySetOrThrowException(SecurityPropertySetInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedSecurityPropertySet(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentSecurityPropertySetVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentSecurityPropertySetVersion(info.id))
                .build();
    }

    public ComTaskEnablement findComTaskEnablementByIdOrThrowException(long id) {
        return deviceConfigurationService.findComTaskEnablement(id)
                .orElseThrow(() -> new WebApplicationException("Required ComTaskEnablement is missing", Response.Status.NOT_FOUND));
    }

    public Long getCurrentComTaskEnablementVersion(long id){
        return deviceConfigurationService.findComTaskEnablement(id).map(ComTaskEnablement::getVersion).orElse(null);
    }

    public Optional<ComTaskEnablement> getLockedComTaskEnablement(long id, long version){
        return deviceConfigurationService.findAndLockComTaskEnablementByIdAndVersion(id, version);
    }

    public ComTaskEnablement lockComTaskEnablementOrThrowException(ComTaskEnablementInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedComTaskEnablement(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.comTask.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentComTaskEnablementVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.comTask.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentComTaskEnablementVersion(info.id))
                .build();
    }

    public LoadProfileType findLoadProfileTypeByIdOrThrowException(Long id) {
        return masterDataService.findLoadProfileType(id)
                .orElseThrow(() -> new WebApplicationException("No load profile type with id " + id, Response.Status.NOT_FOUND));
    }

    public LoadProfileType lockDeviceTypeLoadProfileTypeOrThrowException(LoadProfileTypeOnDeviceTypeInfo info){
        Optional<DeviceType> deviceType = getLockedDeviceType(info.parent.id, info.parent.version);
        if (deviceType.isPresent()) {
            return masterDataService.findLoadProfileType(info.id)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> masterDataService.findLoadProfileType(info.id).map(LoadProfileType::getVersion).orElse(null))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceTypeVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> info.version)
                .build();
    }

    public LoadProfileSpec findLoadProfileSpecOrThrowException(long id) {
        return deviceConfigurationService.findLoadProfileSpec(id)
                .orElseThrow(() -> new WebApplicationException("No load profile spec with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentLoadProfileSpecVersion(long id){
        return deviceConfigurationService.findLoadProfileSpec(id).map(LoadProfileSpec::getVersion).orElse(null);
    }

    public Optional<LoadProfileSpec> getLockedLoadProfileSpec(long id, long version){
        return deviceConfigurationService.findAndLockLoadProfileSpecByIdAndVersion(id, version);
    }

    public LoadProfileSpec lockLoadProfileSpecOrThrowException(LoadProfileSpecInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedLoadProfileSpec(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentLoadProfileSpecVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentLoadProfileSpecVersion(info.id))
                .build();
    }

    public ChannelSpec findChannelSpecOrThrowException(long id) {
        return deviceConfigurationService.findChannelSpec(id)
                .orElseThrow(() -> new WebApplicationException("No channel spec with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentChannelSpecVersion(long id){
        return deviceConfigurationService.findChannelSpec(id).map(ChannelSpec::getVersion).orElse(null);
    }

    public Optional<ChannelSpec> getLockedChannelSpec(long id, long version){
        return deviceConfigurationService.findAndLockChannelSpecByIdAndVersion(id, version);
    }

    public ChannelSpec lockChannelSpecOrThrowException(ChannelSpecFullInfo info) {
        Optional<LoadProfileSpec> lockedLoadProfileSpec = getLockedLoadProfileSpec(info.parent.id, info.parent.version);
        if (lockedLoadProfileSpec.isPresent()) {
            return getLockedChannelSpec(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentLoadProfileSpecVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentChannelSpecVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentLoadProfileSpecVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentChannelSpecVersion(info.id))
                .build();
    }

    public ValidationRuleSet findValidationRuleSetOrThrowException(long id) {
        return validationService.getValidationRuleSet(id)
                .filter(candidate -> candidate.getObsoleteDate() == null)
                .orElseThrow(() -> new WebApplicationException("No ValidationRuleSet with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentValidationRuleSetVersion(long id){
        return  validationService.getValidationRuleSet(id)
                .filter(candidate -> candidate.getObsoleteDate() == null)
                .map(ValidationRuleSet::getVersion)
                .orElse(null);
    }

    public Optional<? extends ValidationRuleSet> getLockedValidationRuleSet(long id, long version){
        return validationService.findAndLockValidationRuleSetByIdAndVersion(id, version);
    }

    public ValidationRuleSet lockValidationRuleSetOrThrowException(ValidationRuleSetInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedValidationRuleSet(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentValidationRuleSetVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentValidationRuleSetVersion(info.id))
                .build();
    }

    public EstimationRuleSet findEstimationRuleSetOrThrowException(long id) {
        return estimationService.getEstimationRuleSet(id)
                .filter(candidate -> candidate.getObsoleteDate() == null)
                .orElseThrow(() -> new WebApplicationException("No EstimationRuleSet with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentEstimationRuleSetVersion(long id){
        return estimationService.getEstimationRuleSet(id)
                .filter(candidate -> candidate.getObsoleteDate() == null)
                .map(EstimationRuleSet::getVersion)
                .orElse(null);
    }

    public Optional<? extends EstimationRuleSet> getLockedEstimationRuleSet(long id, long version){
        return estimationService.findAndLockEstimationRuleSet(id, version);
    }

    public EstimationRuleSet lockEstimationRuleSetOrThrowException(EstimationRuleSetRefInfo info) {
        Optional<DeviceConfiguration> deviceConfiguration = getLockedDeviceConfiguration(info.parent.id, info.parent.version);
        if (deviceConfiguration.isPresent()) {
            return getLockedEstimationRuleSet(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentEstimationRuleSetVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceConfigurationVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentEstimationRuleSetVersion(info.id))
                .build();
    }

    public Optional<ReadingType> findReadingType(String mRID) {
        return meteringService.getReadingType(mRID);
    }

    public long findTimeOfUseOptionsId(DeviceType deviceType) {
        Optional<TimeOfUseOptions> timeOfUseOptions = deviceConfigurationService.findTimeOfUseOptions(deviceType);
        if (timeOfUseOptions.isPresent()) {
            return timeOfUseOptions.get().getVersion();
        } else {
            return 0;
        }
    }

    public Optional<TimeOfUseOptions> findAndLockTimeOfUseOptionsByIdAndVersion(DeviceType deviceType, long version) {
        return deviceConfigurationService.findAndLockTimeOfUseOptionsByIdAndVersion(deviceType, version);
    }
}