package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:23
 */
public class DeviceCapabilityAdapterMappingImpl implements DeviceCapabilityMapping {

    private String deviceProtocolJavaClassName;
    private int deviceProtocolCapabilities;

    // For persistence framework only
    public DeviceCapabilityAdapterMappingImpl() {
        super();
    }

    public DeviceCapabilityAdapterMappingImpl(String deviceProtocolJavaClassName, int deviceProtocolCapabilities) {
        super();
        this.deviceProtocolJavaClassName = deviceProtocolJavaClassName;
        this.deviceProtocolCapabilities = deviceProtocolCapabilities;
    }

    @Override
    public String getDeviceProtocolJavaClassName() {
        return deviceProtocolJavaClassName;
    }

    @Override
    public int getDeviceProtocolCapabilities() {
        return deviceProtocolCapabilities;
    }

}