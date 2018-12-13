/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


public enum DueInType {
    DAY("days", ChronoUnit.DAYS),
    WEEK("weeks", ChronoUnit.WEEKS),
    MONTH("months", ChronoUnit.MONTHS),
    YEAR("years", ChronoUnit.YEARS);

    private ChronoUnit chronoUnit;
    private String name;

    private DueInType(String name, ChronoUnit chronoUnit){
        this.name = name;
        this.chronoUnit = chronoUnit;
    }

    public long dueValueFor(long value){
        return ZonedDateTime.now().plus(value, chronoUnit).toInstant().toEpochMilli();
    }

    public long dueValueFor(long value, Instant instant){
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return zonedDateTime.plus(value, chronoUnit).toInstant().toEpochMilli();
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
}
