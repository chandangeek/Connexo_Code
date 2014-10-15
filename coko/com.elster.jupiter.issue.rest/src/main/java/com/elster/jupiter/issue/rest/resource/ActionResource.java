package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionPhaseInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionTypeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.REASON;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
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
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getAllActionTypes(@BeanParam StandardParametersBean params){
        String issueTypeKey = params.getFirst(ISSUE_TYPE);
        IssueType issueType = getIssueService().findIssueType(issueTypeKey).orElse(null);

        String issueReasonKey = params.getFirst(REASON);
        IssueReason issueReason = getIssueService().findReason(issueReasonKey).orElse(null);

        Query<IssueActionType> query = getIssueActionService().getActionTypeQuery();
        Condition condition = Condition.TRUE;
        if (issueReason != null){
            condition = condition.and(where("issueReason").isEqualTo(issueReason));
        } else {
            condition = where("issueType").isNull();
            if (issueType != null){
                condition = condition.or(where("issueType").isEqualTo(issueType));
            }
        }
        List<IssueActionType> ruleActionTypes = query.select(condition);
        return entity(ruleActionTypes, CreationRuleActionTypeInfo.class).build();
    }


    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getavailableactionphases">Get available action phases</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/phases")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getAllActionPhases(){
        List<CreationRuleActionPhaseInfo> availablePhases = new ArrayList<>();
        for (CreationRuleActionPhase phase : CreationRuleActionPhase.values()) {
            availablePhases.add(new CreationRuleActionPhaseInfo(phase, getThesaurus()));
        }
        return entity(availablePhases, CreationRuleActionPhaseInfo.class).build();
    }
}
