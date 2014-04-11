package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

/**
 * Copyrights EnergyICT
 * Date: 3/04/14
 * Time: 13:40
 */
public class ConnectionTypeInfo {
    public long id;
    public String name;

    public ConnectionTypeInfo() {
    }

    public static ConnectionTypeInfo from(long id, String name) {
        ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();
        connectionTypeInfo.id = id;
        connectionTypeInfo.name = name;
        return connectionTypeInfo;
    }

    public static ConnectionTypeInfo from(ConnectionTypePluggableClass connectionTypePluggableClass) {
        ConnectionTypeInfo connectionTypeInfo = new ConnectionTypeInfo();
        connectionTypeInfo.id = connectionTypePluggableClass.getId();
        connectionTypeInfo.name = connectionTypePluggableClass.getName();
        connectionTypePluggableClass.getProperties(connectionTypePluggableClass.getConnectionType().getPropertySpecs());
        return connectionTypeInfo;
    }
}
