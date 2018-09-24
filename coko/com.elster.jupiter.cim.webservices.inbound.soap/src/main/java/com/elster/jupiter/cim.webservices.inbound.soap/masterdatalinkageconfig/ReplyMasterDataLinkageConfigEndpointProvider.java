/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import ch.iec.tc57._2011.replymasterdatalinkageconfig.MasterDataLinkageConfigPort;
import ch.iec.tc57._2011.replymasterdatalinkageconfig.ReplyMasterDataLinkageConfig;
import org.osgi.service.component.annotations.Component;

import javax.xml.ws.Service;

@Component(name = "com.elster.jupiter.cim.masterdatalinkageconfig.ReplyMasterDataLinkageConfigEndpointProvider",
        service = {OutboundSoapEndPointProvider.class}, immediate = true,
        property = "name=CIM ReplyMasterDataLinkageConfig")
public class ReplyMasterDataLinkageConfigEndpointProvider implements OutboundSoapEndPointProvider {

    @Override
    public Service get() {
        return new ReplyMasterDataLinkageConfig(ReplyMasterDataLinkageConfig.class.getResource("/masterdatalinkageconfig/ReplyMasterDataLinkageConfig.wsdl"));
    }

    @Override
    public Class getService() {
        return MasterDataLinkageConfigPort.class;
    }
}
