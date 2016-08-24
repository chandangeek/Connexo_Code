package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.properties.PropertyValueInfoService;

import javax.inject.Inject;

public class IssueActionInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public IssueActionInfoFactory(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public IssueActionTypeInfo asInfo(IssueActionType actionType) {
        IssueActionTypeInfo info = new IssueActionTypeInfo();
        info.id = actionType.getId();
        IssueAction action = actionType.createIssueAction().get();
        info.name = action.getDisplayName();
        info.issueType = new IssueTypeInfo(actionType.getIssueType());
        info.properties = propertyValueInfoService.getPropertyInfos(action.getPropertySpecs());
        return info;
    }
}
