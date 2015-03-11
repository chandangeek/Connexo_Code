package com.elster.jupiter.validation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;


public interface DataValidationTaskBuilder {

    public DataValidationTaskBuilder setName(String name);

    public DataValidationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    public DataValidationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    public DataValidationTaskBuilder scheduleImmediately();

    public DataValidationTaskBuilder setNextExecution(Instant nextExecution);

    public DataValidationTask build();

}
