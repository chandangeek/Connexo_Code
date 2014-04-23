package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueTypeInfo {
    private String uid;
    private String name;

    public IssueTypeInfo() {}

    public IssueTypeInfo(IssueType type) {
        if (type != null) {
            this.uid = type.getUUID();
            this.name = type.getName();
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
