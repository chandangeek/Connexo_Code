package com.elster.jupiter.usagepoint.lifecycle;

public class ExecutableMicroActionException extends UsagePointStateChangeException {
    private ExecutableMicroAction microAction;

    public ExecutableMicroActionException(ExecutableMicroAction microAction, String message) {
        super(message);
        this.microAction = microAction;
    }

    public ExecutableMicroAction getMicroAction() {
        return this.microAction;
    }
}