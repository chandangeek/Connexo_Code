package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.nls.Thesaurus;

public class IssueGroupImpl implements IssueGroup {
    private Thesaurus thesaurus;

    private Object groupKey;
    private String groupName;
    private long count;

    public IssueGroupImpl() {}

    public IssueGroupImpl(Thesaurus thesaurus){
        this.thesaurus = thesaurus;
    }

    public IssueGroupImpl init(Object key, String reason, long count) {
        this.groupKey = key;
        this.groupName = reason;
        this.count = count;
        return this;
    }

    public long getCount() {
        return count;
    }

    public String getGroupName() {
        return thesaurus.getStringBeyondComponent(groupName, groupName);
    }

    public Object getGroupKey() {
        return groupKey;
    }
}
