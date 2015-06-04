package com.elster.jupiter.issue.rest.response.cep;

import java.util.ArrayList;

import javax.inject.Inject;

import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfo.DueInInfo;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;

public class CreationRuleInfoFactory {

    private final PropertyUtils propertyUtils;
    private final CreationRuleTemplateInfoFactory templateFactory;
    private final CreationRuleActionInfoFactory actionFactory;

    @Inject
    public CreationRuleInfoFactory(PropertyUtils propertyUtils, CreationRuleTemplateInfoFactory templateFactory, CreationRuleActionInfoFactory actionFactory) {
        this.propertyUtils = propertyUtils;
        this.templateFactory = templateFactory;
        this.actionFactory = actionFactory;
    }
    
    public CreationRuleInfo asInfo(CreationRule rule) {
        CreationRuleInfo info = new CreationRuleInfo();
        info.id = rule.getId();
        info.name = rule.getName();
        info.comment = rule.getComment();
        info.reason = new IssueReasonInfo(rule.getReason());
        info.issueType = new IssueTypeInfo(rule.getReason().getIssueType());
        if (rule.getDueInType() != null) {
            info.dueIn = new DueInInfo(rule.getDueInType().getName(), rule.getDueInValue());
        }
        if (rule.getActions() != null) {
            info.actions = new ArrayList<>();
            for (CreationRuleAction action : rule.getActions()) {
                info.actions.add(actionFactory.asInfo(action));
            }
        }
        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(rule.getPropertySpecs(), rule.getProperties());
        info.template = templateFactory.asInfo(rule.getTemplate());
        info.modificationDate = rule.getModTime().toEpochMilli();
        info.creationDate = rule.getCreateTime().toEpochMilli();
        info.version = rule.getVersion();
        return info;
    }
}
