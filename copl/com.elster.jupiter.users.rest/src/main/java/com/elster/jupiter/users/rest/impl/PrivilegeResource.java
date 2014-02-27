package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.PrivilegeInfos;
import com.elster.jupiter.users.rest.actions.AddPrivilegeToGroupTransaction;
import com.elster.jupiter.users.rest.actions.DeletePrivilegeFromGroupTransaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/privileges")
public class PrivilegeResource {

    private final UserService userService;
    private final TransactionService transactionService;

    @Inject
    public PrivilegeResource(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PrivilegeInfos getPrivileges(@Context UriInfo uriInfo) {
        return new PrivilegeInfos(userService.getPrivileges());
    }

    @POST
    @Path("/{privilege}/groups/{idgroup}")
    @Produces(MediaType.APPLICATION_JSON)
    public PrivilegeInfos addPrivilegeToGroup(@PathParam("privilege") String privilege, @PathParam("idgroup") long idGroup) {
        PrivilegeInfos result = new PrivilegeInfos();
        result.addAll(transactionService.execute(new AddPrivilegeToGroupTransaction(privilege, idGroup, userService)));

        return result;
    }

    @DELETE
    @Path("/{privilege}/groups/{idgroup}")
    @Produces(MediaType.APPLICATION_JSON)
    public PrivilegeInfos removePrivilegeFromGroup(@PathParam("privilege") String privilege, @PathParam("idgroup") long idGroup) {
        PrivilegeInfos result = new PrivilegeInfos();
        result.addAll(transactionService.execute(new DeletePrivilegeFromGroupTransaction(privilege, idGroup, userService)));

        return result;
    }

    @GET
    @Path("/groups/{idgroup}")
    @Produces(MediaType.APPLICATION_JSON)
    public PrivilegeInfos getPrivilegesForGroup(@PathParam("idgroup") long idGroup) {
        PrivilegeInfos result = new PrivilegeInfos();

        Optional<Group> foundGroup = userService.getGroup(idGroup);
        if(!foundGroup.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        result.addAll(foundGroup.get().getPrivileges());
        return result;
    }

    @GET
    @Path("/users/{iduser}")
    @Produces(MediaType.APPLICATION_JSON)
    public PrivilegeInfos getPrivilegesForUser(@PathParam("iduser") long idUser) {
        PrivilegeInfos result = new PrivilegeInfos();

        Optional<User> foundUser = userService.getUser(idUser);
        if(!foundUser.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        result.addAll(foundUser.get().getPrivileges());
        return result;
    }
}
