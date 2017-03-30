/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

public interface MessageAdapterMappingFactory {

    /**
     * Finds the {@link MessageAdapterMapping} for the specified
     * deviceProtocolJavaClassName and returns the messageSupportJavaClassName.
     *
     * @param deviceProtocolJavaClassName the javaClassName of the deviceProtocol which you need the referenced MessageAdapterMapping from
     * @return the requested MessageAdapterMapping mapping javaClassName
     */
    public String getMessageMappingJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName);
}
