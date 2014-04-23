package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.ok;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/reasons")
public class ReasonResource extends BaseResource {

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getreasons">Get reasons</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ISSUE_TYPE}'<br />
     * <b>Optional parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#LIKE}'<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReasons(@BeanParam StandardParametersBean params) {
        validateMandatory(params, ISSUE_TYPE);
        IssueType issueType = getIssueService().findIssueType(params.getFirst(ISSUE_TYPE)).orNull();

        Condition condition = where("issueType").isEqualTo(issueType);
        if (params.get(LIKE) != null) {
            String value = "%" + params.getFirst(LIKE) + "%";
            condition = condition.and(where("name").likeIgnoreCase(value));
        }

        Query<IssueReason> query = getIssueService().query(IssueReason.class);
        List<IssueReason> reasons = query.select(condition);
        return ok(reasons, IssueReasonInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getreasons">Get reasons</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReason(@PathParam(ID) long id){
        Optional<IssueReason> reasonRef = getIssueService().findReason(id);
        if(!reasonRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return ok(new IssueReasonInfo(reasonRef.get())).build();
    }
}
