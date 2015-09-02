package com.elster.jupiter.users.rest.impl;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.DomainInfos;
import com.elster.jupiter.users.security.Privileges;

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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public DomainInfos getDomains(@Context UriInfo uriInfo) {
        return new DomainInfos(userService.getUserDirectories());
    }

    @GET
    @Path("/{name}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public DomainInfos getDomain(@PathParam("name") String name) {
        Optional<UserDirectory> domain = userService.findUserDirectory(name);
        if (domain.isPresent()) {
            return new DomainInfos(domain.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}