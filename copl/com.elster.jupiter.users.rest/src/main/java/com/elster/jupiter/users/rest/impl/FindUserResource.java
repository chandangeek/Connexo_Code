package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfos;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

/**
 * Created by dragos on 11/20/2015.
 */

@Path("/findusers")
public class FindUserResource {
    private final UserService userService;
    private final RestQueryService restQueryService;
    private final Thesaurus thesaurus;

    @Inject
    public FindUserResource(UserService userService, RestQueryService restQueryService, Thesaurus thesaurus) {
        this.userService = userService;
        this.restQueryService = restQueryService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/{user}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public UserInfo getUser(@PathParam("user") String user) {
        Optional<User> found = null;
        try {
            found = userService.findUser(URLDecoder.decode(user, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(found.isPresent()){
            return new UserInfo(thesaurus, found.get());
        }

        return null;
    }

    @GET
    @Path("/{user}/groups")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public GroupInfos getUserGroups(@PathParam("user") String user) {
        Optional<User> found = null;
        try {
            found = userService.findUser(URLDecoder.decode(user, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(found.isPresent()){
             return new GroupInfos(thesaurus, found.get().getGroups());
        }

        return null;
    }
}
