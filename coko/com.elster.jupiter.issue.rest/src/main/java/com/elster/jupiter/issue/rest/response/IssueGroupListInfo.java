package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IssueGroupListInfo {
    private long total;
    private List<IssueGroupInfo> groups;

    public IssueGroupListInfo() {
        this.groups = new ArrayList<>();
    }

    public IssueGroupListInfo(Map<String, Long> groups, int start, int limit){
        this();
        for(Map.Entry<String, Long> groupEntry : groups.entrySet()) {
            IssueGroupInfo groupInfo = new IssueGroupInfo();
            groupInfo.setReason(groupEntry.getKey());
            groupInfo.setNumber(groupEntry.getValue());
            this.groups.add(groupInfo);
        }
        this.total = start + groups.size();
        if (groups.size() == limit){
            this.total++;
        }
    }

    public List<IssueGroupInfo> getGroups() {
        return groups;
    }

    public void add(IssueGroupInfo row) {
        getGroups().add(row);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
