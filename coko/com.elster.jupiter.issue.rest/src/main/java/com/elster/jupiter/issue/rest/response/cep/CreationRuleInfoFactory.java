package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PriorityInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo.DueInInfo;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import java.util.ArrayList;

public class CreationRuleInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;
    private final CreationRuleTemplateInfoFactory templateFactory;
    private final CreationRuleActionInfoFactory actionFactory;

    @Inject
    public CreationRuleInfoFactory(PropertyValueInfoService propertyValueInfoService, CreationRuleTemplateInfoFactory templateFactory, CreationRuleActionInfoFactory actionFactory) {
        this.propertyValueInfoService = propertyValueInfoService;
        this.templateFactory = templateFactory;
        this.actionFactory = actionFactory;
    }
    
    public CreationRuleInfo asInfo(CreationRule rule) {
        CreationRuleInfo info = new CreationRuleInfo();
        info.id = rule.getId();
        info.name = rule.getName();
        info.comment = rule.getComment();
        info.reason = new IssueReasonInfo(rule.getReason());
        info.issueType = new IssueTypeInfo(rule.getIssueType());
        info.priority = new PriorityInfo(rule.getPriority());
        if (rule.getDueInType() != null) {
            info.dueIn = new DueInInfo(rule.getDueInType().getName(), rule.getDueInValue());
        }
        if (rule.getActions() != null) {
            info.actions = new ArrayList<>();
            for (CreationRuleAction action : rule.getActions()) {
                info.actions.add(actionFactory.asInfo(action));
            }
        }
        info.properties = propertyValueInfoService.getPropertyInfos(rule.getPropertySpecs(), rule.getProperties());
        info.template = templateFactory.asInfo(rule.getTemplate());
        info.modificationDate = rule.getModTime().toEpochMilli();
        info.creationDate = rule.getCreateTime().toEpochMilli();
        info.version = rule.getVersion();
        return info;
    }
}
