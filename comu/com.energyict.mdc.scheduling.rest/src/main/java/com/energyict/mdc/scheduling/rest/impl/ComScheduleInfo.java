package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class ComScheduleInfo {

    public long id;
    public String name;
    public TemporalExpressionInfo temporalExpression;
    public Date plannedDate;
    public boolean isInUse;
    @XmlJavaTypeAdapter(SchedulingStatusAdapter.class)
    public SchedulingStatus schedulingStatus;
    public List<ComTaskInfo> comTaskUsages;
    public Date startDate;

    public ComScheduleInfo() {
    }

    public static ComScheduleInfo from(ComSchedule comSchedule, boolean inUse) {
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.id = comSchedule.getId();
        comScheduleInfo.name = comSchedule.getName();
        comScheduleInfo.temporalExpression = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
        comScheduleInfo.plannedDate = comSchedule.getPlannedDate();
        comScheduleInfo.schedulingStatus = comSchedule.getSchedulingStatus();
        comScheduleInfo.startDate = comSchedule.getStartDate()==null?null:comSchedule.getStartDate().toDate();
        comScheduleInfo.isInUse = inUse;
        comScheduleInfo.comTaskUsages = ComTaskInfo.from(comSchedule.getComTasks());
        return comScheduleInfo;
    }

}
