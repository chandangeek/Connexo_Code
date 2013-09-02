package com.elster.jupiter.fileimport;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.io.File;

/**
 * Entity that models when a certain directory should be scanned for files to import and where to move them during different stages of the import cycle.
 */
public interface ImportSchedule {

    /**
     * @return this entity's id
     */
    long getId();

    /**
     * @return the Destination on which to post a message when a file needs processing.
     */
    DestinationSpec getDestination();

    /**
     * @return File representing the directory to scan for new files to import
     */
    File getImportDirectory();

    /**
     * @return File representing the directory where files are moved for awaiting processing.
     */
    File getInProcessDirectory();

    /**
     * @return File representing the directory where files are moved once they've been processed successfully.
     */
    File getSuccessDirectory();

    /**
     * @return File representing the directory where files are moved in case they could not be processed successfully.
     */
    File getFailureDirectory();

    /**
     * @return a CronExpression that indicates the times at which the import directory should be scanned.
     */
    CronExpression getScheduleExpression();

    /**
     * Persists or updates this instance.
     */
    void save();

    /**
     * @param file
     * @return creates a new FileImport instance for the given file.
     */
    FileImport createFileImport(File file);
}
