package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;

public class ActionRequestFail {
    private String reason;
    private List<IssueShortInfo> issues;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<IssueShortInfo> getIssues() {
        if (issues == null){
            issues = new ArrayList<IssueShortInfo>();
        }
        return issues;
    }

    public void setIssues(List<IssueShortInfo> issues) {
        this.issues = issues;
    }

}
