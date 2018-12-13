/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.ResourceInfos;
import com.elster.jupiter.users.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/resources")
public class ResourceResource {
    private final UserService userService;
    private final NlsService nlsService;

    @Inject
    public ResourceResource(UserService userService, NlsService nlsService) {
        this.userService = userService;
        this.nlsService = nlsService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public ResourceInfos getResources(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Resource> list = userService.getResources();
        ResourceInfos infos = new ResourceInfos(this.nlsService, queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

}