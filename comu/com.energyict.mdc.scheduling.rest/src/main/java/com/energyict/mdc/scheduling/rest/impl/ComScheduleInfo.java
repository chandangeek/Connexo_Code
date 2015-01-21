package com.energyict.mdc.scheduling.rest.impl;

import java.time.Instant;
import java.util.List;

import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;

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

    public ComScheduleInfo() {
    }

    public static ComScheduleInfo from(ComSchedule comSchedule, boolean inUse) {
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.id = comSchedule.getId();
        comScheduleInfo.name = comSchedule.getName();
        comScheduleInfo.temporalExpression = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
        comScheduleInfo.plannedDate = comSchedule.getPlannedDate().orElse(null);
        comScheduleInfo.startDate = comSchedule.getStartDate() == null ? null : comSchedule.getStartDate();
        comScheduleInfo.isInUse = inUse;
        comScheduleInfo.comTaskUsages = ComTaskInfo.from(comSchedule.getComTasks());
        comScheduleInfo.mRID = comSchedule.getmRID();
        return comScheduleInfo;
    }

}