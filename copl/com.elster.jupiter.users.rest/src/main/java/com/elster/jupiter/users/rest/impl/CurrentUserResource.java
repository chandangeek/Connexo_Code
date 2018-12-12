/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/currentuser")
public class CurrentUserResource {

    private final UserService userService;
    private final NlsService nlsService;
    private final UserInfoFactory userInfoFactory;

    @Inject
    public CurrentUserResource(UserService userService, NlsService nlsService, UserInfoFactory userInfoFactory) {
        this.userService = userService;
        this.nlsService = nlsService;
        this.userInfoFactory = userInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        User user = fetchUser((User) securityContext.getUserPrincipal());
        return Response.ok(userInfoFactory.from(nlsService, user)).build();
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
