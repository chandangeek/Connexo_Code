package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueAssignee;

public class IssueAssigneeInfo {
    private String type;
    private Long id;
    private String name;

    public IssueAssigneeInfo(IssueAssignee assignee){
        if (assignee != null) {
            this.setType(assignee.getType().getType());
            switch (assignee.getType()){
                case USER:
                    this.setName(assignee.getUser() != null ? assignee.getUser().getName() : "");
                    break;
                case TEAM:
                    this.setName(assignee.getTeam() != null ? assignee.getTeam().getName() : "");
                    break;
                case ROLE:
                    this.setName(assignee.getRole() != null ? assignee.getRole().getName() : "");
                    break;
            }
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
