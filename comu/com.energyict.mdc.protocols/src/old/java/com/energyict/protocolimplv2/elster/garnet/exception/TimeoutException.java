/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.exception;

/**
 * @author sva
 * @since 23/05/2014 - 11:20
 */
public class TimeoutException extends ConnectionException {

    public TimeoutException(String s, Exception e) {
        super(s, e);
    }

    public TimeoutException(String s) {
        super(s);
    }

    public TimeoutException(Exception e) {
        super(e);
    }

    public TimeoutException() {
    }
}
