/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import com.elster.jupiter.util.time.ScheduleExpression;

import aQute.bnd.annotation.ProviderType;

import java.nio.file.Path;

/**
 * Interface for builders for an ImportSchedule
 */
@ProviderType
public interface ImportScheduleBuilder {

    /**
     * Builds the ImportSchedule
     * @return
     */
    ImportSchedule create();

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

    /**
     * @param activeInUI defines possibility for file import via user interface
     * @return this
     */
    ImportScheduleBuilder setActiveInUI(boolean activeInUI);

    PropertyBuilder addProperty(String name);

    ImportScheduleBuilder setName(String name);

     interface PropertyBuilder {

        ImportScheduleBuilder withValue(Object value);

    }
}
