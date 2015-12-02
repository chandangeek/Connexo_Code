package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

@ProviderType
public interface DataValidationTaskBuilder {

    public DataValidationTaskBuilder setName(String name);

    public DataValidationTaskBuilder setApplication(String application);

    public DataValidationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    public DataValidationTaskBuilder setUsagePointGroup(UsagePointGroup usagePointGroup);

    public DataValidationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    public DataValidationTaskBuilder scheduleImmediately();

    public DataValidationTaskBuilder setNextExecution(Instant nextExecution);

    public DataValidationTask build();

}
