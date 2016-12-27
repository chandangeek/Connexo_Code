package com.energyict.mdc.upl.security;

import com.energyict.mdc.upl.properties.TypedProperties;

/**
 * Converts the <i>new</i> {@link DeviceProtocolSecurityPropertySet}
 * to old property values which are used in legacy protocols.
 *
 * Copyrights EnergyICT
 * Date: 22/01/13
 * Time: 9:15
 */
public interface LegacySecurityPropertyConverter {

    /**
     * Convert/adapt the given {@link DeviceProtocolSecurityPropertySet} to a proper {@link TypedProperties}
     * which is understandable by a legacy protocol. The propertySet can be null however.
     *
     * @param deviceProtocolSecurityPropertySet
     *         the securityPropertySet which is used for this communication <b>OR NULL</b>
     * @return the created TypedProperties
     */
    TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

    /**
     * Convert the old style TypedProperties to the new SecurityPropertySet. This method will be used during migration and
     * should be required during normal operations.
     * @param typedProperties The value to convert
     * @return The DeviceProtocolSecurityPropertySet corresponding to the TypedProperties. Only the authenticationDeviceAccessLevel
     * and encryptionDeviceAccessLevel will be available, SecurityProperties will always be null.
     * @throws IllegalStateException if the TypedProperties dot contain the properties required by the protocol to convert the value
     * @throws IllegalArgumentException if the required properties are available but contain illegal values (like alfanumeric values
     * for numeric fields.
     */
    DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties);

}
