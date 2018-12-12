/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfoFactory;
import com.elster.jupiter.users.rest.WorkGroupInfo;
import com.elster.jupiter.users.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dragos on 11/20/2015.
 */
@Path("/findworkgroups")
public class FindGroupResource {
    private final UserService userService;
    private final NlsService nlsService;
    private final UserInfoFactory userInfoFactory;

    @Inject
    public FindGroupResource(UserService userService, NlsService nlsService, UserInfoFactory userInfoFactory) {
        this.userService = userService;
        this.nlsService = nlsService;
        this.userInfoFactory = userInfoFactory;
    }

    @GET
    @Path("/{workgroup}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public WorkGroupInfo getGroup(@PathParam("workgroup") String workGroupName) {
        try {
            return userService
                    .getWorkGroup(URLDecoder.decode(workGroupName, "UTF-8"))
                    .map(group -> new WorkGroupInfo(group))
                    .orElse(null);
        } catch (UnsupportedEncodingException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @GET
    @Path("/{workgroup}/users")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public List<UserInfo> getGroupUsers(@PathParam("workgroup") String workGroup) {
        try {
            WorkGroup group = userService.getWorkGroup(URLDecoder.decode(workGroup, "UTF-8")).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            return group.getUsersInWorkGroup().stream()
                    .map(user -> userInfoFactory.from(this.nlsService, user))
                    .collect(Collectors.toList());
        } catch (UnsupportedEncodingException e) {
            throw new UnderlyingIOException(e);
        }
    }

}