package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueReason;

import java.util.ArrayList;
import java.util.List;

public class IssueReasonListInfo {
    private List<IssueReasonInfo> data;

    public IssueReasonListInfo() {
        this.data = new ArrayList<>();
    }

    public IssueReasonListInfo(List<IssueReason> list) {
        this();
        for (IssueReason reason : list) {
            data.add(new IssueReasonInfo(reason));
        }
    }

    public List<IssueReasonInfo> getData() {
        return data;
    }

    public void setData(List<IssueReasonInfo> reasons) {
        this.data = reasons;
    }
}
