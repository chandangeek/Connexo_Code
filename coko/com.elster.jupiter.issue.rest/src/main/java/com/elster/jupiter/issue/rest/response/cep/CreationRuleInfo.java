/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PriorityInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationRuleInfo {
    public long id;
    public String name;
    public boolean active;
    public String comment;
    public IssueReasonInfo reason;
    public IssueTypeInfo issueType;
    public CreationRuleTemplateInfo template;
    public PriorityInfo priority;
    public DueInInfo dueIn;
    public List<CreationRuleActionInfo> actions = Collections.emptyList();
    public List<PropertyInfo> properties = Collections.emptyList();
    public List<CreationRuleExclGroupInfo> exclGroups = Collections.emptyList();
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
