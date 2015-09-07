package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionPhaseInfo;
import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/actions")
public class ActionResource extends BaseResource {
    
    private final IssueActionInfoFactory actionInfoFactory;
    
    @Inject
    public ActionResource(IssueActionInfoFactory actionInfoFactory) {
        this.actionInfoFactory = actionInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
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
        
        List<IssueActionTypeInfo> ruleActionTypes = query.select(condition).stream()
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
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllActionPhases(@BeanParam JsonQueryParameters queryParameters) {
        List<CreationRuleActionPhaseInfo> infos = Arrays.asList(CreationRuleActionPhase.values()).stream().map(phase -> new CreationRuleActionPhaseInfo(phase, getThesaurus())).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("creationRuleActionPhases", infos, queryParameters);
    }
}
