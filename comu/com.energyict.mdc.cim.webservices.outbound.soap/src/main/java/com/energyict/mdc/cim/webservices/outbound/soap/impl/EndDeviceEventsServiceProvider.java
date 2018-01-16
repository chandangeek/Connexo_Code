/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import ch.iec.tc57._2011.enddeviceevents.ReplyEndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.ReplyEndDeviceEventsPort;

import org.osgi.service.component.annotations.Component;

import javax.xml.ws.Service;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents.provider",
        service = {OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=EndDeviceEvents"})
public class EndDeviceEventsServiceProvider implements OutboundSoapEndPointProvider {

    @Override
    public Service get() {
        return new ReplyEndDeviceEvents();
    }

    @Override
    public Class getService() {
        return ReplyEndDeviceEventsPort.class;
    }
}
