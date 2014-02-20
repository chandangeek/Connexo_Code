package com.elster.jupiter.issue.rest.request;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignIssueRequest {
    private List<EntityReference> issues;
    private String comment;
    private AssigneeReference assignee;
    private boolean force;

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

    public AssigneeReference getAssignee() {
        return assignee;
    }

    public void setAssignee(AssigneeReference assignee) {
        this.assignee = assignee;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssigneeReference {
        private String type;
        private long id;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
}
