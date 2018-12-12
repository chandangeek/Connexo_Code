/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

public interface CapabilityAdapterMappingFactory {

    /**
     * Finds the {@link DeviceCapabilityMapping} for the specified
     * deviceProtocolJavaClassName and returns the capabilities.
     *
     * @param deviceProtocolJavaClassName the javaClassName of the deviceProtocol which you need the referenced DeviceProtocolCapabilities from
     * @return the requested capabilities
     */
    public Integer getCapabilitiesMappingForDeviceProtocol(String deviceProtocolJavaClassName);

}