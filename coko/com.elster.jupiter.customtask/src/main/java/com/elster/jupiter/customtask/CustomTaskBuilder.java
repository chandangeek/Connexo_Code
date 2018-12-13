/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface CustomTaskBuilder {

    CustomTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression);

    CustomTaskBuilder setNextExecution(Instant nextExecution);

    CustomTaskBuilder setApplication(String application);

    CustomTaskBuilder setName(String string);

    CustomTaskBuilder setLogLevel(int logLevel);

    CustomTaskBuilder setTaskType(String taskType);

    CustomTaskBuilder setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks);

    PropertyBuilder<CustomTaskBuilder> addProperty(String name);

    CustomTask create();

    interface PropertyBuilder<T> {
        T withValue(Object value);
    }
}
