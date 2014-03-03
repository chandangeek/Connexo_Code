package com.elster.jupiter.issue.rest.controller;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.AssigneeRole;
import com.elster.jupiter.issue.AssigneeTeam;
import com.elster.jupiter.issue.rest.response.AssignListInfo;
import com.elster.jupiter.rest.util.QueryParameters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/assign")
public class IssueAssignController extends BaseController {

    public IssueAssignController() { 
        super();
    }

    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getGroups(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeTeam> query = getIssueService().getAssigneeTeamListQuery();
        List<AssigneeTeam> list = getQueryService().wrap(query).select(queryParameters);
        return new AssignListInfo(list);
    }

    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getTeams(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeRole> query = getIssueService().getAssigneeRoleListQuery();
        List<AssigneeRole> list = getQueryService().wrap(query).select(queryParameters);
        return new AssignListInfo(list);
    }
}
