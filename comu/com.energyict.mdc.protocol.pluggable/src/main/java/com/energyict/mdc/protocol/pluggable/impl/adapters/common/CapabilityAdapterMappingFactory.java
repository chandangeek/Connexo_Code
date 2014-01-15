package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:17
 */
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