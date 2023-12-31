/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import javax.inject.Inject;

/**
 * Created by bvn on 6/10/16.
 */
public class WebServicesInfoFactory {
    private final Thesaurus thesaurus;
    private final PropertyValueInfoService propertyValueInfoService;
    private final WebServicesService webServicesService;

    @Inject
    public WebServicesInfoFactory(Thesaurus thesaurus, PropertyValueInfoService propertyValueInfoService, WebServicesService webServicesService) {
        this.thesaurus = thesaurus;
        this.propertyValueInfoService = propertyValueInfoService;
        this.webServicesService = webServicesService;
    }

    public WebServicesInfo from(WebService webService) {
        WebServicesInfo info = new WebServicesInfo();
        info.name = webService.getName();
        WebServiceDirection webServiceDirection = webService.isInbound() ? WebServiceDirection.INBOUND : WebServiceDirection.OUTBOUND;
        info.direction = new IdWithLocalizedValueInfo<>(webServiceDirection, webServiceDirection.getDisplayName(thesaurus));
        info.type = webService.getProtocol();
        info.properties = propertyValueInfoService.getPropertyInfos(webServicesService.getWebServicePropertySpecs(webService.getName()));
        return info;
    }
}
