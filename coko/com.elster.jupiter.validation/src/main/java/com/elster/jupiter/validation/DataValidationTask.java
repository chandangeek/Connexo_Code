package com.elster.jupiter.validation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.time.Instant;

public interface DataValidationTask {

    public void activate();

    public DataValidationStatus execute(DataValidationOccurence taskOccurence);

    public void deactivate();

    public Instant getNextExecution();

    public void save();

    public void delete();

    public String getName();

    public void setName(String name);

    public EndDeviceGroup getEndDeviceGroup();

    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    public long getId();

    public void setId(long id);

}
