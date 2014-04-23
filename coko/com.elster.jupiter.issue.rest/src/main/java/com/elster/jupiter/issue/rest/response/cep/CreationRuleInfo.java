package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleInfo {
    private long id;
    private String name;
    private String comment;
    private IssueReasonInfo reason;
    private IssueTypeInfo issueType;
    private CreationRuleTemplateInfo template;
    private DueInInfo dueIn;
    private List<CreationRuleActionInfo> actions;
    private Map<String, String> parameters;
    private long creationDate;
    private long modificationDate;
    private long version;

    public CreationRuleInfo() {}

    public CreationRuleInfo(CreationRule rule) {
        if (rule != null) {
            this.id = rule.getId();
            this.name = rule.getName();
            this.comment = rule.getComment();
            this.reason = new IssueReasonInfo(rule.getReason());
            this.issueType = new IssueTypeInfo(rule.getReason().getIssueType());
            if (rule.getDueInType() != null){
                this.dueIn = new DueInInfo(rule.getDueInType().getName(), rule.getDueInValue());
            }
            if (rule.getActions() != null) {
                this.actions = new ArrayList<>();
                for (CreationRuleAction action : rule.getActions()) {
                    actions.add(new CreationRuleActionInfo(action));
                }
            }
            if (rule.getParameters() != null) {
                this.parameters = new HashMap<>();
                for (CreationRuleParameter parameter : rule.getParameters()) {
                    parameters.put(parameter.getKey(), parameter.getValue());
                }
            }
            this.template = new CreationRuleTemplateInfo(rule.getTemplate());
            this.modificationDate = rule.getModTime().getTime();
            this.creationDate = rule.getCreateTime().getTime();
            this.version = rule.getVersion();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public IssueReasonInfo getReason() {
        return reason;
    }

    public void setReason(IssueReasonInfo reason) {
        this.reason = reason;
    }

    public CreationRuleTemplateInfo getTemplate() {
        return template;
    }

    public void setTemplate(CreationRuleTemplateInfo template) {
        this.template = template;
    }

    public IssueTypeInfo getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueTypeInfo issueType) {
        this.issueType = issueType;
    }

    public DueInInfo getDueIn() {
        return dueIn;
    }

    public void setDueIn(DueInInfo dueIn) {
        this.dueIn = dueIn;
    }

    public List<CreationRuleActionInfo> getActions() {
        return actions;
    }

    public void setActions(List<CreationRuleActionInfo> actions) {
        this.actions = actions;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public long getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(long modificationDate) {
        this.modificationDate = modificationDate;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static class DueInInfo {
        private long number;
        private String type;

        public DueInInfo(){}

        private DueInInfo(String type, long number) {
            this.number = number;
            this.type = type;
        }

        public long getNumber() {
            return number;
        }

        public void setNumber(long number) {
            this.number = number;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
