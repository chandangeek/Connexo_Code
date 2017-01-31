/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/webservices")
public class WebServicesResource {

    private final WebServicesService webServicesService;
    private final WebServicesInfoFactory webServicesInfoFactory;

    @Inject
    public WebServicesResource(WebServicesService webServicesService, WebServicesInfoFactory webServicesInfoFactory) {
        this.webServicesService = webServicesService;
        this.webServicesInfoFactory = webServicesInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getWebServices(@BeanParam JsonQueryParameters queryParameters) {
        List<WebServicesInfo> collect = webServicesService.getWebServices()
                .stream()
                .map(webServicesInfoFactory::from)
                .sorted(Comparator.comparing(x -> x.name))
                .collect(toList());
        return PagedInfoList.fromCompleteList("webServices", collect, queryParameters);
    }

}
