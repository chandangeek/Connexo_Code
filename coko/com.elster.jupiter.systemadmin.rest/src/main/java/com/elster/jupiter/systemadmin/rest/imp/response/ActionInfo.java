package com.elster.jupiter.systemadmin.rest.imp.response;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ActionInfo {
    public Set<String> success;
    public List<ErrorMessage> errors = new ArrayList<>();

    public ActionInfo() {
        this.success = new LinkedHashSet<>();
    }

    public ActionInfo setErrors(String failure) {
        errors.add(new ErrorMessage(failure));
        return this;
    }

    public ActionInfo setSuccess(Set<String> success) {
        this.success = success;
        return this;
    }
}

class ErrorMessage {
    public String msg;
    public ErrorMessage(String message) {
        this.msg = message;
    }
}
