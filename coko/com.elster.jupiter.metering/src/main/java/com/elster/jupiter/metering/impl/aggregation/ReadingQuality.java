/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

public enum ReadingQuality {

    DERIVED_DETERMINISTIC("3.11.0"),
    DERIVED_INDETERMINISTIC("3.11.1"),  //edited/estimated
    DERIVED_SUSPECT("3.11.1000"),
    DERIVED_MISSING("3.11.1001");

    private String code;

    ReadingQuality(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String toString() {
        return super.toString() + " (" + code + ")";
    }

    public static ReadingQuality getReadingQuality(String code) {
        if (code.equals(ReadingQuality.DERIVED_SUSPECT.getCode())) {
            return DERIVED_SUSPECT;
        } else if (code.equals(ReadingQuality.DERIVED_MISSING.getCode())) {
            return DERIVED_MISSING;
        } else if (code.equals(ReadingQuality.DERIVED_INDETERMINISTIC.getCode())) {
            return DERIVED_INDETERMINISTIC;
        } else {
            return DERIVED_DETERMINISTIC;
        }
    }
}
