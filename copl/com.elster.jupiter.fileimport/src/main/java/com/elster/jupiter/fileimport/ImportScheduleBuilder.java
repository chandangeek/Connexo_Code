package com.elster.jupiter.fileimport;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.io.File;

/**
 * Interface for builders for an ImportSchedule
 */
public interface ImportScheduleBuilder {

    /**
     * Builds the ImportSchedule
     * @return
     */
    ImportSchedule build();

    /**
     *
     * @param destination the destination for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setDestination(String destination);

    /**
     *
     * @param importerName the importer name for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setImporterName(String importerName);

    /**
     *
     * @param directory the import directory for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setImportDirectory(File directory);

    /**
     *
     * @param pathMatcher the path matcher for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setPathMatcher(String pathMatcher);
    /**
     *
     * @param directory the processing directory for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setProcessingDirectory(File directory);

    /**
     *
     * @param directory the success directory for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setSuccessDirectory(File directory);

    /**
     *
     * @param directory the failure directory for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setFailureDirectory(File directory);

    /**
     *
     * @param cronExpression the cron expression for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setCronExpression(CronExpression cronExpression);


    PropertyBuilder addProperty(String name);

    interface PropertyBuilder {

        ImportScheduleBuilder withValue(Object value);

    }
}
