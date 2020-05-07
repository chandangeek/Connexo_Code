/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PriorityInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo.DueInInfo;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleExclGroup;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import java.util.ArrayList;

public class CreationRuleInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;
    private final CreationRuleTemplateInfoFactory templateFactory;
    private final CreationRuleActionInfoFactory actionFactory;
    private final CreationRuleExclGroupInfoFactory exclGroupFactory;

    @Inject
    public CreationRuleInfoFactory(PropertyValueInfoService propertyValueInfoService,
            CreationRuleTemplateInfoFactory templateFactory, CreationRuleActionInfoFactory actionFactory,
            CreationRuleExclGroupInfoFactory exclGroupFactory) {
        this.propertyValueInfoService = propertyValueInfoService;
        this.templateFactory = templateFactory;
        this.actionFactory = actionFactory;
        this.exclGroupFactory = exclGroupFactory;
    }
    
    public CreationRuleInfo asInfo(CreationRule rule) {
        CreationRuleInfo info = asShortInfo(rule);
        info.comment = rule.getComment();
        info.priority = new PriorityInfo(rule.getPriority());
        if (rule.getActions() != null) {
            info.actions = new ArrayList<>();
            for (CreationRuleAction action : rule.getActions()) {
                info.actions.add(actionFactory.asInfo(action));
            }
        }
        if (rule.getExcludedGroupMappings() != null) {
            info.exclGroups = new ArrayList<>();
            for (CreationRuleExclGroup groupMapping : rule.getExcludedGroupMappings()) {
                info.exclGroups.add(exclGroupFactory.asInfo(groupMapping));
            }
        }
        info.properties = propertyValueInfoService.getPropertyInfos(rule.getPropertySpecs(), rule.getProperties());
        info.template = templateFactory.asInfo(rule.getTemplate());
        return info;
    }

    //asShortInfo() was added because asInfo() takes a long time (due to propertyValueInfoService.getPropertyInfos())
    public CreationRuleInfo asShortInfo(CreationRule rule) {
        CreationRuleInfo info = new CreationRuleInfo();
        info.id = rule.getId();
        info.name = rule.getName();
        info.active = rule.isActive();
        info.reason = new IssueReasonInfo(rule.getReason());
        info.issueType = new IssueTypeInfo(rule.getIssueType());
        if (rule.getDueInType() != null) {
            info.dueIn = new DueInInfo(rule.getDueInType().getName(), rule.getDueInValue());
        }
        info.template = templateFactory.asShortInfo(rule.getTemplate());
        info.modificationDate = rule.getModTime().toEpochMilli();
        info.creationDate = rule.getCreateTime().toEpochMilli();
        info.version = rule.getVersion();
        return info;
    }
}
