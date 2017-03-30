/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.exception;

/**
 * @author sva
 * @since 23/05/2014 - 10:14
 */
public class UnknownFunctionCodeParsingException extends ParsingException {

    public UnknownFunctionCodeParsingException() {
        super();
    }

    public UnknownFunctionCodeParsingException(Exception e) {
        super(e);
    }

    public UnknownFunctionCodeParsingException(String s) {
        super(s);
    }

    public UnknownFunctionCodeParsingException(String s, Exception e) {
        super(s, e);
    }
}