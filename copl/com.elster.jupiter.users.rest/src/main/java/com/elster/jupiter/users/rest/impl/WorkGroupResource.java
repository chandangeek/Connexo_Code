package com.elster.jupiter.users.rest.impl;


import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;
import com.elster.jupiter.users.rest.actions.CreateWorkGroupTransaction;
import com.elster.jupiter.users.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/workgroups")
public class WorkGroupResource {

    private final TransactionService transactionService;
    private final UserService userService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public WorkGroupResource(TransactionService transactionService, UserService userService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE})
    public WorkGroupInfo getWorkGroup(@PathParam("id") long id) {
        return userService.getWorkGroup(id).map(WorkGroupInfo::new).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public WorkGroupInfo createWorkGroup(WorkGroupInfo info) {
        return new WorkGroupInfo(transactionService.execute(new CreateWorkGroupTransaction(info, userService)));
    }

}
