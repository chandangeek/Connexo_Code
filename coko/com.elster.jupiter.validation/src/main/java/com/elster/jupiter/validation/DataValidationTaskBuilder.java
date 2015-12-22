package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;

@ProviderType
public interface DataValidationTaskBuilder {

    DataValidationTaskBuilder setName(String name);

    DataValidationTaskBuilder setApplication(String application);

    DataValidationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    DataValidationTaskBuilder setUsagePointGroup(UsagePointGroup usagePointGroup);

    DataValidationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    DataValidationTaskBuilder scheduleImmediately();

    DataValidationTaskBuilder setNextExecution(Instant nextExecution);

    DataValidationTask create();

}
