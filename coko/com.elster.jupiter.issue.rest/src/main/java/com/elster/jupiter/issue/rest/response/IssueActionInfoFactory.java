/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import aQute.bnd.annotation.ProviderType;

import javax.inject.Inject;
import java.util.List;

@ProviderType
public class IssueActionInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public IssueActionInfoFactory(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public IssueActionTypeInfo asInfo(Issue issue, IssueActionType actionType) {
        IssueAction action = actionType.createIssueAction().get();
        return createInfo(issue, actionType, action);

    }

    public IssueActionTypeInfo asInfo(Issue issue, IssueActionType actionType, String reasonName) {
        IssueAction action = actionType.createIssueAction().get().setReasonName(reasonName);
        return createInfo(issue, actionType, action);
    }

    public IssueActionTypeInfo asInfo(IssueActionType actionType) {
        return asInfo(null, actionType);
    }

    public IssueActionTypeInfo asInfo(IssueActionType actionType, String reasonName) {
        return asInfo(null, actionType, reasonName);
    }

    private IssueActionTypeInfo createInfo(Issue issue, IssueActionType actionType, IssueAction action){
        IssueActionTypeInfo info = new IssueActionTypeInfo();
        info.id = actionType.getId();
        info.name = action.getDisplayName();
        info.issueType = new IssueTypeInfo(actionType.getIssueType());
        action.setIssue(issue);
        List<PropertySpec> propertySpecs = action.getPropertySpecs();
        info.properties = propertySpecs != null && !propertySpecs.isEmpty() ? propertyValueInfoService.getPropertyInfos(propertySpecs) : null;
        return info;
    }
}
