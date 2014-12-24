package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.services.Finder;
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
import java.util.Optional;
import java.util.List;

/**
 * Provides services that relate to {@link DeviceType}s, {@link DeviceConfiguration}s?
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:34)
 */
public interface DeviceConfigurationService {

    public static String COMPONENTNAME = "DTC";

    public Finder<DeviceType> findAllDeviceTypes();

    public DeviceType newDeviceType (String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    /**
     * Find the {@link DeviceType} which is uniquely identified by the provided ID.
     *
     * @param deviceTypeId the ID of the DeviceType
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    public Optional<DeviceType> findDeviceType(long deviceTypeId);

    /**
     * Find the {@link DeviceType} with the specified name.
     *
     * @param name The name
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    public Optional<DeviceType> findDeviceTypeByName(String name);

    /**
     * Finds a {@link DeviceConfiguration} which is uniquely identified by the given ID.
     *
     * @param id the id of the DeviceConfiguration
     * @return the DeviceConfiguration or <code>null</code> if there is no such DeviceConfiguration
     */
    public Optional<DeviceConfiguration> findDeviceConfiguration(long id);

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
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are owned by the given {@link DeviceType} and modelled by the given RegisterType
     * where the register spec if owned by an active device configuration
     *
     * @param deviceType      the DeviceType
     * @param registerType the list of RegisterType
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    public List<RegisterSpec> findActiveRegisterSpecsByDeviceTypeAndRegisterType(DeviceType deviceType, RegisterType registerType);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are owned by the given {@link DeviceType} and modelled by the given RegisterType
     * where the register spec if owned by an inactive device configuration
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
     * Finds a list of {@link ChannelSpec ChannelSpecs} which are linked to the given {@link LoadProfileSpec}
     *
     * @param loadProfileSpec the LoadProfileSpec
     * @return the list of ChannelSpecs
     */
    public List<ChannelSpec> findChannelSpecsForLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    /**
     * Find the {@link LoadProfileSpec} with the given ID
     *
     * @param loadProfileSpecId the ID of the LoadProfileSpec
     * @return the LoadProfileSpec or <code>null</code> if there is no such LoadProfileSpec
     */
    public Optional<LoadProfileSpec> findLoadProfileSpec(long loadProfileSpecId);

    /**
     * Find a {@link LoadProfileSpec} which is modeled by the given {@link LoadProfileType} for the given
     * {@link DeviceConfiguration}
     *
     * @param deviceConfig    the DeviceConfiguration
     * @param loadProfileType the LoadProfileType which models the LoadProfileSpec
     * @return the requested LoadProfileSpec
     */
    public Optional<LoadProfileSpec> findLoadProfileSpecByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType);

    public List<LoadProfileSpec> findLoadProfileSpecsByLoadProfileType(LoadProfileType loadProfileType);

    /**
     * Find a {@link LogBookSpec} with the given ID
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

    public List<DeviceConfiguration> findDeviceConfigurationsUsingLogBookType(LogBookType logBookType);

    public List<DeviceConfiguration> findDeviceConfigurationsUsingMeasurementType(MeasurementType measurementType);

    public boolean isRegisterTypeUsedByDeviceType(RegisterType registerType);

    public List<DeviceType> findDeviceTypesWithDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    public Finder<DeviceConfiguration> findDeviceConfigurationsUsingDeviceType(DeviceType deviceType);

    public DeviceCommunicationConfiguration findDeviceCommunicationConfiguration(long id);

    public DeviceCommunicationConfiguration findDeviceCommunicationConfigurationFor(DeviceConfiguration deviceConfiguration);

    public DeviceCommunicationConfiguration newDeviceCommunicationConfiguration(DeviceConfiguration deviceConfiguration);

    public Optional<PartialConnectionTask> getPartialConnectionTask(long id);

    public List<PartialConnectionTask> findByConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    public Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(long id);

    public List<PartialConnectionTask> findByComPortPool(ComPortPool comPortPool);

    public Optional<SecurityPropertySet> findSecurityPropertySet(long id);

    public boolean isPhenomenonInUse(Phenomenon phenomenon);

    public Optional<ComTaskEnablement> findComTaskEnablement (long id);

    /**
     * Finds the {@link ComTaskEnablement} that enables the execution
     * of the {@link ComTask} on devices of the specified {@link DeviceConfiguration}.
     *
     * @param comTask The ComTask
     * @param deviceConfiguration The DeviceConfiguration
     * @return The ComTaskEnablement
     */
    public Optional<ComTaskEnablement> findComTaskEnablement (ComTask comTask, DeviceConfiguration deviceConfiguration);

    /**
     * Return a list of ComTasks that are legal for assignment to the ComSchedule. A ComTask can be assigned to the comSchedule IF all devices
     * already linked to the schedule are enabled for the ComTask.
     * This list will include ComTasks already linked to the ComSchedule!
     * @param comSchedule
     * @return List of ComTasks, including ComTasks already linked to the schedule.
     */
    public List<ComTask> findAvailableComTasks(ComSchedule comSchedule);

    /**
     * Finds all currently <i>active</i> DeviceConfigurations for the given DeviceType
     *
     * @param deviceType the DeviceType
     * @return the list of <i>active</i> DeviceConfigurations
     */
    public Finder<DeviceConfiguration> findActiveDeviceConfigurationsForDeviceType(DeviceType deviceType);

    public List<DeviceConfiguration> findDeviceConfigurationsForValidationRuleSet(long validationRuleSetId);

    public List<ReadingType> getReadingTypesRelatedToConfiguration(DeviceConfiguration configuration);

    List<DeviceConfiguration> getLinkableDeviceConfigurations(ValidationRuleSet validationRuleSet);

    public List<SecurityPropertySet> findUniqueSecurityPropertySets();
}