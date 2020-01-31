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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getAllActionTypes(@QueryParam("reason") String reasonParam,
                                           @QueryParam("phase") String phaseParam,
                                           @QueryParam("createdActions") List<Long> createdActionTypeIds,
                                           @BeanParam JsonQueryParameters params) {
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
                .filter(issueActionType -> isStartProcessApplicable(issueReason, issueActionType))
                .filter(issueActionType -> additionalRestrictionOnActions(issueActionType, createdActionTypeIds))
                .map(i -> actionInfoFactory.asInfo(i, issueReason.map(IssueReason::getName).orElse(null), issueType.orElse(null), issueReason.orElse(null)))
            //    .filter(item -> !(phaseParam.equals("OVERDUE") && item.name.equals("Email")))
                .sorted(Comparator.comparing(a -> a.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("ruleActionTypes", ruleActionTypes, params);
    }

    private boolean isStartProcessApplicable(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<IssueReason> issueReason, IssueActionType actionType) {
        return actionType.createIssueAction().map(i -> i.isApplicable(issueReason.map(IssueReason::getName).orElse(null)))
                .orElse(true);
    }

    private String getCreatedActionTypeClassName(final Long createdActionTypeId) {
        final Optional<IssueActionType> issueActionType = getIssueActionService().findActionType(createdActionTypeId);
        return issueActionType.map(IssueActionType::getClassName).orElse(null);
    }

    private Map<Long, String> getCreatedActionTypeClassNames(final List<Long> createdActionTypeIds) {
        final Map<Long, String> resultMap = new HashMap<>();
        createdActionTypeIds.forEach(id -> resultMap.put(id, getCreatedActionTypeClassName(id)));
        return resultMap;
    }

    private boolean additionalRestrictionOnActions(final IssueActionType issueActionType, final List<Long> createdActionTypeIds) {

        final Map<Long, String> createdActionTypeClassNames = getCreatedActionTypeClassNames(createdActionTypeIds);

        final String actionTypeClassName = issueActionType.getClassName();

        if (actionTypeClassName.equals("com.energyict.mdc.device.alarms.impl.actions.WebServiceNotificationAlarmAction")) {
            final boolean anyMatch = createdActionTypeClassNames.containsValue("com.energyict.mdc.device.alarms.impl.actions.StartProcessAlarmAction");
            return !anyMatch;
        }

        if (actionTypeClassName.equals("com.energyict.mdc.device.alarms.impl.actions.StartProcessAlarmAction")) {
            final boolean anyMatch = createdActionTypeClassNames.containsValue("com.energyict.mdc.device.alarms.impl.actions.WebServiceNotificationAlarmAction");
            return !anyMatch;
        }

        return true;
    }

    @GET
    @Path("/phases")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
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