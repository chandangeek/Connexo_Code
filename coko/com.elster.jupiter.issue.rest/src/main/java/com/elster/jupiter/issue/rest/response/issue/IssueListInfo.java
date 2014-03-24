package com.elster.jupiter.issue.rest.response.issue;

import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.issue.share.entity.BaseIssue;

import java.util.ArrayList;
import java.util.List;

public class IssueListInfo {
    private List<IssueInfo> data;
    private long total;

    public IssueListInfo() {
        data = new ArrayList<>();
    }

    public IssueListInfo(List<? extends BaseIssue> allIssues, int start, int limit) {
        this();
        if (allIssues != null && allIssues.size() > 0){
            for (BaseIssue issue : allIssues) {
                IssueInfo<DeviceShortInfo> rowIssue = new IssueInfo<>(issue, DeviceShortInfo.class);
                data.add(rowIssue);
            }
            total = start + allIssues.size();
            if (allIssues.size() == limit) {
                total++;
            }
        }
    }

    public List<IssueInfo> getData() {
        return data;
    }

    public void add(IssueInfo issue) {
        getData().add(issue);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
