package com.elster.jupiter.issue.rest.response;

import java.util.List;

public class BaseActionResponse {
    private long[] success;
    private List<ActionRequestFail> failure;

    public long[] getSuccess() {
        return success;
    }

    public void setSuccess(long[] success) {
        this.success = success;
    }

    public List<ActionRequestFail> getFailure() {
        return failure;
    }

    public void setFailure(List<ActionRequestFail> failure) {
        this.failure = failure;
    }
}
