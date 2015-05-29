package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueTypeInfo {
    public String uid;
    public String name;

    public IssueTypeInfo() {}

    public IssueTypeInfo(IssueType type) {
        if (type != null) {
            this.uid = type.getKey();
            this.name = type.getName();
        }
    }
}
