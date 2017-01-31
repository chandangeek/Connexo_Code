/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

public interface DeviceProtocolMessageService {

    /**
     * Create a DeviceProtocol messages related object
     * for the given javaClassName
     *
     * @param javaClassName the javaClassName to use as model for the DeviceProtocol messages related object
     * @return the created DeviceProtocolPluggableClass
     */
    Object createDeviceProtocolMessagesFor(String javaClassName);

}
