/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class ComScheduleInfo {

    public long id;
    public String name;
    public TemporalExpressionInfo temporalExpression;
    @JsonDeserialize(using = NullifyingDeserializer.class)
    public Instant plannedDate;
    public boolean isInUse;
    public List<ComTaskInfo> comTaskUsages;
    public Instant startDate;
    public String mRID;
    public long version;
    public boolean isDefault;

    public ComScheduleInfo() {
    }

    public static ComScheduleInfo from(ComSchedule comSchedule, boolean inUse, Instant instant) {
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();

        if (comSchedule == null) {
            return comScheduleInfo;
        }

        comScheduleInfo.id = comSchedule.getId();
        comScheduleInfo.isDefault = comSchedule.isDefault();
        comScheduleInfo.name = comSchedule.getName();
        comScheduleInfo.temporalExpression = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
        comScheduleInfo.startDate = comSchedule.getStartDate();
        comScheduleInfo.isInUse = inUse;
        comScheduleInfo.comTaskUsages = ComTaskInfo.from(comSchedule.getComTasks());
        comScheduleInfo.mRID = comSchedule.getmRID().orElse(null);
        comScheduleInfo.version = comSchedule.getVersion();

        setPlannedDate(comScheduleInfo, comSchedule, instant);

        return comScheduleInfo;
    }

    /**
     * Set the ComScheduleInfo plannedDate based on the ComSchedule starting date.
     *
     * @param comScheduleInfo the ComScheduleInfo object
     * @param comSchedule the ComSchedule
     * @param instant the Instant
     */
    private static void setPlannedDate(ComScheduleInfo comScheduleInfo, ComSchedule comSchedule, Instant instant) {
        boolean isStartTimeAfterNow = comSchedule.getStartDate().isAfter(instant);

        if (isStartTimeAfterNow) {
            comScheduleInfo.plannedDate = comSchedule.getPlannedDate().orElseGet(comSchedule::getStartDate);

        } else {
            Optional<ZonedDateTime> nextOccurrence = comSchedule.getTemporalExpression().nextOccurrence(ZonedDateTime.ofInstant((isStartTimeAfterNow ? comSchedule.getStartDate() : instant), ZoneId.systemDefault()));
            nextOccurrence.ifPresent(zonedDateTime -> comScheduleInfo.plannedDate = zonedDateTime.toInstant());
        }
    }
}