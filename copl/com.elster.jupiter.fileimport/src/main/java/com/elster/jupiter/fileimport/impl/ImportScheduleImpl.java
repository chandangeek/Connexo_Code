package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.io.File;

class ImportScheduleImpl implements ImportSchedule {

    private long id;
    private String destinationName;
    private transient DestinationSpec destination;
    private File importDirectory;
    private File inProcessDirectory;
    private File successDirectory;
    private File failureDirectory;
    private transient CronExpression cronExpression;
    private String cronString;

    private ImportScheduleImpl() {
    }

    ImportScheduleImpl(CronExpression cronExpression, DestinationSpec destination, File importDirectory, File inProcessDirectory, File failureDirectory, File successDirectory) {
        this.cronExpression = cronExpression;
        this.cronString = cronExpression.toString();
        this.destination = destination;
        this.destinationName = destination.getName();
        this.importDirectory = importDirectory;
        this.inProcessDirectory = inProcessDirectory;
        this.failureDirectory = failureDirectory;
        this.successDirectory = successDirectory;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public DestinationSpec getDestination() {
        if (destination == null) {
            destination = Bus.getMessageService().getDestinationSpec(destinationName).get();
        }
        return destination;
    }

    @Override
    public File getImportDirectory() {
        return importDirectory;
    }

    @Override
    public File getInProcessDirectory() {
        return inProcessDirectory;
    }

    @Override
    public File getSuccessDirectory() {
        return successDirectory;
    }

    @Override
    public File getFailureDirectory() {
        return failureDirectory;
    }

    @Override
    public CronExpression getScheduleExpression() {
        if (cronExpression == null) {
            cronExpression = Bus.getCronExpressionParser().parse(cronString);
        }
        return cronExpression;
    }

    @Override
    public FileImport createFileImport(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException();
        }
        return FileImportImpl.create(this, file);
    }

    @Override
    public void save() {
        if (id == 0) {
            Bus.getOrmClient().getImportScheduleFactory().persist(this);
        } else {
            Bus.getOrmClient().getImportScheduleFactory().update(this);
        }
    }
}
