package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.GroupByReasonEntity;

public class IssueGroupInfo {
    private long id;
    private String reason;
    private long number;

    public IssueGroupInfo(GroupByReasonEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.reason = entity.getReason();
            this.number = entity.getCount();
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

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }
}
