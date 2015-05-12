package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class DefaultImportScheduleBuilder implements ImportScheduleBuilder {

    private List<PropertyBuilderImpl> properties = new ArrayList<>();
    private String destination;
    private File importDirectory;
    private String pathMatcher;
    private File inProcessDirectory;
    private File successDirectory;
    private File failureDirectory;
    private transient CronExpression cronExpression;
    private final DataModel dataModel;

    DefaultImportScheduleBuilder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ImportSchedule build() {
        ImportScheduleImpl importSchedule = ImportScheduleImpl.from(dataModel, cronExpression, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory);
        properties.stream().forEach(p -> importSchedule.setProperty(p.name, p.value));
        return importSchedule;
    }

    @Override
    public ImportScheduleBuilder setDestination(String destination) {
        this.destination = destination;
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
    public ImportScheduleBuilder setCronExpression(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    @Override
    public PropertyBuilder addProperty(String name) {
        return new PropertyBuilderImpl(name);
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
