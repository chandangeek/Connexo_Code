/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;


import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.List;
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

    boolean usedByAutorescheduleTask(Long comTaskId);

    List<CustomTaskProperty> findPropertyByComTaskId(Long comTaskId, int skip, int limit);

    List<CustomTaskProperty> findPropertyByDeviceGroupName(String endDeviceGroupName, int skip, int limit);

}
