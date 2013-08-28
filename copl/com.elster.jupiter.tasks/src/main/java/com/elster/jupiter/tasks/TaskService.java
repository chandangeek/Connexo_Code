package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.subscriber.MessageHandler;

public interface TaskService {

    RecurrentTaskBuilder newBuilder();

    MessageHandler createMessageHandler(TaskExecutor taskExecutor);

    RecurrentTask getRecurrentTask(long id);

    void launch();

    boolean isLaunched();
}
