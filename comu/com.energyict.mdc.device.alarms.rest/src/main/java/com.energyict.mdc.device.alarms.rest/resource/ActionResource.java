/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.entity.CreationRuleActionPhase;
import com.energyict.mdc.device.alarms.rest.response.CreationRuleActionPhaseInfo;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/actions")
public class ActionResource extends BaseAlarmResource {

    private final IssueActionInfoFactory actionInfoFactory;

    @Inject
    public ActionResource(IssueActionInfoFactory actionInfoFactory) {
        this.actionInfoFactory = actionInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getAllActionTypes(@QueryParam("reason") String reasonParam,
                                           @QueryParam("phase") String phaseParam,
                                           @QueryParam("createdActions") List<Long> createdActionTypeIds,
                                           @BeanParam JsonQueryParameters params){
        Optional<IssueType> issueType = getIssueService().findIssueType("devicealarm");
        Optional<IssueReason> issueReason = getIssueService().findReason(reasonParam);
        Optional<CreationRuleActionPhase> phase = Optional.ofNullable(CreationRuleActionPhase.fromString(phaseParam));
        Query<IssueActionType> query = getIssueActionService().getActionTypeQuery();

        Condition typeCondition = buildCondition("issueType", issueType);
        Condition reasonCondition = buildCondition("issueReason", issueReason);
        Condition phaseCondition = buildCondition("phase", phase);
        Condition condition = typeCondition.and(reasonCondition).and(phaseCondition);

        List<IssueActionTypeInfo> ruleActionTypes = query.select(condition).stream()
                .filter(issueActionType -> issueActionType.getIssueType() != null)
                .filter(at -> at.createIssueAction().isPresent() && !createdActionTypeIds.contains(at.getId()))
                .map(actionInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("ruleActionTypes", ruleActionTypes, params);
    }


    @GET
    @Path("/phases")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getAllActionPhases(@BeanParam JsonQueryParameters queryParameters) {
        List<CreationRuleActionPhaseInfo> infos = Arrays.asList(CreationRuleActionPhase.values()).stream().map(phase -> new CreationRuleActionPhaseInfo(phase, getThesaurus())).collect(Collectors
                .toList());
        return PagedInfoList.fromCompleteList("creationRuleActionPhases", infos, queryParameters);
    }

    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
    }
}
