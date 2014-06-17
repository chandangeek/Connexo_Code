package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.ConnectionStrategy;

public class ConnectionStrategyAdapter extends MapBasedXmlAdapter<ConnectionStrategy> {

    public ConnectionStrategyAdapter() {
        register("", null);
        register("minimizeConnections", ConnectionStrategy.MINIMIZE_CONNECTIONS);
        register("asSoonAsPossible", ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }
}
