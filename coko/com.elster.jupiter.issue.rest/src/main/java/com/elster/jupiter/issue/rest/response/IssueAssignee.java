package com.elster.jupiter.issue.rest.response;

public class IssueAssignee {
    private String type;
    private String title;

    public IssueAssignee(com.elster.jupiter.issue.IssueAssignee assignee){
        if (assignee != null) {
            this.setType(assignee.getType().name());
            switch (assignee.getType()){
                case USER:
                    this.setTitle(assignee.getUser() != null ? assignee.getUser().getName() : "");
                    break;
                case TEAM:
                    this.setTitle(assignee.getTeam() != null ? assignee.getTeam().getName() : "");
                    break;
                case ROLE:
                    this.setTitle(assignee.getRole() != null ? assignee.getRole().getName() : "");
                    break;
            }
        }
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
}
