package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.google.common.base.Optional;

public interface TaskService {

    RecurrentTaskBuilder newBuilder();

    MessageHandler createMessageHandler(TaskExecutor taskExecutor);

    Optional<RecurrentTask> getRecurrentTask(long id);

    void launch();

    boolean isLaunched();
}
