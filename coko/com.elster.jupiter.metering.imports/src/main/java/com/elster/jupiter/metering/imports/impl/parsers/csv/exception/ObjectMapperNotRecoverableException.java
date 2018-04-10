/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.exception;

public class ObjectMapperNotRecoverableException extends ObjectMapperException{
    public ObjectMapperNotRecoverableException(String message) {
        super(message);
    }

    public ObjectMapperNotRecoverableException(Exception e) {
        super(e);
    }
}
