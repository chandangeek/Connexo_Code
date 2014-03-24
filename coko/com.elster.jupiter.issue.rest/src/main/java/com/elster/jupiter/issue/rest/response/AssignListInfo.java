package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.AssigneeBaseInformation;

import java.util.ArrayList;
import java.util.List;

public class AssignListInfo {
    private List<AssignInfo> data;

    public AssignListInfo() {
        data = new ArrayList<AssignInfo>();
    }

    public AssignListInfo(List<? extends AssigneeBaseInformation> list) {
        this();
        if (list != null && list.size() > 0){
            for (AssigneeBaseInformation assignee : list){
                data.add(new AssignInfo(assignee));
            }
        }
    }

    public long getTotal() {
        return data.size();
    }

    public List<AssignInfo> getData() {
        return data;
    }

    public void setData(List<AssignInfo> assignees) {
        this.data = assignees;
    }

    public static class AssignInfo {
        private long id;
        private String name;
        private long version;

        public AssignInfo(AssigneeBaseInformation info) {
            if (info != null){
                this.id = info.getId();
                this.name = info.getName();
                this.version = info.getVersion();
            }
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getVersion() {
            return version;
        }

        public void setVersion(long version) {
            this.version = version;
        }
    }
}
