/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.PrivilegeInfos;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/privileges")
public class PrivilegeResource {

    private final UserService userService;
    private final RestQueryService restQueryService;
    private final NlsService nlsService;

    @Inject
    public PrivilegeResource(UserService userService, RestQueryService restQueryService, NlsService nlsService) {
        this.userService = userService;
        this.restQueryService = restQueryService;
        this.nlsService = nlsService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public PrivilegeInfos getPrivileges(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Privilege> list = getPrivilegeRestQuery().select(queryParameters, Order.ascending("name"));
        PrivilegeInfos infos = new PrivilegeInfos(this.nlsService, queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    private RestQuery<Privilege> getPrivilegeRestQuery() {
        Query<Privilege> query = userService.getPrivilegeQuery();
        return restQueryService.wrap(query);
    }

}