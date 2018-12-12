/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface TaskService {

    String COMPONENTNAME = "TSK";

    RecurrentTaskBuilder newBuilder();

    MessageHandler createMessageHandler(TaskExecutor taskExecutor);

    Optional<RecurrentTask> getRecurrentTask(long id);

    Optional<RecurrentTask> getRecurrentTask(String name);

    List<RecurrentTask> getRecurrentTasks();

    TaskFinder getTaskFinder(RecurrentTaskFilterSpecification filterSpecification, int start, int limit);

    Optional<TaskOccurrence> getOccurrence(Long id);

    List<TaskOccurrence> getOccurrences(RecurrentTask recurrentTask, Range<Instant> period);

    Query<? extends RecurrentTask> getTaskQuery();

    void launch();

    boolean isLaunched();

    void shutDown();

    QueryExecutor<TaskOccurrence> getTaskOccurrenceQueryExecutor();
}
