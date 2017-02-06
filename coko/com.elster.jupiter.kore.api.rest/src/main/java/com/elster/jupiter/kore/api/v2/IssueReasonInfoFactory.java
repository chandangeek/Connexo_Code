package com.elster.jupiter.kore.api.v2;


import com.elster.jupiter.issue.share.entity.IssueReason;

public class IssueReasonInfoFactory {
    public IssueReasonInfo asInfo(IssueReason reason) {
        IssueReasonInfo info = new IssueReasonInfo();
        info.id = reason.getKey();
        info.name = reason.getName();
        return info;
    }

}
