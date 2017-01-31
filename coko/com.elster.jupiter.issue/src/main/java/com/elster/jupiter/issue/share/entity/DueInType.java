/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import org.joda.time.DateTimeConstants;

public enum DueInType {
    DAY("days", DateTimeConstants.MILLIS_PER_DAY),
    WEEK("weeks", DateTimeConstants.MILLIS_PER_WEEK),
    MONTH("months", Const.MILLIS_PER_MONTH),
    YEAR("years", Const.MILLIS_PER_YEAR);

    private long multiplier;
    private String name;

    private DueInType(String name, long multiplier){
        this.name = name;
        this.multiplier = multiplier;
    }

    public long dueValueFor(long value){
        return System.currentTimeMillis() + value * this.multiplier;
    }

    public String getName() {
        return name;
    }

    public static DueInType fromString(String type) {
        if (type != null) {
            for (DueInType column : DueInType.values()) {
                if (column.getName().equalsIgnoreCase(type)) {
                    return column;
                }
            }
        }
        return null;
    }

    private static class Const {
        private static long MILLIS_PER_MONTH = DateTimeConstants.MILLIS_PER_WEEK * 4;
        private static long MILLIS_PER_YEAR = MILLIS_PER_MONTH * 12;

        private Const(){}
    }
}
