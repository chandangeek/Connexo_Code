package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:17
 */
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
