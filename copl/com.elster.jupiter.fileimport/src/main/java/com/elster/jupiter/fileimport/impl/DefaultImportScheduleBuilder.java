package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class DefaultImportScheduleBuilder implements ImportScheduleBuilder {

    private List<PropertyBuilderImpl> properties = new ArrayList<>();
    private String name;
    private String destination;
    private String importerName;
    private File importDirectory;
    private String pathMatcher;
    private File inProcessDirectory;
    private File successDirectory;
    private File failureDirectory;
    private transient ScheduleExpression scheduleExpression;
    private final DataModel dataModel;

    DefaultImportScheduleBuilder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ImportSchedule build() {
        ImportScheduleImpl importSchedule = ImportScheduleImpl.from(dataModel, name, false, scheduleExpression, importerName, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory);
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
    public ImportScheduleBuilder setImportDirectory(File directory) {
        this.importDirectory = directory;
        return this;
    }

    @Override
    public ImportScheduleBuilder setPathMatcher(String pathMatcher ) {
        this.pathMatcher = pathMatcher ;
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
