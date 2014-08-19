package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.PrivilegeInfos;
import com.elster.jupiter.users.rest.ResourceInfos;
import com.elster.jupiter.users.rest.UserInfos;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/resources")
public class ResourceResource {
    private final UserService userService;
    private final RestQueryService restQueryService;

    @Inject
    public ResourceResource(UserService userService, RestQueryService restQueryService) {
        this.userService = userService;
        this.restQueryService = restQueryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_GROUP)
    public ResourceInfos getResources(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Resource> list = getResourceRestQuery().select(queryParameters, Order.ascending("name"));
        ResourceInfos infos = new ResourceInfos(queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    private RestQuery<Resource> getResourceRestQuery() {
        Query<Resource> query = userService.getResourceQuery();
        return restQueryService.wrap(query);
    }
}
