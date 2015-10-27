package com.elster.jupiter.tasks;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.QueryExecutor;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TaskService {

    String COMPONENTNAME = "TSK";

    RecurrentTaskBuilder newBuilder();

    MessageHandler createMessageHandler(TaskExecutor taskExecutor);

    Optional<RecurrentTask> getRecurrentTask(long id);

    Optional<RecurrentTask> getRecurrentTask(String name);

    Optional<TaskOccurrence> getOccurrence(Long id);

    List<TaskOccurrence> getOccurrences(RecurrentTask recurrentTask, Range<Instant> period);

    Query<? extends RecurrentTask> getTaskQuery();

    void launch();

    boolean isLaunched();

    void shutDown();

    QueryExecutor<TaskOccurrence> getTaskOccurrenceQueryExecutor();
}
