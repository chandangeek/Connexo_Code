package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.multisense.api.impl.utils.TranslationSeeds;

public class ConnectionStrategyAdapter extends MapBasedXmlAdapter<ConnectionStrategy> {

    public ConnectionStrategyAdapter() {
        register(TranslationSeeds.MINIMIZE_CONNECTIONS.getKey(), ConnectionStrategy.MINIMIZE_CONNECTIONS);
        register(TranslationSeeds.AS_SOON_AS_POSSIBLE.getKey(), ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }
}
