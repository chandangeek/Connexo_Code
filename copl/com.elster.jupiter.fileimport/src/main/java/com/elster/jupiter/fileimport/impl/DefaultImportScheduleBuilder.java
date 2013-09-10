package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.io.File;

class DefaultImportScheduleBuilder implements ImportScheduleBuilder {

    private transient DestinationSpec destination;
    private File importDirectory;
    private File inProcessDirectory;
    private File successDirectory;
    private File failureDirectory;
    private transient CronExpression cronExpression;

    @Override
    public ImportSchedule build() {
        return new ImportScheduleImpl(cronExpression, destination, importDirectory, inProcessDirectory, failureDirectory, successDirectory);
    }

    @Override
    public ImportScheduleBuilder setDestination(DestinationSpec destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public ImportScheduleBuilder setImportDirectory(File directory) {
        this.importDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setProcessingDirectory(File directory) {
        this.inProcessDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setSuccessDirectory(File directory) {
        this.successDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setFailureDirectory(File directory) {
        this.failureDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setCronExpression(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }
}
