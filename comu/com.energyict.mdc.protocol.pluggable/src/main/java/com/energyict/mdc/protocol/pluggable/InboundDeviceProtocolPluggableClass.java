/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

public interface InboundDeviceProtocolPluggableClass extends PluggableClass {

    /**
     * Returns the version of the {@link InboundDeviceProtocol} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    public String getVersion ();

    public InboundDeviceProtocol getInboundDeviceProtocol ();

}