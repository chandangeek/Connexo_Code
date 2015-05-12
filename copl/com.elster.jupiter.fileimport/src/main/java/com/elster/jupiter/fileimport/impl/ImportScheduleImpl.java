package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private String pathMatcher;
    private String importerName;
    private transient CronExpression cronExpression;
    private String cronString;
    private final MessageService messageService;
    private final DataModel dataModel;
    private final CronExpressionParser cronExpressionParser;
    private final FileNameCollisionResolver fileNameCollisionresolver;
    private final FileSystem fileSystem;
    private final Thesaurus thesaurus;
    private final FileImportService fileImportService;
    boolean propertiesDirty;

    private List<FileImporterProperty> properties = new ArrayList<>();

    @SuppressWarnings("unused")
    @Inject
	ImportScheduleImpl(DataModel dataModel, FileImportService fileImportService, MessageService messageService, CronExpressionParser cronExpressionParser, FileNameCollisionResolver fileNameCollisionresolver, FileSystem fileSystem, Thesaurus thesaurus) {
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
        this.fileNameCollisionresolver = fileNameCollisionresolver;
        this.fileSystem = fileSystem;
        this.thesaurus = thesaurus;
        this.fileImportService = fileImportService;
    }

    static ImportScheduleImpl from(DataModel dataModel, CronExpression cronExpression, String destination, File importDirectory, String pathMatcher, File inProcessDirectory, File failureDirectory, File successDirectory) {
        return dataModel.getInstance(ImportScheduleImpl.class).init(cronExpression, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory);
    }

    private ImportScheduleImpl init(CronExpression cronExpression, String destinationName, File importDirectory, String pathMatcher, File inProcessDirectory, File failureDirectory, File successDirectory) {
        this.cronExpression = cronExpression;
        this.cronString = cronExpression.toString();
        this.destinationName = destinationName;
        this.importDirectory = importDirectory;
        this.inProcessDirectory = inProcessDirectory;
        this.failureDirectory = failureDirectory;
        this.successDirectory = successDirectory;
        this.pathMatcher = pathMatcher;
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
    public String getPathMatcher() {
        return pathMatcher;
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
    public String getImporterName(){
        return importerName;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return fileImportService.getImportFactory(importerName).orElseThrow(() -> new IllegalArgumentException("No such file importer: " + importerName)).getProperties();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getPropertyDisplayName(String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .findAny()
                .map(FileImporterProperty::getDisplayName)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public List<FileImporterProperty> getImporterProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public void setProperty(String name, Object value) {
        FileImporterProperty fileImporterProperty = properties.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    FileImporterPropertyImpl property = FileImporterPropertyImpl.from(dataModel, this, name, value);
                    properties.add(property);
                    return property;
                });
        fileImporterProperty.setValue(value);
        propertiesDirty = true;
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

        Optional<FileImporterFactory> optional = fileImportService.getImportFactory(importerName);
        if (optional.isPresent()) {
            FileImporterFactory importerFactory = optional.get();
            importerFactory.validateProperties(properties);
        }

        if (id == 0) {
            persist();
        } else {
            update();
        }
        propertiesDirty = false;
    }

    private void persist() {

        Save.CREATE.save(dataModel, this);
        dataModel.mapper(ImportSchedule.class).persist(this);
    }

    private void update() {

        if (propertiesDirty) {
            properties.forEach(FileImporterProperty::save);
        }
        Save.UPDATE.save(dataModel, this);
        //dataModel.mapper(ImportSchedule.class).update(this);
    }


}
