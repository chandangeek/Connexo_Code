/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import java.util.*;
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllActionTypes(@QueryParam(ISSUE_TYPE) String issueTypeParam,
                                           @QueryParam(REASON) String reasonParam,
                                           @QueryParam(PHASE) String phaseParam,
                                           @QueryParam(RULE_TEMPLATE) String ruleTemplateParam,
                                           @QueryParam(CREATED_ACTIONS) List<Long> createdActionTypeIds,
                                           @BeanParam JsonQueryParameters params) {
        Optional<IssueType> issueType = getIssueService().findIssueType(issueTypeParam);
        Optional<IssueReason> issueReason = getIssueService().findReason(reasonParam);
        Optional<CreationRuleActionPhase> phase = Optional.ofNullable(CreationRuleActionPhase.fromString(phaseParam));
        Query<IssueActionType> query = getIssueActionService().getActionTypeQuery();

        Condition typeCondition = buildCondition("issueType", issueType);
        Condition reasonCondition = buildCondition("issueReason", issueReason);
        Condition phaseCondition = buildCondition("phase", phase);
        Condition condition = (typeCondition).and(reasonCondition).and(phaseCondition);

        List<IssueActionTypeInfo> ruleActionTypes = query.select(condition).stream()
                .filter(issueActionType -> issueActionType.createIssueAction().isPresent() && !createdActionTypeIds.contains(issueActionType.getId()))
                .filter(at -> additionalRestrictionOnActions(at, createdActionTypeIds, issueReason))
                .filter(at -> filterByIssueRuleTemplateId(at, ruleTemplateParam))
                .map(issueActionType -> actionInfoFactory.asInfo(issueActionType, reasonParam, issueType.orElse(null), issueReason.orElse(null)))
                .filter(item -> (!((item.name).equals("Email") && issueTypeParam.equals("usagepointdatavalidation"))))
                .sorted(Comparator.comparing(a -> a.name))
                .collect(Collectors.toList());
        // Fix for CXO-11797
        IssueActionTypeInfo objectToRemove = null;
        boolean foundObject = false;
        if (phaseParam != null && phaseParam.equals("OVERDUE")) {
            for (IssueActionTypeInfo issueActionTypeInfo : ruleActionTypes) {
                if (issueActionTypeInfo.name.equals("Close issue")) {
                    foundObject = true;
                    objectToRemove = issueActionTypeInfo;
                }
            }
            if (foundObject) {
                ruleActionTypes.remove(objectToRemove);
            }
        }
        return PagedInfoList.fromCompleteList("ruleActionTypes", ruleActionTypes, params);
    }

    private Condition buildCondition(String field, Optional<?> value) {
        Condition condition = where(field).isNull();
        if (value.isPresent()) {
            condition = condition.or(where(field).isEqualTo(value.get()));
        }
        return condition;
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

    // TODO: Hardcoded solution for E926, must be reworked in future, we need to have possibility to filter ActionTypes by template
    private boolean filterByIssueRuleTemplateId(final IssueActionType issueActionType, final String issueRuleTemplate) {
        if (Objects.nonNull(issueRuleTemplate)) {
            return !issueRuleTemplate.equalsIgnoreCase("Isu.model.CreationRuleTemplate-SuspectCreationRuleTemplate")
                    || !issueActionType.getClassName().equalsIgnoreCase("com.energyict.mdc.issue.datavalidation.impl.actions.CloseIssueAction");
        }
        return true;
    }

    private boolean additionalRestrictionOnActions(final IssueActionType issueActionType, final List<Long> createdActionTypeIds, Optional<IssueReason> issueReason) {

        final Map<Long, String> createdActionTypeClassNames = getCreatedActionTypeClassNames(createdActionTypeIds);

        final String actionTypeClassName = issueActionType.getClassName();

        if ((actionTypeClassName != null) && actionTypeClassName.equals("com.elster.jupiter.issue.impl.actions.WebServiceNotificationAction")) {
            final boolean anyMatch = createdActionTypeClassNames.containsValue("com.elster.jupiter.issue.impl.actions.ProcessAction");
            return !anyMatch;
        }

        if ((actionTypeClassName != null) && actionTypeClassName.equals("com.elster.jupiter.issue.impl.actions.ProcessAction")) {
            final boolean anyMatch = createdActionTypeClassNames.containsValue("com.elster.jupiter.issue.impl.actions.WebServiceNotificationAction");
            return !anyMatch;
        }

        if ((actionTypeClassName != null) && (actionTypeClassName.equals("com.elster.jupiter.issue.servicecall.impl.action.StartProcessAction")
                || actionTypeClassName.equals("com." +
                "elster.jupiter.webservice.issue.impl.actions.StartProcessWebServiceIssueAction"))) {
            return issueReason.map(reason -> issueActionType.createIssueAction()
                    .map(issueAction -> issueAction.isApplicable(reason.getKey())).orElse(false)).orElse(false);
        }

        return true;
    }

    @GET
    @Path("/phases")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllActionPhases(@BeanParam JsonQueryParameters queryParameters) {
        List<CreationRuleActionPhaseInfo> infos = Arrays.stream(CreationRuleActionPhase.values())
                .filter(p -> !p.getTitleId().equals("IssueActionPhaseNotApplicable")).map(phase -> new CreationRuleActionPhaseInfo(phase, getThesaurus())).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("creationRuleActionPhases", infos, queryParameters);
    }
}
