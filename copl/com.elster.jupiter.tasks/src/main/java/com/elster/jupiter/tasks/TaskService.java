package com.elster.jupiter.tasks;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import java.util.Optional;

public interface TaskService {

    String COMPONENTNAME = "TSK";

    RecurrentTaskBuilder newBuilder();

    MessageHandler createMessageHandler(TaskExecutor taskExecutor);

    Optional<RecurrentTask> getRecurrentTask(long id);

    void launch();

    boolean isLaunched();
}
