package com.elster.jupiter.systemadmin.rest.response;

public class ActionInfo {
    Boolean success;
    String message;

    public ActionInfo() {
        this.success = true;
        this.message = "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
