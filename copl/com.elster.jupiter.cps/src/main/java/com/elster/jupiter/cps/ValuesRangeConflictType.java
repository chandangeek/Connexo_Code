/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

public enum ValuesRangeConflictType {
    RANGE_OVERLAP_UPDATE_START,
    RANGE_OVERLAP_UPDATE_END,
    RANGE_OVERLAP_DELETE,
    RANGE_GAP_BEFORE,
    RANGE_GAP_AFTER,
    RANGE_INSERTED;
}