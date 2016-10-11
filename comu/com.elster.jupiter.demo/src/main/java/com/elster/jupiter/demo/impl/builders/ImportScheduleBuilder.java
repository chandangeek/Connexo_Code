package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Copyrights EnergyICT
 * Date: 15/09/2015
 * Time: 11:24
 */
public class ImportScheduleBuilder extends NamedBuilder<ImportSchedule, ImportScheduleBuilder>{

    private FileSystem fileSystem;
    private FileImportService fileImportService;
    private List<Consumer<ImportSchedule>> postBuilders;

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

        importProperties.keySet().stream().forEach(propName -> builder.addProperty(propName).withValue(importProperties.get(propName)));
        ImportSchedule importSchedule = builder.create();
        applyPostBuilders(importSchedule);
        return importSchedule;
    }
}
