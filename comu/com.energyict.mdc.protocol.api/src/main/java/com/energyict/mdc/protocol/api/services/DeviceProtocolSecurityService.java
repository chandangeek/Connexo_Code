/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

public interface DeviceProtocolSecurityService {

    /**
     * Create a DeviceProtocol security related object
     * for the given javaClassName
     *
     * @param javaClassName the javaClassName to use as model for the DeviceProtocol security related object
     * @return the created DeviceProtocolPluggableClass
     */
    Object createDeviceProtocolSecurityFor(String javaClassName);

}
