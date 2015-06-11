package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class DefaultImportScheduleBuilder implements ImportScheduleBuilder {

    private List<PropertyBuilderImpl> properties = new ArrayList<>();
    private String name;
    private String destination;
    private String importerName;
    private Path importDirectory;
    private String pathMatcher;
    private Path inProcessDirectory;
    private Path successDirectory;
    private Path failureDirectory;
    private transient ScheduleExpression scheduleExpression;
    private final DataModel dataModel;
    private final FileImportService fileImportService;
    private final Thesaurus thesaurus;

    DefaultImportScheduleBuilder(DataModel dataModel, FileImportService fileImportService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.fileImportService = fileImportService;
        this.thesaurus = thesaurus;
    }

    @Override
    public ImportSchedule build() {

        String applicationName = null;
        if(fileImportService.getImportFactory(importerName).isPresent()){
            applicationName = fileImportService.getImportFactory(importerName).get().getApplicationName();
        }
        fileImportService.getImportFactory(importerName).ifPresent(in -> destination = in.getDestinationName());

        ImportScheduleImpl importSchedule = ImportScheduleImpl.from(dataModel, name, false, scheduleExpression, applicationName, importerName, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory);
        properties.stream().forEach(p -> importSchedule.setProperty(p.name, p.value));
        return importSchedule;
    }



    @Override
    public ImportScheduleBuilder setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public ImportScheduleBuilder setImporterName(String importerName) {
        this.importerName = importerName;
        return this;
    }

    @Override
    public ImportScheduleBuilder setImportDirectory(Path directory) {
        this.importDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setPathMatcher(String pathMatcher ) {
        this.pathMatcher = pathMatcher ;
        return this;
    }

    @Override
    public ImportScheduleBuilder setProcessingDirectory(Path directory) {
        this.inProcessDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setSuccessDirectory(Path directory) {
        this.successDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setFailureDirectory(Path directory) {
        this.failureDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public PropertyBuilder addProperty(String name) {
        return new PropertyBuilderImpl(name);
    }

    @Override
    public ImportScheduleBuilder setName(String name) {
        this.name = name;
        return this;
    }


    private class PropertyBuilderImpl implements PropertyBuilder {
        private final String name;
        private Object value;

        private PropertyBuilderImpl(String name) {
            this.name = name;
        }

        @Override
        public ImportScheduleBuilder withValue(Object value) {
            this.value = value;
            properties.add(this);
            return DefaultImportScheduleBuilder.this;
        }
    }
}
