package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.inject.Inject;
import java.io.File;

/**
 * ImportSchedule implementation.
 */
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
    private final MessageService messageService;
    private final DataModel dataModel;
    private final CronExpressionParser cronExpressionParser;
    private final FileNameCollisionResolver fileNameCollisionresolver;
    private final FileSystem fileSystem;
    private final Thesaurus thesaurus;

    @SuppressWarnings("unused")
    @Inject
	ImportScheduleImpl(MessageService messageService, DataModel dataModel, CronExpressionParser cronExpressionParser, FileNameCollisionResolver fileNameCollisionresolver, FileSystem fileSystem, Thesaurus thesaurus) {
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
        this.fileNameCollisionresolver = fileNameCollisionresolver;
        this.fileSystem = fileSystem;
        this.thesaurus = thesaurus;
    }

    static ImportScheduleImpl from(DataModel dataModel, CronExpression cronExpression, DestinationSpec destination, File importDirectory, File inProcessDirectory, File failureDirectory, File successDirectory) {
        return dataModel.getInstance(ImportScheduleImpl.class).init(cronExpression, destination, importDirectory, inProcessDirectory, failureDirectory, successDirectory);
    }

    private ImportScheduleImpl init(CronExpression cronExpression, DestinationSpec destination, File importDirectory, File inProcessDirectory, File failureDirectory, File successDirectory) {
        this.cronExpression = cronExpression;
        this.cronString = cronExpression.toString();
        this.destination = destination;
        this.destinationName = destination.getName();
        this.importDirectory = importDirectory;
        this.inProcessDirectory = inProcessDirectory;
        this.failureDirectory = failureDirectory;
        this.successDirectory = successDirectory;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public DestinationSpec getDestination() {
        if (destination == null) {
            destination = messageService.getDestinationSpec(destinationName).get();
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
            cronExpression = cronExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
        }
        return cronExpression;
    }

    @Override
    public FileImportImpl createFileImport(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException();
        }
        return FileImportImpl.create(fileSystem, dataModel, fileNameCollisionresolver, thesaurus, this, file);
    }

    @Override
    public void save() {
        if (id == 0) {
            dataModel.mapper(ImportSchedule.class).persist(this);
        } else {
            dataModel.mapper(ImportSchedule.class).update(this);
        }
    }
}
