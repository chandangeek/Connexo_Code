package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:17
 */
public interface DeviceCapabilityAdapterMappingFactoryProvider {

    public static final AtomicReference<DeviceCapabilityAdapterMappingFactoryProvider> INSTANCE = new AtomicReference<>();

    public CapabilityAdapterMappingFactory getCapabilityAdapterMappingFactory();

}
