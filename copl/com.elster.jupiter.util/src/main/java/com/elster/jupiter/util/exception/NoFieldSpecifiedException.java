package com.elster.jupiter.util.exception;

import com.elster.jupiter.util.MessageSeeds;

public class NoFieldSpecifiedException extends PersistenceException {

    public NoFieldSpecifiedException(String onCommand) {
        super(MessageSeeds.SQLPART_NOFIELD_SPECIFIED, onCommand);
    }
}
