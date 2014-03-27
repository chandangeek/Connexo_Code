package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueReasonListInfo;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.util.conditions.Condition;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/reasons")
public class ReasonResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public IssueReasonListInfo getReasons(@Context UriInfo uriInfo) {
        Condition condition = Condition.TRUE;
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        if (queryParameters.get("like") != null) {
            String value = "%" + queryParameters.get("like").get(0) + "%";
            condition = where("name").likeIgnoreCase(value);
        }

        Query<IssueReason> query = getIssueService().query(IssueReason.class);
        List<IssueReason> list = getQueryService().wrap(query).select(queryParameters, condition);
        return new IssueReasonListInfo(list);
    }
}
