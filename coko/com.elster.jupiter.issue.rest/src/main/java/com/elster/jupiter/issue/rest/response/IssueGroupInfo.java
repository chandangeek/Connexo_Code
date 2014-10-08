package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueGroup;

public class IssueGroupInfo {
    public Object id;
    public String reason;
    public long number;

    public IssueGroupInfo(IssueGroup entity) {
        if (entity != null) {
            this.id = entity.getGroupKey();
            this.reason = entity.getGroupName();
            this.number = entity.getCount();
        }
    }
}
