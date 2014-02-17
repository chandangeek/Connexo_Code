package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;

public class IssueList {
    private List<IssueListRow> issueList;

    public IssueList() {
        issueList = new ArrayList<>();
    }

    public List<IssueListRow> getIssueList() {
        return issueList;
    }

    public void add(IssueListRow issue) {
        getIssueList().add(issue);
    }
}
