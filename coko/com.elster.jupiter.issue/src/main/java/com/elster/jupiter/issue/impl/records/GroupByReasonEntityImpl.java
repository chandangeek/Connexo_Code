package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.GroupByReasonEntity;

public class GroupByReasonEntityImpl implements GroupByReasonEntity {
    private long id;
    private String reason;
    private long count;

    public GroupByReasonEntityImpl() {

    }

    public GroupByReasonEntityImpl(long id, String reason, long count) {
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
