package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.util.Date;
import java.util.List;

public class ComScheduleInfo {

    public long id;
    public String name;
    public TemporalExpressionInfo temporalExpression;
    public Date plannedDate;
    public boolean isInUse;
    public List<ComTaskInfo> comTaskUsages;
    public Date startDate;
    public String mRID;

    public ComScheduleInfo() {
    }

    public static ComScheduleInfo from(ComSchedule comSchedule, boolean inUse) {
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.id = comSchedule.getId();
        comScheduleInfo.name = comSchedule.getName();
        comScheduleInfo.temporalExpression = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
        comScheduleInfo.plannedDate = comSchedule.getPlannedDate();
        comScheduleInfo.startDate = comSchedule.getStartDate()==null?null: Date.from(comSchedule.getStartDate());
        comScheduleInfo.isInUse = inUse;
        comScheduleInfo.comTaskUsages = ComTaskInfo.from(comSchedule.getComTasks());
        comScheduleInfo.mRID = comSchedule.getmRID();
        return comScheduleInfo;
    }

}
