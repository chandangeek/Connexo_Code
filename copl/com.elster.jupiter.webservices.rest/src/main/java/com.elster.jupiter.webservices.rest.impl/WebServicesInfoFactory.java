package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;

import javax.inject.Inject;

/**
 * Created by bvn on 6/10/16.
 */
public class WebServicesInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public WebServicesInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public WebServicesInfo from(WebService webService) {
        WebServicesInfo info = new WebServicesInfo();
        info.name = webService.getName();
        WebServiceType webServiceType = webService.isInbound() ? WebServiceType.INBOUND : WebServiceType.OUTBOUND;
        info.direction = new IdWithDisplayValueInfo<>(webServiceType, webServiceType.getDisplayName(thesaurus));
        info.type = WebServiceProtocol.SOAP;
        return info;
    }
}
