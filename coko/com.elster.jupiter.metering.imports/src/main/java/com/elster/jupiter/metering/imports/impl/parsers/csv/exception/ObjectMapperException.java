/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.exception;

public abstract class ObjectMapperException extends Exception {
    public ObjectMapperException(String message) {
        super(message);
    }

    public ObjectMapperException(Exception e) {
        super(e);
    }
}
