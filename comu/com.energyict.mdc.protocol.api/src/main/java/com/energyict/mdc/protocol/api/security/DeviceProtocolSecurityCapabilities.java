package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.upl.meterdata.Device;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides functionality to expose a {@link DeviceProtocol DeviceProtocol}
 * his security capabilities.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 16:04
 */
public interface DeviceProtocolSecurityCapabilities extends com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities {

    /**
     * Returns the {@link CustomPropertySet} that provides the storage area
     * for the properties of a {@link Device} that has these security capabilities
     * or an empty Optional if there are no properties.
     * In that case, {@link #getSecurityPropertySpecs()} should return
     * an empty collection as well for consistency.
     * <p>
     * Note that none of the properties should be 'required'
     * because it is possible that the communication
     * expert configures the Device in such a way
     * that not all of the properties are actually needed.
     * As an example, say that the security capabilities
     * involve the following set of properties:
     * <ul>
     * <li>clientId</li>
     * <li>password</li>
     * <li>authentication key</li>
     * <li>encryption key</li>
     * </ul>
     * When the communication expert configures the Device
     * to always use a clientId and an authentication key
     * then the password and the encryption key are never used
     * and can therefore never be required.
     *
     * @return The CustomPropertySet
     */
    Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet();

    default List<PropertySpec> getSecurityPropertySpecs() {
        return this.getCustomPropertySet()
                .map(CustomPropertySet::getPropertySpecs)
                .orElseGet(Collections::emptyList);
    }
}