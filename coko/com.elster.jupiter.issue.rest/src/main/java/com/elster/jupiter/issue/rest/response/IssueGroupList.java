package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;

public class IssueGroupList {
    private long total;
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

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
