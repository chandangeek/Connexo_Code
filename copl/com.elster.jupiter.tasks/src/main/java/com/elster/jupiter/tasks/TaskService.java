package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.consumer.MessageHandler;

public interface TaskService {

    RecurrentTaskBuilder newBuilder();

    MessageHandler createMessageHandler(TaskExecutor taskExecutor);

}
