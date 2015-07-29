package com.elster.jupiter.issue.rest.response.cep;

import java.util.Collections;
import java.util.List;

import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
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
    public List<CreationRuleActionInfo> actions = Collections.emptyList();
    public List<PropertyInfo> properties = Collections.emptyList();
    public long creationDate;
    public long modificationDate;
    public long version;

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
