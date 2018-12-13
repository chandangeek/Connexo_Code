/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.inbound.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;

@Component(name = "com.elster.jupiter.metering.rest.provider", service = {InboundRestEndPointProvider.class}, immediate = true, property = {"name=GetMeters"})
public class MeteringProvider implements InboundRestEndPointProvider {

    private volatile MeteringService meteringService;

    public MeteringProvider() {
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public Application get() {
        return new MeteringApplication(meteringService);
    }
}
