package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.DomainInfos;
import com.elster.jupiter.users.rest.GroupInfos;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/domains")
public class DomainResource {

    private final TransactionService transactionService;
    private final UserService userService;

    @Inject
    public DomainResource(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DomainInfos getGroups(@Context UriInfo uriInfo) {
        return new DomainInfos(userService.getUserDirectories());
    }

    @GET
    @Path("/{name}/")
    @Produces(MediaType.APPLICATION_JSON)
    public DomainInfos getGroup(@PathParam("name") String name) {
        Optional<UserDirectory> domain = userService.findUserDirectory(name);
        if (domain.isPresent()) {
            return new DomainInfos(domain.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}