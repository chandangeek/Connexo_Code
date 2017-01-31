/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.messages;

import com.elster.jupiter.properties.PropertySpec;

/**
 * Models an attribute of a {@link DeviceMessage}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (17:14)
 */
public interface DeviceMessageAttribute {

    /**
     * Gets the {@link PropertySpec specification}
     * of this attribute.
     *
     * @return The DeviceMessageAttributeSpec
     */
    PropertySpec getSpecification();

    /**
     * Gets the owning {@link DeviceMessage}.
     *
     * @return The DeviceMessage
     */
    DeviceMessage getDeviceMessage();

    /**
     * Gets the name of this attribute, which is copied from
     * the {@link PropertySpec specification}.
     *
     * @return The name of this attribute
     * @see PropertySpec#getName()
     */
    String getName();

    /**
     * Gets the value of this attribute, which is compatible
     * with the ValueFactory ValueDomain
     * of the {@link PropertySpec specification}.
     * It is the responsibility of the caller to know
     * the ValueDomain and to cast the value to the class
     * or interface that is compatible with that ValueDomain.
     *
     * @return The value of this attribute
     */
    Object getValue();

}