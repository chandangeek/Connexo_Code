package com.energyict.mdc.device.config;

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

    public List<RegisterMapping> findAllRegisterMappings ();

    public List<LoadProfileType> findAllLoadProfileTypes ();

    public List<LogBookType> findAllLogBookTypes ();

}