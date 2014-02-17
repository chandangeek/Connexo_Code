package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;

public class IssueGroupList {
    private List<IssueGroupListRow> groups;

    public IssueGroupList() {
        this.groups = new ArrayList<>();
    }

    public List<IssueGroupListRow> getGroups() {
        return groups;
    }

    public void add(IssueGroupListRow row) {
        getGroups().add(row);
    }
}
