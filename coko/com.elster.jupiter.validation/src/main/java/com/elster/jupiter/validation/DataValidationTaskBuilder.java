/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface DataValidationTaskBuilder {

    DataValidationTaskBuilder setName(String name);

    DataValidationTaskBuilder setQualityCodeSystem(QualityCodeSystem qualityCodeSystem);

    DataValidationTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    DataValidationTaskBuilder setUsagePointGroup(UsagePointGroup usagePointGroup);

    DataValidationTaskBuilder setMetrologyPurpose(MetrologyPurpose metrologyPurpose);

    DataValidationTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    DataValidationTaskBuilder scheduleImmediately();

    DataValidationTaskBuilder setNextExecution(Instant nextExecution);

    DataValidationTaskBuilder setLogLevel(int logLevel);

    DataValidationTaskBuilder setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks);

    DataValidationTask create();

}
