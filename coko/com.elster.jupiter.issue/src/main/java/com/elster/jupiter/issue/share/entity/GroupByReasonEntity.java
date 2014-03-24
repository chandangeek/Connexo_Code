package com.elster.jupiter.issue.share.entity;

public class GroupByReasonEntity {
    private long id;
    private String reason;
    private long count;

    public GroupByReasonEntity() {

    }

    public GroupByReasonEntity(long id, String reason, long count) {
        this.id = id;
        this.reason = reason;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public String getReason() {
        return reason;
    }

    public long getId() {
        return id;
    }
}
