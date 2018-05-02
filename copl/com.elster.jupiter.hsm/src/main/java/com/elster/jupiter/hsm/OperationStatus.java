/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm;

public class OperationStatus {

    private final String msg;
    private final Integer status;
    private final Throwable cause;


    public OperationStatus(String msg, Integer status, Throwable cause) {
        this.msg = msg;
        this.status = status;
        this.cause = cause;
    }


    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public Throwable getCause() {
        return cause;
    }
}
