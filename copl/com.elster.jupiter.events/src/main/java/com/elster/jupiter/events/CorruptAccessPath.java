package com.elster.jupiter.events;

public class CorruptAccessPath extends RuntimeException {

    public CorruptAccessPath(Throwable cause) {
        super(cause);
    }
}
