package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.impl.ServerPartialConnectionTask;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Provides services that relate to {@link DeviceType}s, {@link DeviceConfiguration}s
 * and the related master data such as {@link LogBookType}, {@link LoadProfileType} and
 * {@link RegisterMapping}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:34)
 */
public interface DeviceConfigurationService {

    public static String COMPONENTNAME = "DTC";

    public Finder<DeviceType> findAllDeviceTypes();

    public DeviceType newDeviceType (String name, String deviceProtocolPluggableClassName);

    public DeviceType newDeviceType (String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    /**
     * Find the {@link DeviceType} which is uniquely identified by the provided ID.
     *
     * @param deviceTypeId the ID of the DeviceType
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    public DeviceType findDeviceType(long deviceTypeId);

    /**
     * Find the {@link DeviceType} with the specified name.
     *
     * @param name The name
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    public DeviceType findDeviceTypeByName(String name);

    /**
     * Finds the {@link RegisterGroup} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The RegisterGroup or <code>null</code> if there is no such RegisterGroup
     */
    public RegisterGroup findRegisterGroup(long id);

    public List<RegisterGroup> findAllRegisterGroups();

    /**
     * Creates a new {@link RegisterGroup} with the specified name.
     * Note that the RegisterGroup is only saved in the database
     * after a call to the "save" method.
     *
     * @param name The name of the RegisterGroup
     * @return The new RegisterGroup
     * @see RegisterGroup#save()
     */
    public RegisterGroup newRegisterGroup(String name);

    public Finder<RegisterMapping> findAllRegisterMappings();

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public RegisterMapping findRegisterMapping(long id);

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the name.
     *
     * @param name The name
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public RegisterMapping findRegisterMappingByName(String name);

    public RegisterMapping findRegisterMappingByReadingType(ReadingType readingType);

    /**
     * Creates a new {@link RegisterMapping} with the specified required properties.
     * Note that {@link ObisCode} uniquely identifies the RegisterMapping,
     * i.e. there can only be 1 RegisterMapping for every ObisCode.
     *
     * @param name        The RegisterMapping name
     * @param obisCode    The ObisCode
     * @param unit        The unit
     * @param readingType The reading type
     * @param timeOfUse   Customer specif timeOfUse id
     * @return The new RegisterMapping
     * @see RegisterMapping#save()
     */
    public RegisterMapping newRegisterMapping(String name, ObisCode obisCode, Unit unit, ReadingType readingType, int timeOfUse);

    /**
     * Finds all the {@link LoadProfileType LoadProfileTypes} in the systesm
     *
     * @return all LoadProfileTypes
     */
    public List<LoadProfileType> findAllLoadProfileTypes();

    /**
     * Creates a new LoadProfileType based on the given parameters
     *
     * @param name     the Name of the LoadProfileType
     * @param obisCode the ObisCode of the LoadProfileType
     * @param interval the interval of the LoadProfileType
     * @return the newly created LoadProfileType
     */
    public LoadProfileType newLoadProfileType(String name, ObisCode obisCode, TimeDuration interval);

    /**
     * Find all {@link LogBookType LogBookTypes} in the system
     *
     * @return all the LogBookTypes in the system
     */
    public List<LogBookType> findAllLogBookTypes();

    /**
     * Creates a new LogBookType based on the given parameters
     *
     * @param name     the name of the LogBookType
     * @param obisCode the ObisCode of the LogBookType
     * @return the newly created LogBookType
     */
    public LogBookType newLogBookType(String name, ObisCode obisCode);

    /**
     * Finds a {@link DeviceConfiguration} which is uniquely identified by the given ID
     *
     * @param deviceConfigId the id of the DeviceConfiguration
     * @return the DeviceConfiguration or <code>null</code> if there is no such DeviceConfiguration
     */
    public DeviceConfiguration findDeviceConfiguration(long deviceConfigId);

    /**
     * Finds a {@link ChannelSpec} which is uniquely identified by the given ID
     *
     * @param channelSpecId the id of the ChannelSpec
     * @return the ChannelSpec or <code>null</code> if there is no such ChannelSpec
     */
    public ChannelSpec findChannelSpec(long channelSpecId);

    /**R
     * Finds a {@link RegisterSpec} which is uniquely identified by the given ID
     *
     * @param id the id of the RegisterSpec
     * @return the RegisterSpec or <code>null</code> if there is no such RegisterSpec
     */
    public RegisterSpec findRegisterSpec(long id);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are owned by the given {@link DeviceType} and modelled by the given {@link RegisterMapping RegisterMappings}
     * where the register spec if owned by an active device configuration
     *
     * @param deviceType      the DeviceType
     * @param registerMapping the list of RegisterMappings
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    public List<RegisterSpec> findActiveRegisterSpecsByDeviceTypeAndRegisterMapping(DeviceType deviceType, RegisterMapping registerMapping);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are owned by the given {@link DeviceType} and modelled by the given {@link RegisterMapping RegisterMappings}
     * where the register spec if owned by an inactive device configuration
     *
     * @param deviceType      the DeviceType
     * @param registerMapping the list of RegisterMappings
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    public List<RegisterSpec> findInactiveRegisterSpecsByDeviceTypeAndRegisterMapping(DeviceType deviceType, RegisterMapping registerMapping);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are modeled by the given RegisterMapping
     *
     * @param registerMappingId the ID of the RegisterMapping
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findRegisterSpecsByRegisterMapping(long registerMappingId);

    /**
     * Finds a list of {@link ChannelSpec ChannelSpecs} which are linked to the given {@link LoadProfileSpec}
     *
     * @param loadProfileSpec the LoadProfileSpec
     * @return the list of ChannelSpecs
     */
    public List<ChannelSpec> findChannelSpecsForLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    /**
     * Find the {@link LoadProfileType} with the given ID
     *
     * @param loadProfileTypeId the ID of the {@link LoadProfileType}
     * @return the LoadProfileType or <code>null</code> if there is no such LoadProfileType
     */
    public LoadProfileType findLoadProfileType(long loadProfileTypeId);

    /**
     * Find the {@link LoadProfileSpec} with the given ID
     *
     * @param loadProfileSpecId the ID of the LoadProfileSpec
     * @return the LoadProfileSpec or <code>null</code> if there is no such LoadProfileSpec
     */
    public LoadProfileSpec findLoadProfileSpec(int loadProfileSpecId);

    /**
     * Find a {@link LoadProfileSpec} which is modeled by the given {@link LoadProfileType} for the given
     * {@link DeviceConfiguration}
     *
     * @param deviceConfig    the DeviceConfiguration
     * @param loadProfileType the LoadProfileType which models the LoadProfileSpec
     * @return the requested LoadProfileSpec
     */
    public LoadProfileSpec findLoadProfileSpecsByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType);

