/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import org.osgi.service.component.annotations.Component;

import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;

import javax.xml.ws.Service;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.meterconfig.provider",
        service = {OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=MeterConfig"})
public class MeterConfigServiceProvider implements OutboundSoapEndPointProvider {

    @Override
    public Service get() {
        return new ReplyMeterConfig();
    }

    @Override
    public Class getService() {
        return MeterConfigPort.class;
    }
}