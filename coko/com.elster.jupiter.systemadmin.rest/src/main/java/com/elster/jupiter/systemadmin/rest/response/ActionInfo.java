package com.elster.jupiter.systemadmin.rest.response;

import java.util.LinkedHashSet;
import java.util.Set;

public class ActionInfo {
    Set<String> success;
    String failure;

    public ActionInfo() {
        this.success = new LinkedHashSet<>();
        this.failure = "";
    }

    public String getFailure() {
        return failure;
    }

    public ActionInfo setFailure(String failure) {
        this.failure = failure;
        return this;
    }

    public Set<String> getSuccess() {
        return success;
    }

    public ActionInfo setSuccess(Set<String> success) {
        this.success = success;
        return this;
    }
}
