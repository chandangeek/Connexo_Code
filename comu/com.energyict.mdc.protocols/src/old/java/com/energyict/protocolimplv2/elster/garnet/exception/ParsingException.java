/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.exception;

/**
 * @author sva
 * @since 23/05/2014 - 10:14
 */
public class ParsingException extends GarnetException {

    private static final String MESSAGE = "Failed to parse the frame";

    public ParsingException() {
        super(MESSAGE);
    }

    public ParsingException(Exception e) {
        super(MESSAGE, e);
    }

    public ParsingException(String s) {
        super(MESSAGE + ": " + s);
    }

    public ParsingException(String s, Exception e) {
        super(MESSAGE + ": " + s, e);
    }
}