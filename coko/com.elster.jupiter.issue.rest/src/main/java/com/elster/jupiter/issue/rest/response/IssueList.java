package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.Issue;

import java.util.ArrayList;
import java.util.List;

public class IssueList {
    private List<IssueInfo> issueList;
    private long total;

    public IssueList() {
        issueList = new ArrayList<>();
    }

    public IssueList(List<Issue> allIssues, int start, int limit){
        this();
        if (allIssues != null && allIssues.size() > 0){
            for (Issue issue : allIssues) {
                IssueInfo rowIssue = new IssueInfo(issue);
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
