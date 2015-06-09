package com.elster.jupiter.fileimport;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.io.File;
import java.nio.file.Path;

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
    ImportScheduleBuilder setImportDirectory(Path directory);

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
    ImportScheduleBuilder setProcessingDirectory(Path directory);

    /**
     *
     * @param directory the success directory for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setSuccessDirectory(Path directory);

    /**
     *
     * @param directory the failure directory for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setFailureDirectory(Path directory);

    /**
     *
     * @param scheduleExpression the schedule expression for the ImportSchedule under construction
     * @return this
     */
    ImportScheduleBuilder setScheduleExpression(ScheduleExpression scheduleExpression);




    PropertyBuilder addProperty(String name);

    ImportScheduleBuilder setName(String name);

     interface PropertyBuilder {

        ImportScheduleBuilder withValue(Object value);

    }
}
