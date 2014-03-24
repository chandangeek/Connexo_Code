package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.GroupByReasonEntity;

import java.util.ArrayList;
import java.util.List;

public class IssueGroupListInfo {
    private long total;
    private List<IssueGroupInfo> data;

    public IssueGroupListInfo() {
        this.data = new ArrayList<>();
    }

    public IssueGroupListInfo(List<GroupByReasonEntity> list, int start, int limit){
        this();
        for(GroupByReasonEntity groupEntry : list) {
            IssueGroupInfo groupInfo = new IssueGroupInfo();
            groupInfo.setId(groupEntry.getId());
            groupInfo.setReason(groupEntry.getReason());
            groupInfo.setNumber(groupEntry.getCount());
            this.data.add(groupInfo);
        }
        this.total = start + list.size();
        if (list.size() == limit){
            this.total++;
        }
    }

    public List<IssueGroupInfo> getData() {
        return data;
    }

    public void add(IssueGroupInfo row) {
        getData().add(row);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
