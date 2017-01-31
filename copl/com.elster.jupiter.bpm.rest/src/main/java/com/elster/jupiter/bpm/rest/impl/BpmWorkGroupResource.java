/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.rest.SimplifiedUserInfo;
import com.elster.jupiter.bpm.rest.WorkGroupInfo;
import com.elster.jupiter.bpm.rest.resource.StandardParametersBean;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.bpm.rest.TranslationKeys.BPM_ASSIGNEE_UNASSIGNED;

@Path("/workgroups")
public class BpmWorkGroupResource {

    private final UserService userService;
    private final Thesaurus thesaurus;

    @Inject
    public BpmWorkGroupResource(UserService userService, Thesaurus thesaurus){
        this.userService = userService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public PagedInfoList getAllWorkGroupAssignees(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext){
        if (params.getUriInfo().getQueryParameters().getFirst("myworkgroups") != null && params.getUriInfo()
                .getQueryParameters()
                .getFirst("myworkgroups")
                .equals("true")) {
            return PagedInfoList.fromCompleteList("workgroups", userService.getWorkGroups()
                    .stream()
                    .filter(workGroup -> workGroup.getUsersInWorkGroup()
                            .stream()
                            .anyMatch(user -> user.equals(securityContext.getUserPrincipal())))
                    .map(WorkGroupInfo::new)
                    .sorted((first, second) -> first.name.toLowerCase().compareTo(second.name.toLowerCase()))
                    .collect(Collectors.toList()), queryParameters);
        } else {
            List<WorkGroupInfo> workGroupInfos = userService.getWorkGroups()
                    .stream()
                    .map(WorkGroupInfo::new)
                    .sorted((first, second) -> first.name.toLowerCase().compareTo(second.name.toLowerCase()))
                    .collect(Collectors.toList());
            workGroupInfos.add(0, new WorkGroupInfo(thesaurus.getFormat(BPM_ASSIGNEE_UNASSIGNED).format()));
            return PagedInfoList.fromCompleteList("workgroups", workGroupInfos, queryParameters);
        }
    }

    @GET
    @Path("{id}/users")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public PagedInfoList getUsersForWorkGroup(@BeanParam JsonQueryParameters queryParameters, @PathParam("id") long id) {
        if(id < 0){
            List<SimplifiedUserInfo> users = new ArrayList<>();
            users.add(0, new SimplifiedUserInfo(-1L, thesaurus.getFormat(BPM_ASSIGNEE_UNASSIGNED).format()));
            return PagedInfoList.fromPagedList("data", users, queryParameters);
        }else {
            WorkGroup workGroup = userService.getWorkGroup(id)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            List<SimplifiedUserInfo> users = workGroup.getUsersInWorkGroup()
                    .stream()
                    .sorted((first, second) -> first.getName().toLowerCase().compareTo(second.getName().toLowerCase()))
                    .map(SimplifiedUserInfo::new)
                    .collect(Collectors.toList());
            users.add(0, new SimplifiedUserInfo(-1L, thesaurus.getFormat(BPM_ASSIGNEE_UNASSIGNED).format()));
            return PagedInfoList.fromPagedList("data", users, queryParameters);
        }
    }

}
