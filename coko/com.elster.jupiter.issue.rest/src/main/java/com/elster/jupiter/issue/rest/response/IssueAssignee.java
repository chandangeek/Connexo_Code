package com.elster.jupiter.issue.rest.response;

public class IssueAssignee {
    private long id;
    private String type;
    private String title;
    private long version;

    public IssueAssignee(com.elster.jupiter.issue.IssueAssignee assignee){
        if (assignee != null) {
            this.setId(assignee.getId());
            this.setType(assignee.getAssigneeType().toString());
            // TODO here should be valid path to title
            this.setTitle(assignee.getAssigneeRef());
            this.setVersion(assignee.getVersion());
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
