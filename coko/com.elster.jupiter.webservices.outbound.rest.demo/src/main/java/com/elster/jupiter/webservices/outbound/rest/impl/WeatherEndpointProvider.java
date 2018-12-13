/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.outbound.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;
import com.elster.jupiter.webservices.outbound.rest.WeatherService;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.client.WebTarget;

/**
 * Outbound REST web service -- DEMO BUNDLE
 */
@Component(name = "com.elster.jupiter.webservices.rest.outbound.service.demo.provider",
        service = {OutboundRestEndPointProvider.class},
        immediate = true,
        property = {"name=Open weather maps"})
public class WeatherEndpointProvider implements OutboundRestEndPointProvider {
    @Override
    public WeatherService get(WebTarget target) {
        return new WeatherServiceImpl(target);
    }

    @Override
    public Class<WeatherService> getService() {
        return WeatherService.class;
    }
}
