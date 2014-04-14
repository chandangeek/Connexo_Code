package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.ConnectionStrategy;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ConnectionStrategyAdapter extends XmlAdapter<String, ConnectionStrategy> {

    @Override
    public ConnectionStrategy unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return ConnectionStrategy.valueOf(jsonValue);
    }

    @Override
    public String marshal(ConnectionStrategy connectionStrategy) throws Exception {
        if (connectionStrategy==null) {
            return null;
        }
        return connectionStrategy.name();
    }
}
