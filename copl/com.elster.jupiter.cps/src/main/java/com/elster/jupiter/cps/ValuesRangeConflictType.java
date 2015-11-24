package com.elster.jupiter.cps;


import com.elster.jupiter.util.exception.MessageSeed;

public enum ValuesRangeConflictType {
    RANGE_OVERLAP_UPDATE_START,
    RANGE_OVERLAP_UPDATE_END,
    RANGE_OVERLAP_DELETE,
    RANGE_GAP_BEFORE,
    RANGE_GAP_AFTER,
    RANGE_INSERTED;
}
