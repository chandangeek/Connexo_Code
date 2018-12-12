/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

public class ComServerExecutionException extends RuntimeException {

    public ComServerExecutionException() {
        super();
    }

    public ComServerExecutionException(Throwable cause) {
        super(cause);
    }

    public ComServerExecutionException(String s) {
        super(s);
    }

    public ComServerExecutionException(String s, Throwable cause) {
        super(s, cause);
    }

}