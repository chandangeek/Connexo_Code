package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import java.util.List;

/**
 * Provides functionality to expose a {@link DeviceProtocol DeviceProtocol}
 * his security capabilities.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 16:04
 */
public interface DeviceProtocolSecurityCapabilities {

    /**
     * Injects the {@link PropertySpecService} into this DeviceProtocolSecurityCapabilities
     * for it to be able to create {@link com.energyict.mdc.dynamic.PropertySpec}s
     * @param propertySpecService The PropertySpecService
     */
    public void setPropertySpecService (PropertySpecService propertySpecService);

    /**
     * Gets <b>ALL</b> the {@link PropertySpec properties}
     * that can be set on a physical Device for this DeviceSecuritySupport.
     * Note that none of the properties returned here
     * will be marked as 'required' because it is possible that the communication
     * expert has configured the devices is such a way
     * that not all of the properties are actually needed.
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
    public List<PropertySpec> getSecurityProperties();

    /**
     * Returns a String that is suitable as the name of
     * a RelationType that will hold the values of the
     * security properties of this DeviceSecuritySupport.
     * <p>
     * Note that classes that have exactly the same
     * security properties are allowed to return the same
     * name and this is in fact the main reason
     * why this class has the responsibility
     * of returning the name for the RelationType.
     *
     * @return The name of the RelationType that will hold security property values
     */
    public String getSecurityRelationTypeName();

    /**
     * Returns the List of {@link AuthenticationDeviceAccessLevel}s.
     * The List will be empty if this DeviceSecuritySupport
     * does not require any properties to be specified
     * for a process to be granted access to the data
     * that is contained in the actual Device.
     *
     * @return The List of AuthenticationDeviceAccessLevel
     */
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels();

    /**
     * Returns the List of {@link EncryptionDeviceAccessLevel}s.
     * The List will be empty if this DeviceSecuritySupport
     * does not require any properties to be specified
     * to decrypt the data that is contained in the actual Device
     * or when the Device does not support encryption.
     *
     * @return The List of EncryptionDeviceAccessLevel
     */
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels();

    /**
     * Returns the security {@link PropertySpec} with the specified name
     * or <code>null</code> if no such PropertySpec exists.
     *
     * @param name The name of the security property specification
     * @return The PropertySpec or <code>null</code>
     *         if no such PropertySpec exists
     */
    public PropertySpec getSecurityPropertySpec(String name);

}