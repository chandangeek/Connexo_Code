/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.exception;

public class ObjectMapperInitException extends ObjectMapperException {

    public ObjectMapperInitException(String message) {
        super(message);
    }

    public ObjectMapperInitException(Exception e) {
        super(e);
    }
}
