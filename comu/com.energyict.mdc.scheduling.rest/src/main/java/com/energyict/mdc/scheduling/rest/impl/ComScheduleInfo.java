package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;

public class ComScheduleInfo {

    public long id;
    public String name;
    public NextExecutionsSpecsInfo nextExecutionSpecs;

    public ComScheduleInfo() {
    }

    public ComScheduleInfo(ComSchedule comSchedule) {
        this.id = comSchedule.getId();
        this.name = comSchedule.getName();
        this.nextExecutionSpecs = new NextExecutionsSpecsInfo(comSchedule.getTemporalExpression());
    }
}
