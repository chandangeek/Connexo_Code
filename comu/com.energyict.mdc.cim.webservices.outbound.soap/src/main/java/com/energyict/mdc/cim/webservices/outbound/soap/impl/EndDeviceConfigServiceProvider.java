/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import org.osgi.service.component.annotations.Component;

import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;

import javax.xml.ws.Service;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.enddeviceconfig.provider",
        service = {StateTransitionWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + StateTransitionWebServiceClient.NAME})
public class EndDeviceConfigServiceProvider implements StateTransitionWebServiceClient, OutboundSoapEndPointProvider {

    @Override
    public Service get() {
        return new ReplyMeterConfig();
    }

    @Override
    public Class getService() {
        return MeterConfigPort.class;
    }

    @Override
    public String getWebServiceName() {
        return StateTransitionWebServiceClient.NAME;
    }

    @Override
    public boolean call(long id, EndPointConfiguration endPointConfiguration) {
        return false;
    }
}