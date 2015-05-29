package com.elster.jupiter.issue.rest.impl.resource;

import static com.elster.jupiter.issue.rest.request.RequestHelper.CREATED_ACTIONS;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.PHASE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.REASON;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.util.conditions.Where.where;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionPhaseInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionTypeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;

@Path("/actions")
public class ActionResource extends BaseResource {

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getavailableactiontypes">Get available action types</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ISSUE_TYPE}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getAllActionTypes(@BeanParam StandardParametersBean params){
        String issueTypeKey = params.getFirst(ISSUE_TYPE);
        Optional<IssueType> issueType = getIssueService().findIssueType(issueTypeKey);

        String issueReasonKey = params.getFirst(REASON);
        Optional<IssueReason> issueReason = getIssueService().findReason(issueReasonKey);
        
        String phaseKey = params.getFirst(PHASE);
        Optional<CreationRuleActionPhase> phase = Optional.ofNullable(CreationRuleActionPhase.fromString(phaseKey));

        List<Long> createdActionTypeIds = params.get(CREATED_ACTIONS).stream().map(Long::valueOf).collect(Collectors.toList());

        Query<IssueActionType> query = getIssueActionService().getActionTypeQuery();

        Condition typeCondition = buildCondition("issueType", issueType);
        Condition reasonCondition = buildCondition("issueReason", issueReason);
        Condition phaseCondition = buildCondition("phase", phase);
        Condition condition = (typeCondition).and(reasonCondition).and(phaseCondition);
        
        List<IssueActionType> ruleActionTypes = query.select(condition).stream()
                .filter(at -> at.createIssueAction().isPresent() && !createdActionTypeIds.contains(at.getId()))
                .collect(Collectors.toList());
        return entity(ruleActionTypes, CreationRuleActionTypeInfo.class).build();
    }
    
    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getavailableactionphases">Get available action phases</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/phases")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getAllActionPhases(){
        List<CreationRuleActionPhaseInfo> availablePhases = new ArrayList<>();
        for (CreationRuleActionPhase phase : CreationRuleActionPhase.values()) {
            availablePhases.add(new CreationRuleActionPhaseInfo(phase, getThesaurus()));
        }
        return entity(availablePhases, CreationRuleActionPhaseInfo.class).build();
    }
}
