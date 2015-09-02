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
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.PrivilegeInfos;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

@Path("/privileges")
public class PrivilegeResource {

    private final UserService userService;
    private final RestQueryService restQueryService;

    @Inject
    public PrivilegeResource(UserService userService, RestQueryService restQueryService) {
        this.userService = userService;
        this.restQueryService = restQueryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public PrivilegeInfos getPrivileges(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Privilege> list = getPrivilegeRestQuery().select(queryParameters, Order.ascending("name"));
        PrivilegeInfos infos = new PrivilegeInfos(queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    private RestQuery<Privilege> getPrivilegeRestQuery() {
        Query<Privilege> query = userService.getPrivilegeQuery();
        return restQueryService.wrap(query);
    }

}
