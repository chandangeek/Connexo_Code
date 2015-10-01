package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;

public enum SelectorType {
    CUSTOM, DEFAULT_READINGS, DEFAULT_EVENTS;

    public static SelectorType forSelector(String selector) {
        switch (selector) {
            case DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR:
                return DEFAULT_READINGS;
            case DataExportService.STANDARD_EVENT_DATA_SELECTOR:
                return DEFAULT_EVENTS;
            default:
                return CUSTOM;
        }
    }
}
