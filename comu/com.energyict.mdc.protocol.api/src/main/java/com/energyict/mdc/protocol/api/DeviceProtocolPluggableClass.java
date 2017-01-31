/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Set;

public interface DeviceProtocolPluggableClass extends PluggableClass {

    /**
     * Returns the version of the {@link DeviceProtocol} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    String getVersion ();

    DeviceProtocol getDeviceProtocol ();

    TypedProperties getProperties ();

    default boolean supportsFileManagement() {
        Set<DeviceMessageId> fileMessages = DeviceMessageId.fileManagementRelated();
        return this.getDeviceProtocol()
                .getSupportedMessages()
                .stream()
                .anyMatch(fileMessages::contains);
    }

}