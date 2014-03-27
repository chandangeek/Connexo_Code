package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueStatusListInfo;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.rest.util.QueryParameters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/statuses")
public class StatusResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public IssueStatusListInfo getStatuses(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<IssueStatus> query = getIssueService().query(IssueStatus.class);
        List<IssueStatus> list = getQueryService().wrap(query).select(queryParameters);
        return new IssueStatusListInfo(list);
    }
}
