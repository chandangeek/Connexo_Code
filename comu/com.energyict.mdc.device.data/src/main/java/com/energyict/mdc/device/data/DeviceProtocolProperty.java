/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceProtocolProperty {

    /**
     * Gets the name of this PropertySpec.
     *
     * @return The name
     */
    String getName();

    /**
     * Represents the stringValue of the Property
     *
     * @return the string value of the Property
     */
    String getPropertyValue();

    void setValue(String value);

    void update();

}