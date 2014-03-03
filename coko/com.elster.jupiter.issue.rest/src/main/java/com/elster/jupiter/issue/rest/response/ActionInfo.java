package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;

import java.util.List;

public class ActionInfo {
    private List<IssueShortInfo> success;
    private List<ActionFailInfo> failure;

    public List<IssueShortInfo> getSuccess() {
        return success;
    }

    public void setSuccess(List<IssueShortInfo> success) {
        this.success = success;
    }

    public List<ActionFailInfo> getFailure() {
        return failure;
    }

    public void setFailure(List<ActionFailInfo> failure) {
        this.failure = failure;
    }
}
