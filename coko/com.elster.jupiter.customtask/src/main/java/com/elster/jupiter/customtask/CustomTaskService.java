/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface CustomTaskService {

    String COMPONENTNAME = "CST";

    Optional<? extends CustomTask> findCustomTask(long id);

    List<CustomTaskFactory> getAvailableCustomTasks(String application);

    Optional<CustomTaskFactory> getCustomTaskFactory(String name);

    CustomTaskOccurrence createCustomTaskOccurrence(TaskOccurrence taskOccurrence);

    Optional<CustomTaskOccurrence> findCustomTaskOccurrence(TaskOccurrence occurrence);

    CustomTaskBuilder newBuilder();

    Optional<? extends CustomTask> findCustomTaskByRecurrentTask(long id);

    Optional<? extends CustomTask> findAndLockCustomTask(long id, long version);

    Optional<CustomTaskOccurrence> findCustomTaskOccurrence(long occurrenceId);

}
