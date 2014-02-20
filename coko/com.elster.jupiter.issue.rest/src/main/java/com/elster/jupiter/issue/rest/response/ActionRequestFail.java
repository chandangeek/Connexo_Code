package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.util.collections.ArrayDiffList;

import java.util.ArrayList;
import java.util.List;

public class ActionRequestFail {
    private String reason;
    List<IssueFailInfo> issues;
    private long count;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<IssueFailInfo> getIssues() {
        if (issues == null){
            issues = new ArrayList<IssueFailInfo>();
        }
        return issues;
    }

    public void setIssues(List<IssueFailInfo> issues) {
        this.issues = issues;
    }

    public long getCount() {
        return getIssues() != null ? getIssues().size() : 0;
    }

    public static class IssueFailInfo {
        private long id;
        private String title;

        public IssueFailInfo(long id, String title) {
            this.id = id;
            this.title = title;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
