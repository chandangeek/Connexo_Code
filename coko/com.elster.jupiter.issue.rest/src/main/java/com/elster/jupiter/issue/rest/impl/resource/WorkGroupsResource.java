/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.SimplifiedUserInfo;
import com.elster.jupiter.issue.rest.response.WorkGroupInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.users.WorkGroup;

import javax.annotation.security.RolesAllowed;
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

import static com.elster.jupiter.issue.rest.TranslationKeys.ISSUE_ASSIGNEE_UNASSIGNED;

@Path("/workgroups")
public class WorkGroupsResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllWorkGroupAssignees(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        if (params.getUriInfo().getQueryParameters().getFirst("myworkgroups") != null && params.getUriInfo()
                .getQueryParameters()
                .getFirst("myworkgroups")
                .equals("true")) {
            return PagedInfoList.fromCompleteList("workgroups", getUserService().getWorkGroups()
                    .stream()
                    .filter(workGroup -> workGroup.getUsersInWorkGroup()
                            .stream()
                            .anyMatch(user -> user.equals(securityContext.getUserPrincipal())))
                    .map(WorkGroupInfo::new)
                    .sorted((first, second) -> first.name.toLowerCase().compareTo(second.name.toLowerCase()))
                    .collect(Collectors.toList()), queryParameters);
        } else {
            List<WorkGroupInfo> workGroupInfos = getUserService().getWorkGroups()
                    .stream()
                    .map(WorkGroupInfo::new)
                    .sorted((first, second) -> first.name.toLowerCase().compareTo(second.name.toLowerCase()))
                    .collect(Collectors.toList());
            workGroupInfos.add(0, new WorkGroupInfo(getThesaurus().getFormat(ISSUE_ASSIGNEE_UNASSIGNED).format()));
            return PagedInfoList.fromCompleteList("workgroups", workGroupInfos, queryParameters);
        }
    }

    @GET
    @Path("{id}/users")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getUsersForWorkGroup(@BeanParam JsonQueryParameters queryParameters, @PathParam("id") long id) {
        if(id < 0){
            List<SimplifiedUserInfo> users = new ArrayList<>();
            users.add(0, new SimplifiedUserInfo(-1L, getThesaurus().getFormat(ISSUE_ASSIGNEE_UNASSIGNED).format()));
            return PagedInfoList.fromPagedList("data", users, queryParameters);
        }else {
            WorkGroup workGroup = getUserService().getWorkGroup(id)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            List<SimplifiedUserInfo> users = workGroup.getUsersInWorkGroup()
                    .stream()
                    .sorted((first, second) -> first.getName().toLowerCase().compareTo(second.getName().toLowerCase()))
                    .map(SimplifiedUserInfo::new)
                    .collect(Collectors.toList());
            users.add(0, new SimplifiedUserInfo(-1L, getThesaurus().getFormat(ISSUE_ASSIGNEE_UNASSIGNED).format()));
            return PagedInfoList.fromPagedList("data", users, queryParameters);
        }
    }
}
