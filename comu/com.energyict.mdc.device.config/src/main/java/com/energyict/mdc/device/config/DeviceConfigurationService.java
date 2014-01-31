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

    public List<DeviceType> findAllDeviceTypes ();

    /**
     * Finds the {@link ProductSpec} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The ProductSpec or <code>null</code> if there is no such ProductSpec
     */
    public ProductSpec findProductSpec (long id);

    public List<ProductSpec> findAllProductSpecs ();

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
    public ProductSpec newProductSpec (ReadingType readingType);

    /**
     * Finds the {@link RegisterGroup} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The RegisterGroup or <code>null</code> if there is no such RegisterGroup
     */
    public RegisterGroup findRegisterGroup (long id);

    public List<RegisterGroup> findAllRegisterGroups ();

    /**
     * Creates a new {@link RegisterGroup} with the specified name.
     * Note that the RegisterGroup is only saved in the database
     * after a call to the "save" method.
     *
     * @param name The name of the RegisterGroup
     * @return The new RegisterGroup
     * @see RegisterGroup#save()
     */
    public RegisterGroup newRegisterGroup (String name);

    public List<RegisterMapping> findAllRegisterMappings ();

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public RegisterMapping findRegisterMapping (long id);

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the name.
     *
     * @param name The name
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public RegisterMapping findRegisterMappingByName (String name);

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the name.
     *
     * @param obisCode The ObisCode
     * @param productSpec The ProductSpec
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public RegisterMapping findRegisterMappingByObisCodeAndProductSpec (ObisCode obisCode, ProductSpec productSpec);

    /**
     * Creates a new {@link RegisterMapping} with the specified required properties.
     * Note that {@link ObisCode} uniquely identifies the RegisterMapping,
     * i.e. there can only be 1 RegisterMapping for every ObisCode.
     * Note that the ProductSpec is only saved in the database
     * after a call to the "save" method.
     *
     * @param name The RegisterMapping name
     * @param obisCode The ObisCode
     * @param productSpec The ProductSpec
     * @return The new RegisterMapping
     * @see RegisterMapping#save()
     */
    public RegisterMapping newRegisterMapping (String name, ObisCode obisCode, ProductSpec productSpec);

    public List<LoadProfileType> findAllLoadProfileTypes ();

    public LoadProfileType newLoadProfileType (String name, ObisCode obisCode, TimeDuration interval);

    public List<LogBookType> findAllLogBookTypes ();

    public LogBookType newLogBookType (String name, ObisCode obisCode);

}