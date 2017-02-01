/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

public enum BulkAdvanceReadingsSettings implements AdvanceReadingsSettings {
    INSTANCE;

    public static final String BULK_ADVANCE_READINGS_SETTINGS = "bulk";

    public String toString() {
        return BULK_ADVANCE_READINGS_SETTINGS;
    }
}
