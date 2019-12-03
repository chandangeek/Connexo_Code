package com.energyict.mdc.engine.offline.gui.decorators;

import java.util.HashMap;

/**
 * @author sva
 * @since 11/03/14 - 8:28
 */
public class LoggingEventDecorator extends DefaultEventDecorator implements EventDecorator {

    public LoggingEventDecorator(HashMap<String, Object> event) {
        super(event);
    }

    public String asLogString() {
        return this.getOccurenceDateAsString() + " - " + this.getEvent().get("log-level") + ": " + this.getEvent().get("message");
    }
}