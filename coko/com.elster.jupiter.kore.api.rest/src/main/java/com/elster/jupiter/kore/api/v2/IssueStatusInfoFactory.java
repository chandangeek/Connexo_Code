package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.issue.share.entity.IssueStatus;

public class IssueStatusInfoFactory {

    public IssueStatusInfo asInfo(IssueStatus status) {
        IssueStatusInfo info = new IssueStatusInfo();
        info.id = status.getKey();
        info.name = status.getName();
        info.allowForClosing = status.isHistorical();
        return info;
    }


}
