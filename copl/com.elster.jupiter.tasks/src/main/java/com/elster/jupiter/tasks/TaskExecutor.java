package com.elster.jupiter.tasks;

public interface TaskExecutor {

    void execute(TaskOccurrence occurrence);

    default void postExecute(TaskOccurrence occurrence) {
    }
}
