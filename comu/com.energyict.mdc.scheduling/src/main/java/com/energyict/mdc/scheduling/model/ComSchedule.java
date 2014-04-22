package com.energyict.mdc.scheduling.model;

import com.energyict.mdc.scheduling.NextExecutionSpecs;

public interface ComSchedule {

    public long getId();

    public String getName();
    public void setName(String name);
    public NextExecutionSpecs getNextExecutionSpec();
    public void setNextExecutionSpec(NextExecutionSpecs nextExecutionSpec);
}
