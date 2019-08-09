/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import com.energyict.mdc.common.pluggable.PluggableClass;

public interface InboundDeviceProtocolPluggableClass extends PluggableClass {

    /**
     * Returns the version of the {@link InboundDeviceProtocol} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    String getVersion();

    InboundDeviceProtocol getInboundDeviceProtocol();

}