package com.energyict.mdc.upl.security;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides functionality to expose the security capabilities of a {@link com.energyict.mdc.upl.DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-16 (09:28)
 */
public interface DeviceProtocolSecurityCapabilities extends Serializable {

    /**
     * Gets <b>ALL</b> the {@link PropertySpec property specs}
     * that can be set on a device for this DeviceSecuritySupport.
     * Note that the {@link PropertySpec#isRequired() 'required'}
     * aspect of the PropertySpec is ignored because it is possible
     * that the communication expert has configured the devices in
     * such a way that not all of the properties are actually needed.
     * As an example, say that this DeviceSecuritySupport
     * returns the following set of properties:
     * <ul>
     * <li>clientId</li>
     * <li>password</li>
     * <li>authentication key</li>
     * <li>encryption key</li>
     * </ul>
     * When the communication expert configures the device
     * to always use a clientId and an authentication key
     * then the password and the encryption key are never used
     * and can therefore never be required.
     *
     * @return The list of security properties
     */
    default List<PropertySpec> getSecurityProperties() {
        Set<PropertySpec> allSecurityPropertySpecs = new HashSet<>();
        getAuthenticationAccessLevels().forEach(accessLevel -> allSecurityPropertySpecs.addAll(accessLevel.getSecurityProperties()));
        getEncryptionAccessLevels().forEach(accessLevel -> allSecurityPropertySpecs.addAll(accessLevel.getSecurityProperties()));
        return new ArrayList<>(allSecurityPropertySpecs);
    }

    /**
     * Returns the security {@link PropertySpec} with the specified name
     * or an empty Optional if no such PropertySpec exists.
     *
     * @param name The name of the security property specification
     * @return The PropertySpec or an empty Optional if no such PropertySpec exists
     */
    default Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return getSecurityProperties()
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findAny();
    }

    /**
     * Returns the List of all possible {@link AuthenticationDeviceAccessLevel}s.
     * The List will be empty if this DeviceSecuritySupport
     * does not require any properties to be specified
     * for a process to be granted access to the data
     * that is contained in the actual Device.
     *
     * @return The List of AuthenticationDeviceAccessLevel
     */
    List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels();

    /**
     * Returns the List of all possible {@link EncryptionDeviceAccessLevel}s.
     * The List will be empty if this DeviceSecuritySupport
     * does not require any properties to be specified
     * to decrypt the data that is contained in the actual Device
     * or when the Device does not support encryption.
     *
     * @return The List of EncryptionDeviceAccessLevel
     */
    List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels();

    /**
     * Returns an optional {@link PropertySpec} containing the
     * {@link PropertySpec} to use for the 'client'. This property
     * spec can then be used to validate the value of the client field.
     *
     * If 'client' should not be used(~ communication with the device doesn't
     * need a client), then an empty Optional will be returned.
     *
     * @return The 'client' PropertySpec or an empty Optional in case the 'client' is not supported
     */
    Optional<PropertySpec> getClientSecurityPropertySpec();

}