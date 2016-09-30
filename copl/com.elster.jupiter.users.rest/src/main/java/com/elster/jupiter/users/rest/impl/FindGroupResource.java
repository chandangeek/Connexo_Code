package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.UserInfos;
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
@Path("/findgroups")
public class FindGroupResource {
    private final UserService userService;
    private final NlsService nlsService;

    @Inject
    public FindGroupResource(UserService userService, NlsService nlsService) {
        this.userService = userService;
        this.nlsService = nlsService;
    }

    @GET
    @Path("/{group}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public GroupInfo getGroup(@PathParam("group") String groupName) {
        try {
            return userService
                    .getGroup(URLDecoder.decode(groupName, "UTF-8"))
                    .map(group -> new GroupInfo(this.nlsService, group))
                    .orElse(null);
        } catch (UnsupportedEncodingException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @GET
    @Path("/{group}/users")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public UserInfos getGroupUsers(@PathParam("group") String group) {
        try {
            return new UserInfos(this.nlsService, userService.getGroupMembers(URLDecoder.decode(group, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new UnderlyingIOException(e);
        }
    }

}