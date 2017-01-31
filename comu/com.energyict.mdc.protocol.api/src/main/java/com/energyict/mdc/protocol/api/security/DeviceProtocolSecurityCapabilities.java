/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface DeviceProtocolSecurityCapabilities {

    /**
     * Returns the {@link CustomPropertySet} that provides the storage area
     * for the properties of a {@link BaseDevice} that has these security capabilities
     * or an empty Optional if there are no properties.
     * In that case, {@link #getSecurityPropertySpecs()} should return
     * an empty collection as well for consistency.
     * <p>
     * Note that none of the properties should be 'required'
     * because it is possible that the communication
     * expert configures the BaseDevice in such a way
     * that not all of the properties are actually needed.
     * As an example, say that the security capabilities
     * involve the following set of properties:
     * <ul>
     * <li>clientId</li>
     * <li>password</li>
     * <li>authentication key</li>
     * <li>encryption key</li>
     * </ul>
     * When the communication expert configures the BaseDevice
     * to always use a clientId and an authentication key
     * then the password and the encryption key are never used
     * and can therefore never be required.
     *
     * @return The CustomPropertySet
     */
    Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet();

    default List<PropertySpec> getSecurityPropertySpecs() {
        return this.getCustomPropertySet()
                .map(CustomPropertySet::getPropertySpecs)
                .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the security {@link PropertySpec} with the specified name
     * or an empty Optional if no such PropertySpec exists.
     *
     * @param name The name of the security property specification
     * @return The PropertySpec or an empty Optional if no such PropertySpec exists
     */
    default Optional<PropertySpec> getSecurityPropertySpec (String name) {
        return getSecurityPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findAny();
    }

    /**
     * Returns the List of {@link AuthenticationDeviceAccessLevel}s.
     * The List will be empty if this DeviceSecuritySupport
     * does not require any properties to be specified
     * for a process to be granted access to the data
     * that is contained in the actual Device.
     *
     * @return The List of AuthenticationDeviceAccessLevel
     */
    List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels();

    /**
     * Returns the List of {@link EncryptionDeviceAccessLevel}s.
     * The List will be empty if this DeviceSecuritySupport
     * does not require any properties to be specified
     * to decrypt the data that is contained in the actual Device
     * or when the Device does not support encryption.
     *
     * @return The List of EncryptionDeviceAccessLevel
     */
    List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels();

}