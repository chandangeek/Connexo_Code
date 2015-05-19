package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
    private transient ScheduleExpression scheduleExpression;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String importerName;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String cronString;
    private final MessageService messageService;
    private final DataModel dataModel;
    private final ScheduleExpressionParser scheduleExpressionParser;
    private final FileNameCollisionResolver fileNameCollisionresolver;
    private final FileSystem fileSystem;
    private final Thesaurus thesaurus;
    private final FileImportService fileImportService;
    boolean propertiesDirty;

    private List<FileImporterProperty> properties = new ArrayList<>();
    private boolean active;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @Pattern(regexp="^$|[a-zA-Z0-9\\-' '_]+", groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Constants.INVALID_CHARS +"}")
    private String name;

    @SuppressWarnings("unused")
    @Inject
	ImportScheduleImpl(DataModel dataModel, FileImportService fileImportService, MessageService messageService, ScheduleExpressionParser scheduleExpressionParser, FileNameCollisionResolver fileNameCollisionresolver, FileSystem fileSystem, Thesaurus thesaurus) {
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.scheduleExpressionParser = scheduleExpressionParser;
        this.fileNameCollisionresolver = fileNameCollisionresolver;
        this.fileSystem = fileSystem;
        this.thesaurus = thesaurus;
        this.fileImportService = fileImportService;
    }

    static ImportScheduleImpl from(DataModel dataModel, String name, boolean active, ScheduleExpression scheduleExpression, String importerName, String destination,
                                   File importDirectory, String pathMatcher, File inProcessDirectory, File failureDirectory, File successDirectory) {
        return dataModel.getInstance(ImportScheduleImpl.class).init(name, active, scheduleExpression, importerName, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory);
    }

    private ImportScheduleImpl init( String name, boolean active, ScheduleExpression scheduleExpression, String importerName, String destinationName, File importDirectory, String pathMatcher, File inProcessDirectory, File failureDirectory, File successDirectory) {
        this.name = name;
        this.active = active;
        this.cronString = scheduleExpression.toString();
        this.destinationName = destinationName;
        this.importerName = importerName;
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
    public boolean isActive() {
        return this.active;
    }

    @Override
    public String getName() {
        return this.name;
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
    public ScheduleExpression getScheduleExpression() {
        if (scheduleExpression == null) {
            scheduleExpression = scheduleExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
        }
        return scheduleExpression;
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
    public Map<String, Object> getProperties() {
        return properties.stream()
                .collect(Collectors.toMap(FileImporterProperty::getName, FileImporterProperty::getValue));
    }

    @Override
    public void delete() {
        if (id == 0) {
            return;
        }
        properties.clear();
        dataModel.remove(this);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        this.cronString = scheduleExpression.encoded();
    }

    @Override
    public void setImportDirectory(File importDirectory) {
        this.importDirectory = importDirectory;
    }

    @Override
    public void setFailureDirectory(File failureDirectory) {
        this.failureDirectory = failureDirectory;
    }

    @Override
    public void setSuccessDirectory(File successDirectory) {
        this.successDirectory = successDirectory;
    }

    @Override
    public void setProcessingDirectory(File inProcessDirectory) {
        this.inProcessDirectory = inProcessDirectory;
    }

    @Override
    public void setImporterName(String importerName) {
        this.importerName = importerName;
    }

    @Override
    public void setPathMatcher(String pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    public void setDestination(String destinationName) {
        this.destinationName = destinationName;
    }

    @Override
    public String getApplicationName() {
        Optional<FileImporterFactory> optional = fileImportService.getImportFactory(importerName);
        if (optional.isPresent()) {
            FileImporterFactory importerFactory = optional.get();
            return importerFactory.getApplicationName();
        }
        return null;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
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
    }

    private void update() {

        if (propertiesDirty) {
            properties.forEach(FileImporterProperty::save);
        }
        Save.UPDATE.save(dataModel, this);
    }


}
