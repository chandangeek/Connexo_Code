package com.elster.jupiter.fileimport;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.io.File;

public interface ImportSchedule {

    long getId();

    DestinationSpec getDestination();

    File getImportDirectory();

    File getInProcessDirectory();

    File getSuccessDirectory();

    File getFailureDirectory();

    CronExpression getScheduleExpression();

    void save();

    FileImport createFileImport(File file);
}
