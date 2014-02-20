package com.elster.jupiter.issue.rest.response;

import java.util.List;

public class BaseActionResponse {
    private List<Long> success;
    private List<ActionRequestFail> failure;

    public List<Long> getSuccess() {
        return success;
    }

    public void setSuccess(List<Long> success) {
        this.success = success;
    }

    public List<ActionRequestFail> getFailure() {
        return failure;
    }

    public void setFailure(List<ActionRequestFail> failure) {
        this.failure = failure;
    }
}
