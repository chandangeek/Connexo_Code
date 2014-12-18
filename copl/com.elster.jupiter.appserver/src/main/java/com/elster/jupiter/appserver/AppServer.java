package com.elster.jupiter.appserver;

import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.util.List;

public interface AppServer {

    CronExpression getScheduleFrequency();

    String getName();

	List<? extends SubscriberExecutionSpec> getSubscriberExecutionSpecs();

	SubscriberExecutionSpec createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

    void sendCommand(AppServerCommand command);

    void setRecurrentTaskActive(boolean recurrentTaskActive);

    boolean isRecurrentTaskActive();

    void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec);

    boolean isActive();

    void activate();

    void deactivate();

    void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads);

    BatchUpdate forBatchUpdate();

    void delete();

    interface BatchUpdate extends AutoCloseable {

        SubscriberExecutionSpec createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

        void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec);

        void setRecurrentTaskActive(boolean recurrentTaskActive);

        void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads);

        void activate();

        void deactivate();

        @Override
        void close();

        void delete();
    }
}
