package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueAssignee;

public class IssueAssigneeInfo {
    private String type;
    private Long id;
    private String name;

    public IssueAssigneeInfo(IssueAssignee assignee){
        if (assignee != null && assignee.getType() != null) {
            this.type = assignee.getType();
            this.id = assignee.getId();
            this.name = assignee.getName();
        }
    }

    public IssueAssigneeInfo(String type, Long id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
