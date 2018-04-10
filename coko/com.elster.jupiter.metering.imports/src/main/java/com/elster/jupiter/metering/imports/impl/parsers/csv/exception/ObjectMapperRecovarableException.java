/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.exception;

public class ObjectMapperRecovarableException extends ObjectMapperException {

    public ObjectMapperRecovarableException(String message) {
        super(message);
    }

    public ObjectMapperRecovarableException(Exception e) {
        super(e);
    }
}
