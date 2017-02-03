/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.calendars.ProtocolSupportedCalendarOptions;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides services that relate to {@link DeviceType}s and {@link DeviceConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:34)
 */
@ProviderType
public interface DeviceConfigurationService {

    String COMPONENTNAME = "DTC";
    int MAX_DEVICE_MESSAGE_FILE_SIZE_MB = 2;
    int MAX_DEVICE_MESSAGE_FILE_SIZE_BYTES = MAX_DEVICE_MESSAGE_FILE_SIZE_MB * 1024 * 1024;    // 2MB

    Finder<DeviceType> findAllDeviceTypes();

    /**
     * Creates a new {@link DeviceType} with the specified name
     * that uses the specified {@link DeviceProtocolPluggableClass device protocol}
     * to communicate with the actual device.
     * The {@link com.elster.jupiter.fsm.State} of the devices
     * of this type are managed by the default {@link DeviceLifeCycle}
     * that was installed by this bundle.
     * Note however that if a user deleted that default DeviceLifeCycle
     * the creation will fail because the DeviceLifeCycle is required.
     * This deviceType will not be usable for a datalogger slave
     *
     * @param name The name of the new DeviceType
     * @param deviceProtocolPluggableClass The DeviceProtocolPluggableClass
     * @return The newly persisted DeviceType
     */
    DeviceType newDeviceType(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    /**
     * Creates a new {@link DeviceType} with the specified name
     * that uses the specified {@link DeviceProtocolPluggableClass device protocol}
     * to communicate with the actual device.
     * The {@link com.elster.jupiter.fsm.State} of the devices
     * of this type are managed by the provided {@link DeviceLifeCycle}.
     * This deviceType will not be usable for a datalogger slave
     *
     * @param name The name of the new DeviceType
     * @param deviceProtocolPluggableClass The DeviceProtocolPluggableClass
     * @param deviceLifeCycle The DeviceLifeCycle
     * @return The newly persisted DeviceType
     */
    DeviceType newDeviceType(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceLifeCycle deviceLifeCycle);

    /**
     * Creates a new {@link com.energyict.mdc.device.config.DeviceType.DeviceTypeBuilder} with the specified name
     * that uses the specified {@link DeviceProtocolPluggableClass device protocol}
     * to communicate with the actual device.
     * The {@link com.elster.jupiter.fsm.State} of the devices
     * of this type are managed by the provided {@link DeviceLifeCycle}.
     * This deviceType will not be usable for a datalogger slave
     *
     * @param name The name of the new DeviceType
     * @param deviceProtocolPluggableClass The DeviceProtocolPluggableClass
     * @param deviceLifeCycle The DeviceLifeCycle
     * @return The newly created DeviceType
     */
    DeviceType.DeviceTypeBuilder newDeviceTypeBuilder(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceLifeCycle deviceLifeCycle);

    /**
     * Creates a new datalogger slave {@link com.energyict.mdc.device.config.DeviceType.DeviceTypeBuilder} with the specified name.
     * The {@link com.elster.jupiter.fsm.State} of the devices
     * of this type are managed by the provided {@link DeviceLifeCycle}.
     * Devices of this deviceType will not be able to define communication related items.
     *
     * @param name The name of the new DeviceType
     * @param deviceLifeCycle The DeviceLifeCycle
     * @return The newly created datalogger slave DeviceType
     */
    DeviceType.DeviceTypeBuilder newDataloggerSlaveDeviceTypeBuilder(String name, DeviceLifeCycle deviceLifeCycle);

    /**
     * Find the {@link DeviceType} which is uniquely identified by the provided ID.
     *
     * @param deviceTypeId the ID of the DeviceType
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    Optional<DeviceType> findDeviceType(long deviceTypeId);

    /**
     * Finds and locks a {@link DeviceType} which is uniquely identified by the given ID and with the given VERSION.
     *
     * @param id the id of the DeviceType
     * @param version the version of the DeviceType
     * @return the DeviceType or empty if either the DeviceType does not exist
     * or the version of the DeviceType is not equal to the specified version
     */
    Optional<DeviceType> findAndLockDeviceType(long id, long version);

    /**
     * Find the {@link DeviceType} with the specified name.
     *
     * @param name The name
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    Optional<DeviceType> findDeviceTypeByName(String name);

    /**
     * Returns the topic onto which {@link DeviceLifeCycleChangeEvent}s are published.
     *
     * @return The topic
     */
    String changeDeviceLifeCycleTopicName();

    /**
     * Changes the {@link DeviceLifeCycle} of the specified {@link DeviceType}
     * to the specified DeviceLifeCycle, making sure that the switch will
     * not cause any problems on the existing devices of this type.
     * Note that this may throw an {@link IncompatibleDeviceLifeCycleChangeException}.
     *
     * @param deviceType The DeviceType
     * @param deviceLifeCycle The new DeviceLifeCycle
     */
    void changeDeviceLifeCycle(DeviceType deviceType, DeviceLifeCycle deviceLifeCycle) throws
            IncompatibleDeviceLifeCycleChangeException;

    /**
     * Finds a {@link DeviceConfiguration} which is uniquely identified by the given ID.
     *
     * @param id the id of the DeviceConfiguration
     * @return the DeviceConfiguration or <code>null</code> if there is no such DeviceConfiguration
     */
    Optional<DeviceConfiguration> findDeviceConfiguration(long id);

    /**
     * Finds and locks a {@link DeviceConfiguration} which is uniquely identified by the given ID and with the given VERSION.
     *
     * @param id the id of the DeviceConfiguration
     * @param version the version of the DeviceConfiguration
     * @return the DeviceConfiguration or empty if either the DeviceConfiguration does not exist
     * or the version of the DeviceConfiguration is not equal to the specified version
     */
    Optional<DeviceConfiguration> findAndLockDeviceConfigurationByIdAndVersion(long id, long version);

    Optional<DeviceConfigConflictMapping> findAndLockDeviceConfigConflictMappingByIdAndVersion(long id, long version);

    /**
     * Finds a {@link ChannelSpec} which is uniquely identified by the given ID.
     *
     * @param channelSpecId the id of the ChannelSpec
     * @return the ChannelSpec or <code>null</code> if there is no such ChannelSpec
     */
    Optional<ChannelSpec> findChannelSpec(long channelSpecId);

    Optional<ChannelSpec> findAndLockChannelSpecByIdAndVersion(long id, long version);

    /**
     * Finds a {@link RegisterSpec} which is uniquely identified by the given ID.
     *
     * @param id the id of the RegisterSpec
     * @return the RegisterSpec or <code>null</code> if there is no such RegisterSpec
     */
    Optional<RegisterSpec> findRegisterSpec(long id);

    Optional<RegisterSpec> findAndLockRegisterSpecByIdAndVersion(long id, long version);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs}
     * that are owned by the given {@link DeviceType}
     * and modelled by the given RegisterType where the
     * register spec if owned by an active device configuration.
     *
     * @param deviceType the DeviceType
     * @param registerType the list of RegisterType
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    List<RegisterSpec> findActiveRegisterSpecsByDeviceTypeAndRegisterType(DeviceType deviceType, RegisterType registerType);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs}
     * that are owned by the given {@link DeviceType}
     * and modelled by the given RegisterType where the
     * register spec if owned by an inactive device configuration.
     *
     * @param deviceType the DeviceType
     * @param registerType the list of RegisterType
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    List<RegisterSpec> findInactiveRegisterSpecsByDeviceTypeAndRegisterType(DeviceType deviceType, RegisterType registerType);

    /**
     * Finds a list of {@link RegisterSpec}s which are modeled by the given RegisterType.
     *
     * @param measurementType the MeasurementType
     * @return the list of RegisterSpecs
     */
    List<RegisterSpec> findRegisterSpecsByMeasurementType(MeasurementType measurementType);

    /**
     * Find the {@link LoadProfileSpec} with the given ID.
     *
     * @param loadProfileSpecId the ID of the LoadProfileSpec
     * @return the LoadProfileSpec or <code>null</code> if there is no such LoadProfileSpec
     */
    Optional<LoadProfileSpec> findLoadProfileSpec(long loadProfileSpecId);

    Optional<LoadProfileSpec> findAndLockLoadProfileSpecByIdAndVersion(long id, long version);

    /**
     * Find a {@link LoadProfileSpec} which is modeled by the given {@link LoadProfileType} for the given
     * {@link DeviceConfiguration}.
     *
     * @param deviceConfig the DeviceConfiguration
     * @param loadProfileType the LoadProfileType which models the LoadProfileSpec
     * @return the requested LoadProfileSpec
     */
    Optional<LoadProfileSpec> findLoadProfileSpecByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType);

    List<LoadProfileSpec> findLoadProfileSpecsByLoadProfileType(LoadProfileType loadProfileType);

    /**
     * Find a {@link LogBookSpec} with the given ID.
     *
     * @param id the ID of the LogBookSpec
     * @return the LogBookSpec or <code>null</code> if there is no such LogBookSpec
     */
    Optional<LogBookSpec> findLogBookSpec(long id);

    Optional<LogBookSpec> findAndLockLogBookSpecByIdAndVersion(long id, long version);

    Optional<ChannelSpec> findChannelSpecForLoadProfileSpecAndChannelType(LoadProfileSpec loadProfileSpec, ChannelType channelType);

    List<DeviceConfiguration> findDeviceConfigurationsUsingLoadProfileType(LoadProfileType loadProfileType);

    List<ChannelSpec> findChannelSpecsForMeasurementType(MeasurementType measurementType);

    List<ChannelSpec> findChannelSpecsForChannelTypeInLoadProfileType(ChannelType channelType, LoadProfileType loadProfileType);

    List<DeviceType> findDeviceTypesUsingLoadProfileType(LoadProfileType loadProfileType);

    List<DeviceType> findDeviceTypesUsingLogBookType(LogBookType logBookType);

    List<DeviceType> findDeviceTypesUsingRegisterType(MeasurementType measurementType);

    List<DeviceType> findDeviceTypesUsingDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle);

    List<DeviceConfiguration> findDeviceConfigurationsUsingLogBookType(LogBookType logBookType);

    List<DeviceConfiguration> findDeviceConfigurationsUsingMeasurementType(MeasurementType measurementType);

    boolean isRegisterTypeUsedByDeviceType(RegisterType registerType);

    List<DeviceType> findDeviceTypesWithDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    Finder<DeviceConfiguration> findDeviceConfigurationsUsingDeviceType(DeviceType deviceType);

    Optional<PartialConnectionTask> findPartialConnectionTask(long id);

    Optional<PartialConnectionTask> findAndLockPartialConnectionTaskByIdAndVersion(long id, long version);

    List<PartialConnectionTask> findByConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(long id);

    Optional<ProtocolDialectConfigurationProperties> findAndLockProtocolDialectConfigurationPropertiesByIdAndVersion(long id, long version);

    List<PartialConnectionTask> findByComPortPool(ComPortPool comPortPool);

    List<AllowedCalendar> findAllowedCalendars(String name);

    Optional<SecurityPropertySet> findSecurityPropertySet(long id);

    Optional<SecurityPropertySet> findAndLockSecurityPropertySetByIdAndVersion(long id, long version);

    Optional<ComTaskEnablement> findComTaskEnablement(long id);

    Optional<ComTaskEnablement> findAndLockComTaskEnablementByIdAndVersion(long id, long version);

    /**
     * Return a list of ComTasks that are legal for assignment to the ComSchedule.
     * A ComTask can be assigned to the comSchedule IF all devices
     * already linked to the schedule are enabled for the ComTask.
     * This list will include ComTasks already linked to the ComSchedule!
     *
     * @param comSchedule The ComSchedule
     * @return List of ComTasks, including ComTasks already linked to the schedule.
     */
    List<ComTask> findAvailableComTasks(ComSchedule comSchedule);

    /**
     * Finds all currently <i>active</i> DeviceConfigurations for the given DeviceType.
     *
     * @param deviceType the DeviceType
     * @return the list of <i>active</i> DeviceConfigurations
     */
    Finder<DeviceConfiguration> findActiveDeviceConfigurationsForDeviceType(DeviceType deviceType);

    List<DeviceConfiguration> findDeviceConfigurationsForValidationRuleSet(long validationRuleSetId);

    List<DeviceType> findDeviceTypesForCalendar(Calendar calendar);

    List<ReadingType> getReadingTypesRelatedToConfiguration(DeviceConfiguration configuration);

    List<DeviceConfiguration> getLinkableDeviceConfigurations(ValidationRuleSet validationRuleSet);

    List<DeviceConfiguration> getLinkableDeviceConfigurations(EstimationRuleSet estimationRuleSet);

    List<SecurityPropertySet> findUniqueSecurityPropertySets();

    Finder<DeviceConfiguration> findDeviceConfigurationsForEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    DeviceConfiguration cloneDeviceConfiguration(DeviceConfiguration templateDeviceConfiguration, String name);

    Optional<DeviceConfigConflictMapping> findDeviceConfigConflictMapping(long id);

    Set<ProtocolSupportedCalendarOptions> getSupportedTimeOfUseOptionsFor(DeviceType deviceType, boolean checkForVerifyCalendar);

    Optional<TimeOfUseOptions> findTimeOfUseOptions(DeviceType deviceType);

    Optional<TimeOfUseOptions> findAndLockTimeOfUseOptionsByIdAndVersion(DeviceType deviceType, long version);

    TimeOfUseOptions newTimeOfUseOptions(DeviceType deviceType);

    Optional<KeyAccessorType> findKeyAccessorTypeById(long id);


}