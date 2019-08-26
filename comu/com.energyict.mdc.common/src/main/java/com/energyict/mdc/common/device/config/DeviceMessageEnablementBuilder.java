/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface DeviceMessageEnablementBuilder {

    /**
     * Adds a single DeviceMessageUserAction to the builder
     *
     * @param deviceMessageUserAction the deviceMessageUserAction to add
     * @return the currently building builder
     */
    DeviceMessageEnablementBuilder addUserAction(DeviceMessageUserAction deviceMessageUserAction);

    /**
     * Adds multiple DeviceMessageUserActions to the builder
     *
     * @param deviceMessageUserActions the deviceMessageUserActions to add
     * @return the currently building builder
     */
    DeviceMessageEnablementBuilder addUserActions(DeviceMessageUserAction... deviceMessageUserActions);

    DeviceMessageEnablement build();

}
