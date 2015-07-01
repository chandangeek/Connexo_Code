package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import java.time.Instant;
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
    public List<SecurityProperty> getSecurityProperties(Device device, Instant when, SecurityPropertySet securityPropertySet);

    /**
     * Gets the List of {@link SecurityProperty SecurityProperties},
     * ignoring the user's privileges,
     * that have been created for the specified {@link Device}
     * and that were active on the specified Date.
     *
     * @param device The Device
     * @param securityPropertySet The SecurityPropertySet
     * @return The List of SecurityProperties
     */
    public List<SecurityProperty> getSecurityPropertiesIgnoringPrivileges(Device device, Instant when, SecurityPropertySet securityPropertySet);

    /**
     * Tests if the {@link Device} has properties for the specified {@link SecurityPropertySet}
     * on the specified Date regardless of the user's privileges.
     *
     * @param device The Device
     * @param when The Date
     * @param securityPropertySet The SecurityPropertySet
     * @return A flag that indicates if the Device has security properties on the specified Date
     */
    public boolean hasSecurityProperties(Device device, Instant when, SecurityPropertySet securityPropertySet);

    /**
     * Tests if all the security properties that are define in the configuration level
     * are valid for the specified {@link Device}.
     * Security properties for a SecurityPropertySet can be invalid for the following reasons:
     * <ul>
     * <li>No properties have been defined</li>
     * <li>Some or all of the required properties have not been specified yet</li>
     * </ul>
     *
     * @param device The Device
     * @return A flag that indicates if all security properties are valid for the Device
     */
    public boolean securityPropertiesAreValid(Device device);

    /**
     * Removes all securityProperties for the given Device
     *
     * @param device the device which properties need to be deleted
     */
    void deleteSecurityPropertiesFor(Device device);

}