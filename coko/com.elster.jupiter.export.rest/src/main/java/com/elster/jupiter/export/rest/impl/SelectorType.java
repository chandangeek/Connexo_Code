/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;

public enum SelectorType {
    CUSTOM, DEFAULT_READINGS, DEFAULT_EVENTS, DEFAULT_USAGE_POINT_READINGS;

    public static SelectorType forSelector(String selector) {
        switch (selector) {
            case DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR:
                return DEFAULT_READINGS;
            case DataExportService.STANDARD_EVENT_DATA_SELECTOR:
                return DEFAULT_EVENTS;
            case DataExportService.STANDARD_USAGE_POINT_DATA_SELECTOR:
                return DEFAULT_USAGE_POINT_READINGS;
            default:
                return CUSTOM;
        }
    }
}
