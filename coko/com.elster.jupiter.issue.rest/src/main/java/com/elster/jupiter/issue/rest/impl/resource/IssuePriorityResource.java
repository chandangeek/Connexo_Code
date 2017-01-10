package com.elster.jupiter.issue.rest.impl.resource;


import com.elster.jupiter.issue.rest.response.PriorityInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/{id}/priority")
public class IssuePriorityResource extends BaseResource{


    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response setPriority(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, PriorityInfo priorityInfo) {
        Issue issue = getIssueService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        issue.setPriority(Priority.get(priorityInfo.urgency, priorityInfo.impact));
        issue.update();
        return Response.ok().build();
    }

}
