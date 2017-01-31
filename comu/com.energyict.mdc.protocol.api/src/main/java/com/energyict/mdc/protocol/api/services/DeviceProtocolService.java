/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

public interface DeviceProtocolService {

    public static String COMPONENT_NAME = "PR1"; // Stands for Protocol bundle 1 (as more protocol bundles can follow)

    /**
     * Creates an instance of the protocol of the specified className
     * or throws a {@link com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException}
     * when the class is not actually managed by the Device Protocol service.
     *
     * @param className the fully qualified Class name
     * @return the newly created DeviceProtocol
     */
    public Object createProtocol(String className);

}