package com.elster.jupiter.issue.rest.controller;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.AssigneeRole;
import com.elster.jupiter.issue.AssigneeTeam;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.rest.response.AssignListInfo;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/assign")
public class AssignHelpController {

    private final TransactionService transactionService;
    private final IssueService issueService;
    private final RestQueryService restQueryService;

    @Inject
    public AssignHelpController(RestQueryService restQueryService, IssueService issueService, TransactionService transactionService) {
        this.issueService = issueService;
        this.restQueryService = restQueryService;
        this.transactionService = transactionService;
    }

    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getGroups(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeTeam> query = issueService.getAssigneeTeamListQuery();
        List<AssigneeTeam> list = restQueryService.wrap(query).select(queryParameters);
        AssignListInfo result = new AssignListInfo(list);
        return result;
    }

    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getTeams(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeRole> query = issueService.getAssigneeRoleListQuery();
        List<AssigneeRole> list = restQueryService.wrap(query).select(queryParameters);
        AssignListInfo result = new AssignListInfo(list);
        return result;
    }
}
