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
public interface DeviceMessageAttribute extends com.energyict.mdc.upl.messages.DeviceMessageAttribute {

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

}