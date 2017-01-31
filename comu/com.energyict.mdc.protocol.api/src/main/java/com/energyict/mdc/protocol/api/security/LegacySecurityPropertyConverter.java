/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.energyict.mdc.common.TypedProperties;

public interface LegacySecurityPropertyConverter {

    /**
     * Convert/adapt the given {@link DeviceProtocolSecurityPropertySet} to a proper {@link TypedProperties}
     * which is understandable by a legacy protocol. The propertySet can be null however.
     *
     * @param deviceProtocolSecurityPropertySet
     *         the securityPropertySet which is used for this communication <b>OR NULL</b>
     * @return the created TypedProperties
     */
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

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
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties);

}
