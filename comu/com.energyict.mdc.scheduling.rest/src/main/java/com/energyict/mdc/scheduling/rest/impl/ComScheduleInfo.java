package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import java.util.Calendar;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class ComScheduleInfo {

    public long id;
    public String name;
    public TemporalExpressionInfo temporalExpression;
    public Date plannedDate;
    public boolean isInUse;
    @XmlJavaTypeAdapter(SchedulingStatusAdapter.class)
    public SchedulingStatus schedulingStatus;

    public ComScheduleInfo() {
    }

    public static ComScheduleInfo from(ComSchedule comSchedule, boolean inUse) {
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.id = comSchedule.getId();
        comScheduleInfo.name = comSchedule.getName();
        comScheduleInfo.temporalExpression = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
        comScheduleInfo.plannedDate = SchedulingStatus.PAUSED.equals(comSchedule.getSchedulingStatus())?null:comSchedule.getNextTimestamp(Calendar.getInstance());
        comScheduleInfo.schedulingStatus = comSchedule.getSchedulingStatus();
        comScheduleInfo.isInUse = inUse;
        return comScheduleInfo;
    }
}
