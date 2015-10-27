package com.elster.jupiter.users.rest.impl;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfo;

@Path("/currentuser")
public class CurrentUserResource {
    
    private final UserService userService;
    private final Thesaurus thesaurus;
    
    @Inject
    public CurrentUserResource(UserService userService, Thesaurus thesaurus) {
        this.userService = userService;
        this.thesaurus = thesaurus;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        User user = fetchUser((User) securityContext.getUserPrincipal());
        return Response.ok(new UserInfo(thesaurus, user)).build();
    }
    
    @GET
    @Path("/preferences")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getUserPreferences(@Context SecurityContext securityContext) {
        User user = fetchUser((User) securityContext.getUserPrincipal());
        List<UserPreference> preferences = userService.getUserPreferencesService().getPreferences(user);
        return Response.ok(new UserPreferenceInfos(preferences)).build();
    }
    
    private User fetchUser(User cachedUser) {
        return userService.getUser(cachedUser.getId()).orElse(null);
    }
}
