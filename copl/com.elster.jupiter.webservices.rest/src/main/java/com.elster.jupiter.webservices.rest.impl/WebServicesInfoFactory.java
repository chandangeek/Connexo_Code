package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.WebService;

/**
 * Created by bvn on 6/10/16.
 */
public class WebServicesInfoFactory {
    public WebServicesInfo from(WebService webService) {
        WebServicesInfo info = new WebServicesInfo();
        info.name = webService.getName();
        info.direction = webService.isInbound() ? WebServiceType.Inbound : WebServiceType.Outbound;
        info.type = WebServiceProtocol.SOAP;
        return info;
    }
}
