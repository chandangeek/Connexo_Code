package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.IssueActionType;

import javax.inject.Inject;

public class IssueActionInfoFactory {

    private final PropertyUtils propertyUtils;

    @Inject
    public IssueActionInfoFactory(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
    }

    public IssueActionTypeInfo asInfo(IssueActionType actionType) {
        IssueActionTypeInfo info = new IssueActionTypeInfo();
        info.id = actionType.getId();
        IssueAction action = actionType.createIssueAction().get();
        info.name = action.getDisplayName();
        info.issueType = new IssueTypeInfo(actionType.getIssueType());
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(action.getPropertySpecs());
        return info;
    }
}
