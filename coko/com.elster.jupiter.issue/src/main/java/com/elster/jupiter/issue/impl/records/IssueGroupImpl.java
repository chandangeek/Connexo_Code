package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.IssueGroup;

public class IssueGroupImpl implements IssueGroup {
    private Object groupKey;
    private String groupName;
    private long count;

    public IssueGroupImpl() {}

    public IssueGroupImpl(Object key, String reason, long count) {
        this.groupKey = key;
        this.groupName = reason;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public String getGroupName() {
        return groupName;
    }

    public Object getGroupKey() {
        return groupKey;
    }
}
