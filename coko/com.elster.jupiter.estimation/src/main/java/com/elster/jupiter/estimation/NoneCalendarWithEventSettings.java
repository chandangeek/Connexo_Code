package com.elster.jupiter.estimation;

/**
 * Created by aeryomin on 05.04.2017.
 */
public enum NoneCalendarWithEventSettings implements CalendarWithEventSettings {
    INSTANCE;

    public static final String NONE_CALENDAR_SETTINGS = "none";

    public String toString() {
        return NONE_CALENDAR_SETTINGS;
    }
}
