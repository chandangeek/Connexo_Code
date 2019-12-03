package com.energyict.mdc.engine.offline.gui.decorators;

import java.util.HashMap;

/**
 * @author sva
 * @since 11/03/14 - 8:29
 */
public class DefaultEventDecorator implements EventDecorator {

    HashMap<String, Object> event;
    String occurenceDateAsString;

    public DefaultEventDecorator(HashMap<String, Object> event) {
        this.event = event;
        // the event's timestamp field contains the date as a String formatted by the backend:
        this.setOccurenceDateAsString((String) event.get("timestamp"));
    }

    public String asLogString() {
        String logString = getOccurenceDateAsString() + " - " + getEvent().get("clazz") + " {";

        for (String attrib : getEvent().keySet()) {
            if (!attrib.equals("timestamp") && !attrib.equals("clazz")) {
                logString += " " + attrib + ": " + this.getEvent().get(attrib);
            }
        }
        logString += "}";
        return logString;
    }

    public HashMap<String, Object> getEvent() {
        return event;
    }

    public void setEvent(HashMap<String, Object> event) {
        this.event = event;
    }

    public String getOccurenceDateAsString() {
        return occurenceDateAsString;
    }

    public void setOccurenceDateAsString(String occurenceDateAsString) {
        this.occurenceDateAsString = occurenceDateAsString;
    }
}
