package com.elster.jupiter.issue.rest.response;


import com.elster.jupiter.issue.Issue;

public class IssueListRow {
    private long id;
    private String reason;
    private String status;
    private long dueDate;
    private IssueAssignee assignee;
    private long version;

    public IssueListRow(Issue issue){
        if (issue != null) {
            this.setId(issue.getId());
            this.setReason(issue.getTitle());
            this.setStatus(issue.getStatus().toString());
            this.setDueDate(issue.getDueDate().getTime());
            this.setAssignee(new IssueAssignee(issue.getAssignee()));
            this.setVersion(issue.getVersion());
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public IssueAssignee getAssignee() {
        return assignee;
    }

    public void setAssignee(IssueAssignee assignee) {
        this.assignee = assignee;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
