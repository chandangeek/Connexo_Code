package com.elster.jupiter.appserver;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface AppServer {

    CronExpression getScheduleFrequency();

    String getName();

	List<? extends SubscriberExecutionSpec> getSubscriberExecutionSpecs();

    List<? extends ImportScheduleOnAppServer> getImportSchedulesOnAppServer();

	SubscriberExecutionSpec createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

    ImportScheduleOnAppServer addImportScheduleOnAppServer(ImportSchedule importSchedule);

    void sendCommand(AppServerCommand command);

    void setRecurrentTaskActive(boolean recurrentTaskActive);

    boolean isRecurrentTaskActive();

    void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec);

    void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer);

    boolean isActive();

    void activate();

    void deactivate();

    void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads);

    void setImportDirectory(Path path);
    void removeImportDirectory();
    Optional<Path> getImportDirectory();

    BatchUpdate forBatchUpdate();

    void delete();

    interface BatchUpdate extends AutoCloseable {

        SubscriberExecutionSpec createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

        void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec);

        ImportScheduleOnAppServer addImportScheduleOnAppServer(ImportSchedule importSchedule);

        void removeImportScheduleOnAppServer(ImportScheduleOnAppServer importScheduleOnAppServer);

        void setRecurrentTaskActive(boolean recurrentTaskActive);

        void setThreadCount(SubscriberExecutionSpec subscriberExecutionSpec, int threads);

        void activate();

        void deactivate();

        @Override
        void close();

        void delete();
    }
}
