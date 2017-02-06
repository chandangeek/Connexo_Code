/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

public class ProcessorException extends ImportException {
    public ProcessorException(MessageSeed message, Object... args) {
        super(message, args);
    }
}
