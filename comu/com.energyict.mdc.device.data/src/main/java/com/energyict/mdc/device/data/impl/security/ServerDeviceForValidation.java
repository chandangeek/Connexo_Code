package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;

import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/01/2016 - 17:28
 */
public interface ServerDeviceForValidation extends Device {

    /**
     * Return a list per security property set that contains dirty properties that need to be validated
     */
    Map<SecurityPropertySet, TypedProperties> getDirtySecurityProperties();

}
