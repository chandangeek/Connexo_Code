package com.elster.jupiter.issue.rest.response;


import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;

public class IssueReasonInfo {
    private long id;
    private String name;

    public IssueReasonInfo(IssueReason reason) {
        this.id = reason.getId();
        this.name = reason.getName();
    }

    public IssueReasonInfo(IssueStatus status) {
        this.id = status.getId();
        this.name = status.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}
