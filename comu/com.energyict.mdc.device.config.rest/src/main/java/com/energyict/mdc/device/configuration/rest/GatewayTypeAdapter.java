/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.device.config.GatewayType;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class GatewayTypeAdapter extends XmlAdapter<String, GatewayType> {
    @Override
    public GatewayType unmarshal(String v) throws Exception {
        return GatewayType.fromKey(v).orElse(GatewayType.NONE);
    }

    @Override
    public String marshal(GatewayType v) throws Exception {
        return v != null ? v.getKey() : null;
    }
}
