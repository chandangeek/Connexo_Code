/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.exception;

/**
 * @author sva
 * @since 23/05/2014 - 11:21
 */
public class CrcMismatchException extends AbntException {

    public CrcMismatchException(String s, Exception e) {
        super(s, e);
    }

    public CrcMismatchException(String s) {
        super(s);
    }

    public CrcMismatchException(Exception e) {
        super(e);
    }

    public CrcMismatchException() {
    }
}