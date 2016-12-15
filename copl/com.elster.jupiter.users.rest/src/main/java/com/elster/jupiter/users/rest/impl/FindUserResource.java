package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.UnderlyingIOException;
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

/**
 * Created by dragos on 11/20/2015.
 */

@Path("/findusers")
public class FindUserResource {
    private final UserService userService;
    private final NlsService nlsService;

    @Inject
    public FindUserResource(UserService userService, NlsService nlsService) {
        this.userService = userService;
        this.nlsService = nlsService;
    }

    @GET
    @Path("/{user}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public UserInfo getUser(@PathParam("user") String authenticationName) {
        try {
            return userService
                    .findUser(URLDecoder.decode(authenticationName, "UTF-8"))
                    .map(user -> new UserInfo(this.nlsService, user))
                    .orElse(null);
        } catch (UnsupportedEncodingException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @GET
    @Path("/{user}/groups")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public GroupInfos getUserGroups(@PathParam("user") String authenticationName) {
        try {
            return userService
                    .findUser(URLDecoder.decode(authenticationName, "UTF-8"))
                    .map(user -> new GroupInfos(this.nlsService, user.getGroups()))
                    .orElse(null);
        } catch (UnsupportedEncodingException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @GET
    @Path("/{user}/workgroups")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public WorkGroupInfos getUserWorkGroups(@PathParam("user") String authenticationName) {
        try {
            return userService
                    .findUser(URLDecoder.decode(authenticationName, "UTF-8"))
                    .map(user -> new WorkGroupInfos(user.getWorkGroups()))
                    .orElse(null);
        } catch (UnsupportedEncodingException e) {
            throw new UnderlyingIOException(e);
        }
    }

}