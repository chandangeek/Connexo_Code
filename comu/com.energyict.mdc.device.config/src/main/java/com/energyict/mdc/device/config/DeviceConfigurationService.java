package com.energyict.mdc.device.config;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link DeviceType}s and {@link DeviceConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:34)
 */
@ProviderType
public interface DeviceConfigurationService {

    public static String COMPONENTNAME = "DTC";

    public Finder<DeviceType> findAllDeviceTypes();

    /**
     * Creates a new {@link DeviceType} with the specified name
     * that uses the specified {@link DeviceProtocolPluggableClass device protocol}
     * to communicate with the actual device.
     * The {@link com.elster.jupiter.fsm.State} of the devices
     * of this type are managed by the default {@link DeviceLifeCycle}
     * that was installed by this bundle.
     * Note however that if a user deleted that default DeviceLifeCycle
     * the creation will fail because the DeviceLifeCycle is required.
     *
     * @param name The name of the new DeviceType
     * @param deviceProtocolPluggableClass The DeviceProtocolPluggableClass
     * @return The newly created DeviceType
     */
    public DeviceType newDeviceType (String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    /**
     * Creates a new {@link DeviceType} with the specified name
     * that uses the specified {@link DeviceProtocolPluggableClass device protocol}
     * to communicate with the actual device.
     * The {@link com.elster.jupiter.fsm.State} of the devices
     * of this type are managed by the default {@link DeviceLifeCycle}.
     *
     * @param name The name of the new DeviceType
     * @param deviceProtocolPluggableClass The DeviceProtocolPluggableClass
     * @param deviceLifeCycle The DeviceLifeCycle
     * @return The newly created DeviceType
     */
    public DeviceType newDeviceType (String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceLifeCycle deviceLifeCycle);

    /**
     * Find the {@link DeviceType} which is uniquely identified by the provided ID.
     *
     * @param deviceTypeId the ID of the DeviceType
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    public Optional<DeviceType> findDeviceType(long deviceTypeId);

    /**
     * Finds and locks a {@link DeviceType} which is uniquely identified by the given ID and with the given VERSION.
     *
     * @param id the id of the DeviceType
     * @param version the version of the DeviceType
     * @return the DeviceType or empty if either the DeviceType does not exist
     *         or the version of the DeviceType is not equal to the specified version
     */
    public Optional<DeviceType> findAndLockDeviceType(long id, long version);
    /**
     * Find the {@link DeviceType} with the specified name.
     *
     * @param name The name
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    public Optional<DeviceType> findDeviceTypeByName(String name);

    /**
     * Returns the topic onto which {@link DeviceLifeCycleChangeEvent}s are published.
     *
     * @return The topic
     */
    public String changeDeviceLifeCycleTopicName();

    /**
     * Changes the {@link DeviceLifeCycle} of the specified {@link DeviceType}
     * to the specified DeviceLifeCycle, making sure that the switch will
     * not cause any problems on the existing devices of this type.
     * Note that this may throw an {@link IncompatibleDeviceLifeCycleChangeException}.
     *
     * @param deviceType The DeviceType
     * @param deviceLifeCycle The new DeviceLifeCycle
     */
    public void changeDeviceLifeCycle(DeviceType deviceType, DeviceLifeCycle deviceLifeCycle) throws IncompatibleDeviceLifeCycleChangeException;

    /**
     * Finds a {@link DeviceConfiguration} which is uniquely identified by the given ID.
     *
     * @param id the id of the DeviceConfiguration
     * @return the DeviceConfiguration or <code>null</code> if there is no such DeviceConfiguration
     */
    public Optional<DeviceConfiguration> findDeviceConfiguration(long id);

    /**
     * Finds and locks a {@link DeviceConfiguration} which is uniquely identified by the given ID and with the given VERSION.
     *
     * @param id the id of the DeviceConfiguration
     * @param version the version of the DeviceConfiguration
     * @return the DeviceConfiguration or empty if either the DeviceConfiguration does not exist
     *         or the version of the DeviceConfiguration is not equal to the specified version
     */
    public Optional<DeviceConfiguration> findAndLockDeviceConfigurationByIdAndVersion(long id, long version);

    /**
     * Finds a {@link ChannelSpec} which is uniquely identified by the given ID.
     *
     * @param channelSpecId the id of the ChannelSpec
     * @return the ChannelSpec or <code>null</code> if there is no such ChannelSpec
     */
    public Optional<ChannelSpec> findChannelSpec(long channelSpecId);

    /**R
     * Finds a {@link RegisterSpec} which is uniquely identified by the given ID.
     *
     * @param id the id of the RegisterSpec
     * @return the RegisterSpec or <code>null</code> if there is no such RegisterSpec
     */
    public Optional<RegisterSpec> findRegisterSpec(long id);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs}
     * that are owned by the given {@link DeviceType}
     * and modelled by the given RegisterType where the
     * register spec if owned by an active device configuration.
     *
     * @param deviceType      the DeviceType
     * @param registerType the list of RegisterType
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    public List<RegisterSpec> findActiveRegisterSpecsByDeviceTypeAndRegisterType(DeviceType deviceType, RegisterType registerType);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs}
     * that are owned by the given {@link DeviceType}
     * and modelled by the given RegisterType where the
     * register spec if owned by an inactive device configuration.
     *
     * @param deviceType      the DeviceType
     * @param registerType the list of RegisterType
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    public List<RegisterSpec> findInactiveRegisterSpecsByDeviceTypeAndRegisterType(DeviceType deviceType, RegisterType registerType);

    /**
     * Finds a list of {@link RegisterSpec}s which are modeled by the given RegisterType.
     *
     * @param measurementType the MeasurementType
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findRegisterSpecsByMeasurementType(MeasurementType measurementType);

    /**
     * Find the {@link LoadProfileSpec} with the given ID.
     *
     * @param loadProfileSpecId the ID of the LoadProfileSpec
     * @return the LoadProfileSpec or <code>null</code> if there is no such LoadProfileSpec
     */
    public Optional<LoadProfileSpec> findLoadProfileSpec(long loadProfileSpecId);

    /**
     * Find a {@link LoadProfileSpec} which is modeled by the given {@link LoadProfileType} for the given
     * {@link DeviceConfiguration}.
     *
     * @param deviceConfig    the DeviceConfiguration
     * @param loadProfileType the LoadProfileType which models the LoadProfileSpec
     * @return the requested LoadProfileSpec
     */
    public Optional<LoadProfileSpec> findLoadProfileSpecByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType);

    public List<LoadProfileSpec> findLoadProfileSpecsByLoadProfileType(LoadProfileType loadProfileType);

    /**
     * Find a {@link LogBookSpec} with the given ID.
     *
     * @param id the ID of the LogBookSpec
     * @return the LogBookSpec or <code>null</code> if there is no such LogBookSpec
     */
    public Optional<LogBookSpec> findLogBookSpec(long id);

    public Optional<ChannelSpec> findChannelSpecForLoadProfileSpecAndChannelType(LoadProfileSpec loadProfileSpec, ChannelType channelType);

    public List<DeviceConfiguration> findDeviceConfigurationsUsingLoadProfileType(LoadProfileType loadProfileType);

    public List<ChannelSpec> findChannelSpecsForMeasurementType(MeasurementType measurementType);

    public List<ChannelSpec> findChannelSpecsForChannelTypeInLoadProfileType(ChannelType channelType, LoadProfileType loadProfileType);

    public List<DeviceType> findDeviceTypesUsingLoadProfileType(LoadProfileType loadProfileType);

    public List<DeviceType> findDeviceTypesUsingLogBookType(LogBookType logBookType);

    public List<DeviceType> findDeviceTypesUsingRegisterType(MeasurementType measurementType);

    public List<DeviceType> findDeviceTypesUsingDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle);

    public List<DeviceConfiguration> findDeviceConfigurationsUsingLogBookType(LogBookType logBookType);

    public List<DeviceConfiguration> findDeviceConfigurationsUsingMeasurementType(MeasurementType measurementType);

    public boolean isRegisterTypeUsedByDeviceType(RegisterType registerType);

    public List<DeviceType> findDeviceTypesWithDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    public Finder<DeviceConfiguration> findDeviceConfigurationsUsingDeviceType(DeviceType deviceType);

    public Optional<PartialConnectionTask> findPartialConnectionTask(long id);

    public List<PartialConnectionTask> findByConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    public Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(long id);

    public List<PartialConnectionTask> findByComPortPool(ComPortPool comPortPool);

    public Optional<SecurityPropertySet> findSecurityPropertySet(long id);

    public Optional<ComTaskEnablement> findComTaskEnablement (long id);

    /**
     * Return a list of ComTasks that are legal for assignment to the ComSchedule.
     * A ComTask can be assigned to the comSchedule IF all devices
     * already linked to the schedule are enabled for the ComTask.
     * This list will include ComTasks already linked to the ComSchedule!
     *
     * @param comSchedule The ComSchedule
     * @return List of ComTasks, including ComTasks already linked to the schedule.
     */
    public List<ComTask> findAvailableComTasks(ComSchedule comSchedule);

    /**
     * Finds all currently <i>active</i> DeviceConfigurations for the given DeviceType.
     *
     * @param deviceType the DeviceType
     * @return the list of <i>active</i> DeviceConfigurations
     */
    public Finder<DeviceConfiguration> findActiveDeviceConfigurationsForDeviceType(DeviceType deviceType);

    public List<DeviceConfiguration> findDeviceConfigurationsForValidationRuleSet(long validationRuleSetId);

    public List<ReadingType> getReadingTypesRelatedToConfiguration(DeviceConfiguration configuration);

    public List<DeviceConfiguration> getLinkableDeviceConfigurations(ValidationRuleSet validationRuleSet);

    public List<SecurityPropertySet> findUniqueSecurityPropertySets();

    public Finder<DeviceConfiguration> findDeviceConfigurationsForEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    DeviceConfiguration cloneDeviceConfiguration(DeviceConfiguration templateDeviceConfiguration, String name);

}