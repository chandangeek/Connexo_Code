package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueStatus;

import java.util.ArrayList;
import java.util.List;

public class IssueStatusListInfo {
    private List<IssueStatusInfo> data;

    public IssueStatusListInfo() {
        this.data = new ArrayList<>();
    }

    public IssueStatusListInfo(List<IssueStatus> list) {
        this();
        for (IssueStatus status : list) {
            data.add(new IssueStatusInfo(status));
        }
    }

    public List<IssueStatusInfo> getData() {
        return data;
    }

    public void setData(List<IssueStatusInfo> statuses) {
        this.data = statuses;
    }
}
