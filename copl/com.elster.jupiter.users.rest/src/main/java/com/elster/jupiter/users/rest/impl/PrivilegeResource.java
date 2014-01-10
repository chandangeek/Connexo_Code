package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.PrivilegeInfos;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/privileges")
public class PrivilegeResource {

    private final UserService userService;

    @Inject
    public PrivilegeResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PrivilegeInfos getPrivileges(@Context UriInfo uriInfo) {
        return new PrivilegeInfos(userService.getPrivileges());
    }


}
