package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;

/**
 * Copyrights EnergyICT
 * Date: 14/05/13
 * Time: 11:32
 */
public class DefaultCollectedDataFactoryProvider implements CollectedDataFactoryProvider {

    private CollectedDataFactory collectedDataFactory;

    @Override
    public CollectedDataFactory getCollectedDataFactory() {
        if (collectedDataFactory == null) {
            collectedDataFactory = new CollectedDataFactoryImpl(ServiceProvider.instance.get());
        }
        return collectedDataFactory;
    }
}