    /**
     * Find a {@link LogBookType} with the given ID
     *
     * @param logBookTypeId the ID of the LogBookType
     * @return the LogBookType or <code>null</code> if there is no such LogBookType
     */
    public LogBookType findLogBookType(long logBookTypeId);

    /**
     * Find a {@link LogBookSpec} with the given ID
     *
     * @param id the ID of the LogBookSpec
     * @return the LogBookSpec or <code>null</code> if there is no such LogBookSpec
     */
    public LogBookSpec findLogBookSpec(long id);

    /**
     * Checks whether or not the given Phenomenon is in use
     *
     * @param phenomenon the Phenomenon to check
     * @return true if some object uses this Phenomenon, false otherwise
     */
    public boolean isPhenomenonInUse(Phenomenon phenomenon);

    public Phenomenon findPhenomenon(int phenomenonId);

    public Phenomenon newPhenomenon(String name, Unit unit);

    public Phenomenon findPhenomenonByNameAndUnit(String name, String unit);

    public ChannelSpec findChannelSpecForLoadProfileSpecAndRegisterMapping(LoadProfileSpec loadProfileSpec, RegisterMapping registerMapping);

    public List<DeviceConfiguration> findDeviceConfigurationsUsingLoadProfileType(LoadProfileType loadProfileType);

    public List<ChannelSpec> findChannelSpecsForRegisterMappingInLoadProfileType (RegisterMapping registerMapping, LoadProfileType loadProfileType);

    public List<DeviceConfiguration> findDeviceConfigurationsUsingLogBookType(LogBookType logBookType);

    public List<DeviceConfiguration> findDeviceConfigurationsUsingRegisterMapping(RegisterMapping registerMapping);

    public boolean isRegisterMappingUsedByDeviceType(RegisterMapping registerMapping);

    public List<DeviceType> findDeviceTypesWithDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    public List<LoadProfileType> findLoadProfileTypesByName(String name);

    public LogBookType findLogBookTypeByName(String name);

    public List<Phenomenon> findAllPhenomena();

    public Finder<DeviceConfiguration> findDeviceConfigurationsUsingDeviceType(DeviceType deviceType);

    public NextExecutionSpecs newNextExecutionSpecs (TemporalExpression temporalExpression);

    public NextExecutionSpecs findNextExecutionSpecs (long id);

    public DeviceCommunicationConfiguration findDeviceConfigurationService(long id);

    public DeviceCommunicationConfiguration findDeviceCommunicationConfigurationFor(DeviceConfiguration deviceConfiguration);

    public DeviceCommunicationConfiguration newDeviceCommunicationConfiguration(DeviceConfiguration deviceConfiguration);

    public Optional<ServerPartialConnectionTask> getPartialConnectionTask(long id);

    public List<ServerPartialConnectionTask> findByConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass);

    public Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(long id);

    List<ServerPartialConnectionTask> findByComPortPool(ComPortPool comPortPool);
}