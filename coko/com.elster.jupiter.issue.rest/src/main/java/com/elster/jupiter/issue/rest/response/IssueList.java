package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;

public class IssueList {
    private List<IssueInfo> issueList;

    public IssueList() {
        issueList = new ArrayList<>();
    }

    public List<IssueInfo> getIssueList() {
        return issueList;
    }

    public void add(IssueInfo issue) {
        getIssueList().add(issue);
    }
}
