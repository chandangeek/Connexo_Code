/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import java.util.List;

public class ConnectionAttributes {
    private List<Attributes> attributes;
    private String connectionMethod;

    public List<Attributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attributes> attributes) {
        this.attributes = attributes;
    }

    public String getConnectionMethod() {
        return connectionMethod;
    }

    public void setConnectionMethod(String connectionMethod) {
        this.connectionMethod = connectionMethod;
    }
}
