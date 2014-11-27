package com.elster.jupiter.issue.rest.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseIssueRequest {
    private List<EntityReference> issues;
    private String comment;
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
