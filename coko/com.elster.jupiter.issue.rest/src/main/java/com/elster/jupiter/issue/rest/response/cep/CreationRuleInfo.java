package com.elster.jupiter.issue.rest.response.cep;

import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleInfo {
    public long id;
    public String name;
    public String comment;
    public IssueReasonInfo reason;
    public IssueTypeInfo issueType;
    public CreationRuleTemplateInfo template;
    public DueInInfo dueIn;
    public List<CreationRuleActionInfo> actions;
    public List<PropertyInfo> properties;
    public long creationDate;
    public long modificationDate;
    public long version;

    public CreationRuleInfo() {
    }

    public CreationRuleInfo(CreationRule rule) {
        this.id = rule.getId();
        this.name = rule.getName();
        this.comment = rule.getComment();
        this.reason = new IssueReasonInfo(rule.getReason());
        this.issueType = new IssueTypeInfo(rule.getReason().getIssueType());
        if (rule.getDueInType() != null) {
            this.dueIn = new DueInInfo(rule.getDueInType().getName(), rule.getDueInValue());
        }
        if (rule.getActions() != null) {
            this.actions = new ArrayList<>();
            for (CreationRuleAction action : rule.getActions()) {
                actions.add(new CreationRuleActionInfo(action));
            }
        }
        this.properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(rule.getPropertySpecs(), rule.getProps());
        this.template = new CreationRuleTemplateInfo(rule.getTemplate());
        this.modificationDate = rule.getModTime().toEpochMilli();
        this.creationDate = rule.getCreateTime().toEpochMilli();
        this.version = rule.getVersion();
    }

    public static class DueInInfo {
        public long number;
        public String type;

        public DueInInfo() {
        }

        public DueInInfo(String type, long number) {
            this.number = number;
            this.type = type;
        }
    }
}
