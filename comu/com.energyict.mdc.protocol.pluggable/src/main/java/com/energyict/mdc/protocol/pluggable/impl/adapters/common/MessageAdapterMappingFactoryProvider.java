package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:17
 */
public interface MessageAdapterMappingFactoryProvider {

    public static final AtomicReference<MessageAdapterMappingFactoryProvider> INSTANCE = new AtomicReference<>();

    public MessageAdapterMappingFactory getMessageAdapterMappingFactory();

}
