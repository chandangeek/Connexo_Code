package com.elster.jupiter.issue.rest.response.cep;

import javax.inject.Inject;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.IssueActionType;

public class CreationRuleActionInfoFactory {

    private final PropertyUtils propertyUtils;

    @Inject
    public CreationRuleActionInfoFactory(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
    }

    public CreationRuleActionInfo asInfo(CreationRuleAction action) {
        CreationRuleActionInfo info = new CreationRuleActionInfo();
        info.phase = new CreationRuleActionPhaseInfo(action.getPhase());
        info.type = asInfo(action.getAction());
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(action.getPropertySpecs(), action.getProperties());
        return info;
    }

    public CreationRuleActionTypeInfo asInfo(IssueActionType actionType) {
        CreationRuleActionTypeInfo info = new CreationRuleActionTypeInfo();
        info.id = actionType.getId();
        IssueAction action = actionType.createIssueAction().get();
        info.name = action.getDisplayName();
        info.issueType = new IssueTypeInfo(actionType.getIssueType());
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(action.getPropertySpecs());
        return info;
    }

}
