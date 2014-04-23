package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueStatus;

public class IssueStatusInfo {
    private long id;
    private String name;
    private boolean allowForClosing;

    public IssueStatusInfo(IssueStatus status) {
        if (status != null) {
            this.id = status.getId();
            this.name = status.getName();
            this.allowForClosing = status.isFinal();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAllowForClosing() {
        return allowForClosing;
    }

    public void setAllowForClosing(boolean allowForClosing) {
        this.allowForClosing = allowForClosing;
    }
}
