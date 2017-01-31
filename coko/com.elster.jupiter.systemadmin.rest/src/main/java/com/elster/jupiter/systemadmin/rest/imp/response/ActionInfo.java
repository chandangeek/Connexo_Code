/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import java.util.ArrayList;
import java.util.List;

public class ActionInfo {
    public List<String> success;
    public List<ErrorMessage> errors = new ArrayList<>();

    public ActionInfo setFailure(String failure) {
        this.errors.add(new ErrorMessage(failure));
        return this;
    }

    public ActionInfo setSuccess(List<String> success) {
        this.success = success;
        return this;
    }

    private static class ErrorMessage {
        public String msg;

        public ErrorMessage(String message) {
            this.msg = message;
        }
    }
}
