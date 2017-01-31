/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.properties.PropertySpec;

public interface OfflineDeviceMessageAttribute {

    /**
     * The PropertySpec which models the DeviceMessageAttribute.
     *
     * @return the propertySpec of the DeviceMessageAttribute
     */
    PropertySpec getPropertySpec();

    /**
     * The name of this DeviceMessageAttribute.
     *
     * @return the name of the DeviceMessageAttribute
     */
    String getName();

    /**
     * The related object/value of the DeviceMessageAttribute.
     *
     * @return this will contain the information to send or the action to perform on the Device
     */
    String getDeviceMessageAttributeValue();

}