package com.elster.jupiter.users.rest.impl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.ResourceInfos;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

@Path("/resources")
public class ResourceResource {
    private final UserService userService;
    private final RestQueryService restQueryService;
    private final Thesaurus thesaurus;

    @Inject
    public ResourceResource(UserService userService, RestQueryService restQueryService, Thesaurus thesaurus) {
        this.userService = userService;
        this.restQueryService = restQueryService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public ResourceInfos getResources(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Resource> list = userService.getResources();//getResourceRestQuery().select(queryParameters, Order.ascending("name"));
        ResourceInfos infos = new ResourceInfos(thesaurus, queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    private RestQuery<Resource> getResourceRestQuery() {
        Query<Resource> query = userService.getResourceQuery();
        return restQueryService.wrap(query);
    }
}
