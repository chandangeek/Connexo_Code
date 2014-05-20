package com.energyict.mdc.engine;

import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 * Date: 8/05/13
 * Time: 16:30
 */
public interface CollectedDataFactoryProvider {

    AtomicReference<CollectedDataFactoryProvider> instance = new AtomicReference<>();

    CollectedDataFactory getCollectedDataFactory();
}
