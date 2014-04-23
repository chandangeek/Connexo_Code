package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionTypeInfo;
import com.elster.jupiter.issue.share.entity.CreationRuleActionType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.ok;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/actions")
public class ActionResource extends BaseResource {

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getavailableactiontypes">Get available action types</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ISSUE_TYPE}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllActionTypes(@BeanParam StandardParametersBean params){
        validateMandatory(params, ISSUE_TYPE);
        IssueType issueType = getIssueService().findIssueType(params.getFirst(ISSUE_TYPE)).orNull();
        if (issueType == null) {
            return ok("").build();
        }
        Query<CreationRuleActionType> query = getIssueService().query(CreationRuleActionType.class, IssueType.class);
        Condition condition = where("issueType").isEqualTo(issueType).or(where("issueType").isNull());
        List<CreationRuleActionType> ruleActionTypes = query.select(condition);
        return ok(ruleActionTypes, CreationRuleActionTypeInfo.class).build();
    }
}
