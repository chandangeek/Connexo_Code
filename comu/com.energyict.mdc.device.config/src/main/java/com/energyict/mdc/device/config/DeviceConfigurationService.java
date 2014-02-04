package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;

import java.util.List;

/**
 * Provides services that relate to {@link DeviceType}s, {@link DeviceConfiguration}s
 * and the related master data such as {@link LogBookType}, {@link LoadProfileType},
 * {@link RegisterMapping} and {@link ProductSpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:34)
 */
public interface DeviceConfigurationService {

    public static String COMPONENTNAME = "DTC";

    public List<DeviceType> findAllDeviceTypes();

    /**
     * Finds the {@link ProductSpec} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The ProductSpec or <code>null</code> if there is no such ProductSpec
     */
    public ProductSpec findProductSpec(long id);

    public List<ProductSpec> findAllProductSpecs();

    /**
     * Creates a new {@link ProductSpec} for the specified {@link ReadingType}.
     * Note the ReadingType uniquely identifies a ProductSpec,
     * i.e. there can only be 1 ProductSpec for every ReadingType.
     * Note that the ProductSpec is only saved in the database
     * after a call to the "save" method.
     *
     * @param readingType The ReadingType
     * @return The new ProductSpec
     * @see ProductSpec#save()
     */
    public ProductSpec newProductSpec(ReadingType readingType);

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

    public List<RegisterMapping> findAllRegisterMappings();

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

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the name.
     *
     * @param obisCode    The ObisCode
     * @param productSpec The ProductSpec
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public RegisterMapping findRegisterMappingByObisCodeAndProductSpec(ObisCode obisCode, ProductSpec productSpec);

    /**
     * Creates a new {@link RegisterMapping} with the specified required properties.
     * Note that {@link ObisCode} uniquely identifies the RegisterMapping,
     * i.e. there can only be 1 RegisterMapping for every ObisCode.
     * Note that the ProductSpec is only saved in the database
     * after a call to the "save" method.
     *
     * @param name        The RegisterMapping name
     * @param obisCode    The ObisCode
     * @param productSpec The ProductSpec
     * @return The new RegisterMapping
     * @see RegisterMapping#save()
     */
    public RegisterMapping newRegisterMapping(String name, ObisCode obisCode, ProductSpec productSpec);

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
     * Creates a new {@link RegisterSpec} based on the given parameters
     *
     * @param deviceConfiguration the DeviceConfiguration which will own the new RegisterSpec
     * @param registerMapping     the RegisterMapping which serves as a model for the RegisterSpec
     * @return the newly created RegisterSpec
     */
    public RegisterSpec newRegisterSpec(DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping);

    /**
     * Finds a {@link ChannelSpec} which is uniquely identified by the given ID
     *
     * @param channelSpecId the id of the ChannelSpec
     * @return the ChannelSpec or <code>null</code> if there is no such ChannelSpec
     */
    public ChannelSpec findChannelSpec(long channelSpecId);

    /**
     * Finds a {@link RegisterSpec} which is uniquely identified by the given ID
     *
     * @param id the id of the RegisterSpec
     * @return the RegisterSpec or <code>null</code> if there is no such RegisterSpec
     */
    public RegisterSpec findRegisterSpec(long id);

    /**
     * Finds all the {@link RegisterMapping RegisterMappings} which are defined in the given DeviceType
     *
     * @param deviceTypeId the ID of the {@link DeviceType}
     * @return all the RegisterMappings which are defined for the given DeviceType
     */
    public List<RegisterMapping> findRegisterMappingByDeviceType(int deviceTypeId);

    /**
     * Finds all the {@link RegisterSpec RegisterSpecs} which are modeled by the given list of {@link RegisterMapping RegisterMappings}
     *
     * @param mappings the list of {@link RegisterMapping RegisterMappings} which model the requested {@link RegisterSpec RegisterSpecs}
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findRegisterSpecsByRegisterMappings(List<RegisterMapping> mappings);

    /**
     * Find the {@link DeviceType} which is uniquely identified by the provided ID
     *
     * @param deviceTypeId the ID of the {@link DeviceType}
     * @return the DeviceType or <code>null</code> if there is no such DeviceType
     */
    public DeviceType findDeviceType(long deviceTypeId);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are owned by the given {@link DeviceType} and modeled by the given {@link RegisterMapping RegisterMappings}
     *
     * @param deviceType      the DeviceType
     * @param registerMapping the list of RegisterMappings
     * @return all the {@link RegisterSpec RegisterSpecs} which are defined for the given parameters
     */
    public List<RegisterSpec> findRegisterSpecsByDeviceTypeAndRegisterMapping(DeviceType deviceType, RegisterMapping registerMapping);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are modeled by the given RegisterMapping
     *
     * @param registerMappingId the ID of the RegisterMapping
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findRegisterSpecsByRegisterMapping(long registerMappingId);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are defined for the given {@link DeviceConfiguration}
     *
     * @param deviceConfig the DeviceConfiguration
     * @return the list of RegisterSpecs of the given DeviceConfiguration
     */
    public List<RegisterSpec> findRegisterSpecsByDeviceConfiguration(DeviceConfiguration deviceConfig);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are linked to the given {@link ChannelSpec} and
     * has the given {@link ChannelSpecLinkType}
     *
     * @param channelSpecId the ID of the {@link ChannelSpec}
     * @param linkType      the {@link ChannelSpecLinkType}
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findByChannelSpecAndLinkType(long channelSpecId, ChannelSpecLinkType linkType);

    /**
     * Finds a list of {@link RegisterSpec RegisterSpecs} which are owned by the given DeviceConfiguration and
     * modeled by the given RegisterMapping
     *
     * @param deviceConfigId    the ID of the {@link DeviceConfiguration}
     * @param registerMappingId the ID of the {@link RegisterMapping}
     * @return the list of RegisterSpecs
     */
    public List<RegisterSpec> findRegisterSpecsByDeviceConfigurationAndRegisterMapping(long deviceConfigId, long registerMappingId);
}