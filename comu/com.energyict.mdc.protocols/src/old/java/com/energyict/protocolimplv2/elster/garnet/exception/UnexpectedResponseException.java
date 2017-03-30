/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.exception;

/**
 * @author sva
 * @since 23/05/2014 - 11:20
 */
public class UnexpectedResponseException extends GarnetException {

    public UnexpectedResponseException(String s, Exception e) {
        super(s, e);
    }

    public UnexpectedResponseException(String s) {
        super(s);
    }

    public UnexpectedResponseException(Exception e) {
        super(e);
    }

    public UnexpectedResponseException() {
    }
}
