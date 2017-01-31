/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim.soap.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.elster.jupiter.metering.cim.soap", service = {InboundSoapEndPointProvider.class}, immediate = true, property = {"name=CIM GetMeterReadings"})
public class GetMeterReadingsEndpointProvider implements InboundSoapEndPointProvider {

    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Clock clock;

    @Activate
    public void activate() {
        System.out.println("");
    }

    @Deactivate
    public void deactivate() {

    }

    @Override
    public Object get() {
        return new GetMeterReadingsPortImpl(meteringService, meteringGroupsService, clock);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
