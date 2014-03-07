package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.AssignListInfo;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.rest.util.QueryParameters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/assign")
public class IssueAssignResource extends BaseResource {

    public IssueAssignResource() {
        super();
    }

    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getGroups(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeTeam> query = getIssueMainService().query(AssigneeTeam.class);
        List<AssigneeTeam> list = getQueryService().wrap(query).select(queryParameters);
        return new AssignListInfo(list);
    }

    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getTeams(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeRole> query = getIssueMainService().query(AssigneeRole.class);
        List<AssigneeRole> list = getQueryService().wrap(query).select(queryParameters);
        return new AssignListInfo(list);
    }
}
