package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 * Date: 11/04/13
 * Time: 15:28
 */
public interface SecuritySupportAdapterMappingFactoryProvider {

    public static final AtomicReference<SecuritySupportAdapterMappingFactoryProvider> INSTANCE = new AtomicReference<>();

    SecuritySupportAdapterMappingFactory getSecuritySupportAdapterMappingFactory();
}
