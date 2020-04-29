/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
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

    long getVesion();

}