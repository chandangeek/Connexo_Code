package com.elster.jupiter.fileimport;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.io.File;

public interface ImportScheduleBuilder {

    ImportSchedule build();

    ImportScheduleBuilder setDestination(DestinationSpec destination);

    ImportScheduleBuilder setImportDirectory(File directory);

    ImportScheduleBuilder setProcessingDirectory(File directory);

    ImportScheduleBuilder setSuccessDirectory(File directory);

    ImportScheduleBuilder setFailureDirectory(File directory);

    ImportScheduleBuilder setCronExpression(CronExpression cronExpression);
}
