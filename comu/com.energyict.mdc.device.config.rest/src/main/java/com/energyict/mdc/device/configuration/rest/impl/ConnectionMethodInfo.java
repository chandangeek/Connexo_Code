package com.energyict.mdc.device.configuration.rest.impl;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Copyrights EnergyICT
 * Date: 1/04/14
 * Time: 10:43
 */
public class ConnectionMethodInfo {
    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;


    public ConnectionMethodInfo() {
    }

    public static ConnectionMethodInfo from(long id, String name) {
        ConnectionMethodInfo connectionMethodInfo = new ConnectionMethodInfo();
        connectionMethodInfo.id = id;
        connectionMethodInfo.name = name;
        return connectionMethodInfo;
    }
}
