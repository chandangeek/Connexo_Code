package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 15/09/2015
 * Time: 11:24
 */
public class ImportScheduleBuilder extends com.elster.jupiter.demo.impl.builders.NamedBuilder<ImportSchedule, ImportScheduleBuilder>{

    private FileSystem fileSystem;
    private FileImportService fileImportService;

    private String fileImporterFactoryName;
    private String pathMatcher;
    private ScheduleExpression scheduleExpression;
    private Path importDirectory;
    private Path failureDirectory;
    private Path successDirectory;
    private Path inProcessDirectory;
    private Map<String,Object> importProperties = new HashMap<>();

    @Inject
    public ImportScheduleBuilder(FileSystem fileSystem, FileImportService fileImportService){
        super(ImportScheduleBuilder.class);
        this.fileImportService = fileImportService;
        this.fileSystem = fileSystem;
    }

    public ImportScheduleBuilder withFileImporterFactoryName(String fileImporterFactoryName){
        this.fileImporterFactoryName = fileImporterFactoryName;
        return this;
    }

    public ImportScheduleBuilder withPathMatcher(String pathMatcher){
        this.pathMatcher = pathMatcher;
        return this;
    }

    public ImportScheduleBuilder withScheduleExpression(ScheduleExpression scheduleExpression){
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    public ImportScheduleBuilder withImportDirectory(String importDirectory){
        this.importDirectory = fileSystem.getPath(importDirectory);
        return this;
    }

    public ImportScheduleBuilder withFailureDirectory(String failureDirectory){
        this.failureDirectory = fileSystem.getPath(failureDirectory);
        return this;
    }

    public ImportScheduleBuilder withSuccessDirectory(String successDirectory){
        this.successDirectory = fileSystem.getPath(successDirectory);
        return this;
    }

    public ImportScheduleBuilder withInProcessDirectory(String InProcessDirectory){
        this.inProcessDirectory = fileSystem.getPath(InProcessDirectory);
        return this;
    }

    public ImportScheduleBuilder withProperties(Map<String, Object> importProperties){
        this.importProperties = importProperties;
        return this;
    }

    @Override
    public Optional<ImportSchedule> find() {
        return fileImportService.getImportSchedule(getName());
    }

    @Override
    public ImportSchedule create() {
        Log.write(this);

        FileImporterFactory importerFactory = fileImportService.getImportFactory(fileImporterFactoryName).get();

        com.elster.jupiter.fileimport.ImportScheduleBuilder builder =
                 fileImportService.newBuilder()
                 .setName(getName())
                .setImporterName(importerFactory.getName())
                .setDestination(importerFactory.getDestinationName())
                .setScheduleExpression(scheduleExpression)
                .setImportDirectory(importDirectory)
                .setFailureDirectory(failureDirectory)
                .setSuccessDirectory(successDirectory)
                .setProcessingDirectory(inProcessDirectory)
                         .setPathMatcher(pathMatcher);

        importProperties.keySet().stream().forEach(propname -> builder.addProperty(propname).withValue(importProperties.get(propname)));

        ImportSchedule importSchedule = builder.build();
        importSchedule.save();

        return importSchedule;
    }
}
