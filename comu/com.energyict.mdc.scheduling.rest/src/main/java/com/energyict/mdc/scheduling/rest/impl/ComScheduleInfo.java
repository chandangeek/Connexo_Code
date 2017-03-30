/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;
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

    public ComScheduleInfo() {
    }

    public static ComScheduleInfo from(ComSchedule comSchedule, boolean inUse, Instant instant) {
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.id = comSchedule.getId();
        comScheduleInfo.name = comSchedule.getName();
        comScheduleInfo.temporalExpression = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
        Optional<ZonedDateTime> nextOccurrence = comSchedule.getTemporalExpression().nextOccurrence(ZonedDateTime.ofInstant((comSchedule.getStartDate().isAfter(instant) ? comSchedule.getStartDate() : instant) , ZoneId.systemDefault()));
        nextOccurrence.ifPresent(zonedDateTime -> comScheduleInfo.plannedDate = zonedDateTime.toInstant());
        comScheduleInfo.startDate = comSchedule.getStartDate();
        comScheduleInfo.isInUse = inUse;
        comScheduleInfo.comTaskUsages = ComTaskInfo.from(comSchedule.getComTasks());
        comScheduleInfo.mRID = comSchedule.getmRID().orElse(null);
        comScheduleInfo.version = comSchedule.getVersion();
        return comScheduleInfo;
    }
}