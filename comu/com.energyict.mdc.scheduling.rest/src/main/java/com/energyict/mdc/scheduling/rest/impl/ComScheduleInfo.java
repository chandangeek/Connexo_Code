package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;

public class ComScheduleInfo {

    public long id;
    public String name;
    public NextExecutionsSpecsInfo nextExecutionSpecs;

    public ComScheduleInfo() {
    }

    public static ComScheduleInfo from(ComSchedule comSchedule) {
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.id = comSchedule.getId();
        comScheduleInfo.name = comSchedule.getName();
        comScheduleInfo.nextExecutionSpecs = new NextExecutionsSpecsInfo(comSchedule.getTemporalExpression());
        return comScheduleInfo;
    }
}
