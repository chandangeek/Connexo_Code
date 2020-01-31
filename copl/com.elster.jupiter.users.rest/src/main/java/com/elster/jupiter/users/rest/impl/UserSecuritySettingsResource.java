package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserSecuritySettingsInfo;
import com.elster.jupiter.users.rest.actions.UpdateUserSecuritySettings;
import com.elster.jupiter.users.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/userSecuritySettings")
public class UserSecuritySettingsResource {

    private final TransactionService transactionService;
    private final UserService userService;

    @Inject
    public UserSecuritySettingsResource(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE})
    public UserSecuritySettingsInfo getLoginSettings(@BeanParam JsonQueryParameters queryParameters) {
        if(userService.getLockingAccountSettings().isPresent())
            return new UserSecuritySettingsInfo(userService.getLockingAccountSettings().get());

        return new UserSecuritySettingsInfo();
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE})
    public Response updateLoginSettings(UserSecuritySettingsInfo info, @PathParam("id") long id) {
        transactionService.execute(new UpdateUserSecuritySettings(info, userService));
        if(userService.getLockingAccountSettings().isPresent())
            return Response.ok().entity(new UserSecuritySettingsInfo(userService.getLockingAccountSettings().get())).build();

        return Response.ok().build();
    }
}