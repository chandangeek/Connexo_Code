package com.elster.jupiter.users.rest.impl;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.security.Privileges;

@Path("/currentuser")
public class CurrentUserResource {
    
    private final UserPreferencesService userPreferencesService;
    
    @Inject
    public CurrentUserResource(UserPreferencesService userPreferencesService) {
        this.userPreferencesService = userPreferencesService;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_USER_ROLE)
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        return Response.ok(new UserInfo(user)).build();
    }
    
    @GET
    @Path("/preferences")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_USER_ROLE)
    public Response getUserPreferences(@Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        List<UserPreference> preferences = userPreferencesService.getPreferences(user);
        return Response.ok(new UserPreferenceInfos(preferences)).build();
    }
}
