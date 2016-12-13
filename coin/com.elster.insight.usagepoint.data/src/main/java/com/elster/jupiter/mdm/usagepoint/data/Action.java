package com.elster.jupiter.mdm.usagepoint.data;

public enum Action {
    ADD_CALENDAR("addCalendar");

    private final String matcherString;

    Action(String matcherString) {
        this.matcherString = matcherString;
    }

    public boolean matches(String action) {
        return matcherString.equalsIgnoreCase(action);
    }
}
