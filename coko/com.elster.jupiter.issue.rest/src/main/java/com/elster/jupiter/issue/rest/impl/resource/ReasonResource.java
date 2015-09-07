package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getReasons(@BeanParam StandardParametersBean params) {
        validateMandatory(params, ISSUE_TYPE);
        IssueType issueType = getIssueService().findIssueType(params.getFirst(ISSUE_TYPE)).orElse(null);
        Query<IssueReason> query = getIssueService().query(IssueReason.class);
        Condition condition = where("issueType").isEqualTo(issueType);
        List<IssueReason> reasons = query.select(condition);

        if (params.getFirst(LIKE) != null) {
            reasons = reasons.stream().filter(reason -> reason.getName().toLowerCase().contains(params.getFirst(LIKE).toLowerCase())).collect(Collectors.toList());
        }
        return entity(reasons, IssueReasonInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getreasons">Get reasons</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getReason(@PathParam(ID) String key){
        Optional<IssueReason> reasonRef = getIssueService().findReason(key);
        if(!reasonRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return entity(new IssueReasonInfo(reasonRef.get())).build();
    }
}
