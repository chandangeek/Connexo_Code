package com.elster.jupiter.issue.rest.request;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseIssueRequest {
    private List<EntityReference> issues;
    private String comment;
    private long status;

    public List<EntityReference> getIssues() {
        return issues;
    }

    public void setIssues(List<EntityReference> issues) {
        this.issues = issues;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }
}
