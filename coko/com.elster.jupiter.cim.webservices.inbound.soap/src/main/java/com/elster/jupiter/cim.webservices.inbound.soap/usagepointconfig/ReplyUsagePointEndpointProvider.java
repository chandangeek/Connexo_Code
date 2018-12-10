/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import ch.iec.tc57._2011.replyusagepointconfig.ReplyUsagePointConfig;
import ch.iec.tc57._2011.replyusagepointconfig.UsagePointConfigPort;
import org.osgi.service.component.annotations.Component;

import javax.xml.ws.Service;


@Component(name = "com.elster.jupiter.cim.usagepointconfig.ReplyUsagePointEndpointProvider",
        service = {OutboundSoapEndPointProvider.class}, immediate = true,
        property = "name=CIM ReplyUsagePointEndpoint")
public class ReplyUsagePointEndpointProvider implements OutboundSoapEndPointProvider {

    @Override
    public Service get() {
        return new ReplyUsagePointConfig(ReplyUsagePointConfig.class.getResource("/usagepointconfig/ReplyUsagePointConfig.wsdl"));
    }

    @Override
    public Class getService() {
        return UsagePointConfigPort.class;
    }
}
