package com.elster.jupiter.issue.rest.response;

import java.util.List;

public class BaseActionResponse {
    private List<IssueShortInfo> success;
    private List<ActionRequestFail> failure;

    public List<IssueShortInfo> getSuccess() {
        return success;
    }

    public void setSuccess(List<IssueShortInfo> success) {
        this.success = success;
    }

    public List<ActionRequestFail> getFailure() {
        return failure;
    }

    public void setFailure(List<ActionRequestFail> failure) {
        this.failure = failure;
    }
}
