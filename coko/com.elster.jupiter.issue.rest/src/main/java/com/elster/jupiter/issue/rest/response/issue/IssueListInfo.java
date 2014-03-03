package com.elster.jupiter.issue.rest.response.issue;

import com.elster.jupiter.issue.Issue;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;

import java.util.ArrayList;
import java.util.List;

public class IssueListInfo {
    private List<IssueInfo> issueList;
    private long total;

    public IssueListInfo() {
        issueList = new ArrayList<>();
    }

    public IssueListInfo(List<Issue> allIssues, int start, int limit){
        this();
        if (allIssues != null && allIssues.size() > 0){
            for (Issue issue : allIssues) {
                IssueInfo<DeviceShortInfo> rowIssue = new IssueInfo<>(issue, DeviceShortInfo.class);
                issueList.add(rowIssue);
            }
            total = start + allIssues.size();
            if (allIssues.size() == limit) {
                total++;
            }
        }
    }

    public List<IssueInfo> getIssueList() {
        return issueList;
    }

    public void add(IssueInfo issue) {
        getIssueList().add(issue);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
