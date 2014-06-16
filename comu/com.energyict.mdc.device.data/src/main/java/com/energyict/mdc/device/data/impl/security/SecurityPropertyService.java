package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import java.util.Date;
import java.util.List;

/**
 * Models the behavior of an internal component that manages the
 * {@link SecurityProperty security properties} of a {@link com.energyict.mdc.device.data.Device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (16:25)
 */
public interface SecurityPropertyService {

    /**
     * Gets the List of {@link SecurityProperty SecurityProperties}
     * that have been created for the specified {@link Device}
     * and that were active on the specified Date.
     *
     * @param device The Device
     * @param securityPropertySet The SecurityPropertySet
     * @return The List of SecurityProperties
     */
    public List<SecurityProperty> getSecurityProperties(Device device, Date when, SecurityPropertySet securityPropertySet);

    /**
     * Tests if the {@link Device} has properties for the specified {@link SecurityPropertySet}
     * on the specified Date.
     *
     * @param device The Device
     * @param when The Date
     * @param securityPropertySet The SecurityPropertySet
     * @return A flag that indicates if the Device has security properties on the specified Date
     */
    public boolean hasSecurityProperties(Device device, Date when, SecurityPropertySet securityPropertySet);

}