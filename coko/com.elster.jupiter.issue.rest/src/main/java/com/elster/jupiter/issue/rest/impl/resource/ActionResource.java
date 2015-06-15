package com.elster.jupiter.issue.rest.impl.resource;

import static com.elster.jupiter.issue.rest.request.RequestHelper.CREATED_ACTIONS;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.PHASE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.REASON;
import static com.elster.jupiter.util.conditions.Where.where;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionPhaseInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionTypeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.conditions.Condition;

@Path("/actions")
public class ActionResource extends BaseResource {
    
    private final CreationRuleActionInfoFactory actionInfoFactory;
    
    @Inject
    public ActionResource(CreationRuleActionInfoFactory actionInfoFactory) {
        this.actionInfoFactory = actionInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public PagedInfoList getAllActionTypes(@QueryParam(ISSUE_TYPE) String issueTypeParam,
            @QueryParam(REASON) String reasonParam,
            @QueryParam(PHASE) String phaseParam,
            @QueryParam(CREATED_ACTIONS) List<Long> createdActionTypeIds,
            @BeanParam JsonQueryParameters params){
        Optional<IssueType> issueType = getIssueService().findIssueType(issueTypeParam);
        Optional<IssueReason> issueReason = getIssueService().findReason(reasonParam);
        Optional<CreationRuleActionPhase> phase = Optional.ofNullable(CreationRuleActionPhase.fromString(phaseParam));
        Query<IssueActionType> query = getIssueActionService().getActionTypeQuery();

        Condition typeCondition = buildCondition("issueType", issueType);
        Condition reasonCondition = buildCondition("issueReason", issueReason);
        Condition phaseCondition = buildCondition("phase", phase);
        Condition condition = (typeCondition).and(reasonCondition).and(phaseCondition);
        
        List<CreationRuleActionTypeInfo> ruleActionTypes = query.select(condition).stream()
                .filter(at -> at.createIssueAction().isPresent() && !createdActionTypeIds.contains(at.getId()))
                .map(actionInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("ruleActionTypes", ruleActionTypes, params);
    }
    
    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
    }

    @GET
    @Path("/phases")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public PagedInfoList getAllActionPhases(@BeanParam JsonQueryParameters queryParameters) {
        List<CreationRuleActionPhaseInfo> infos = Arrays.asList(CreationRuleActionPhase.values()).stream().map(phase -> new CreationRuleActionPhaseInfo(phase, getThesaurus())).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("creationRuleActionPhases", infos, queryParameters);
    }
}
