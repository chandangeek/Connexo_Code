package com.elster.jupiter.validation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    void setScheduleImmediately(boolean scheduleImmediately);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    public Optional<Instant> getLastRun();

    public ScheduleExpression getScheduleExpression();

    public Optional<ScheduleExpression> getScheduleExpression(Instant at);

    public void setNextExecution(Instant instant);

    public void triggerNow();

    public boolean canBeDeleted();

    public Optional<DataValidationOccurence> getLastOccurrence();

    public List<? extends DataValidationOccurence> getOccurrences();
}
