package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.util.time.UtcInstant;

public class Issue extends BaseIssue {

    public Issue copy(Issue issue){
        this.id = issue.id;
        if (issue.getDueDate() != null) {
            this.setDueDate(new UtcInstant(issue.getDueDate().getTime()));
        }
        this.setReason(issue.getReason());
        this.setStatus(issue.getStatus());
        this.setAssignee(IssueAssignee.class.cast(issue.getAssignee()));
        this.setDevice(issue.getDevice());
        this.setDevice(issue.getDevice());
        return this;
    }
}
