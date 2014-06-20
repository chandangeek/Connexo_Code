package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.configuration.rest.impl.MessageSeeds;

public class ConnectionStrategyAdapter extends MapBasedXmlAdapter<ConnectionStrategy> {

    public ConnectionStrategyAdapter() {
        register("", null);
        register(MessageSeeds.MINIMIZE_CONNECTIONS.getKey(), ConnectionStrategy.MINIMIZE_CONNECTIONS);
        register(MessageSeeds.AS_SOON_AS_POSSIBLE.getKey(), ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }
}
