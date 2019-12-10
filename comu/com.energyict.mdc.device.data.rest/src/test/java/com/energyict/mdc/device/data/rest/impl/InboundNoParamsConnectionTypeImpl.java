/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import javax.xml.bind.annotation.XmlElement;

public class InboundNoParamsConnectionTypeImpl extends NoParamsConnectionType {

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }
}