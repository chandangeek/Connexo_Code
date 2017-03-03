/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.fileimport.NonNullPath;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ImportSchedule implementation.
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_IMPORT_SCHEDULE + "}")
@NotSamePath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_THE_SAME_AS_IMPORT_FOLDER + "}")
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
final class ImportScheduleImpl implements ServerImportSchedule {

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String destinationName;

    private transient DestinationSpec destination;

    @NonNullPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.VALID_PATH_REQUIRED + "}")
    @NonEmptyPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private Path importDirectory;
    @NonNullPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.VALID_PATH_REQUIRED + "}")
    @NonEmptyPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private Path inProcessDirectory;
    @NonNullPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.VALID_PATH_REQUIRED + "}")
    @NonEmptyPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private Path successDirectory;
    @NonNullPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.VALID_PATH_REQUIRED + "}")
    @NonEmptyPath(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private Path failureDirectory;

    private boolean activeInUI;

    private String pathMatcher;
    private transient ScheduleExpression scheduleExpression;

    private String applicationName;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String importerName;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String cronString;

    private final MessageService messageService;
    private final EventService eventService;
    private final DataModel dataModel;
    private final ScheduleExpressionParser scheduleExpressionParser;
    private final FileNameCollisionResolver fileNameCollisionresolver;
    private final FileUtils fileUtils;
    private final FileSystem fileSystem;
    private final Thesaurus thesaurus;
    private final FileImportService fileImportService;
    private final Clock clock;
    private boolean propertiesDirty;

    private List<FileImporterProperty> properties = new ArrayList<>();
    private boolean active;
    private Instant obsoleteTime;

    //audit fields
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @Pattern(regexp = "^$|[a-zA-Z0-9\\-' '_]+", groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_CHARS + "}")
    private String name;

    @SuppressWarnings("unused")
    @Inject
    ImportScheduleImpl(DataModel dataModel, FileImportService fileImportService, MessageService messageService, EventService eventService, ScheduleExpressionParser scheduleExpressionParser, FileNameCollisionResolver fileNameCollisionresolver, FileUtils fileUtils, Thesaurus thesaurus, FileSystem fileSystem, Clock clock) {
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.scheduleExpressionParser = scheduleExpressionParser;
        this.fileNameCollisionresolver = fileNameCollisionresolver;
        this.fileUtils = fileUtils;
        this.thesaurus = thesaurus;
        this.fileImportService = fileImportService;
        this.fileSystem = fileSystem;
        this.clock = clock;
    }

    /**
     * @deprecated
     * New method below this one must be used instead.
     * {@link ImportScheduleImpl from(DataModel dataModel, String name, boolean active, ScheduleExpression scheduleExpression, String applicationName, String importerName, String destination,
    Path importDirectory, String pathMatcher, Path inProcessDirectory, Path failureDirectory, Path successDirectory, boolean isActiveOnUI)}
     * It has new parameter activeInUI which defines possibility to use import schedule via user interface
     * **/
    @Deprecated
    static ImportScheduleImpl from(DataModel dataModel, String name, boolean active, ScheduleExpression scheduleExpression, String applicationName, String importerName, String destination,
                                   Path importDirectory, String pathMatcher, Path inProcessDirectory, Path failureDirectory, Path successDirectory) {
        return dataModel.getInstance(ImportScheduleImpl.class).init(name, active, scheduleExpression, applicationName, importerName, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory, false);
    }

    static ImportScheduleImpl from(DataModel dataModel, String name, boolean active, ScheduleExpression scheduleExpression, String applicationName, String importerName, String destination,
                                   Path importDirectory, String pathMatcher, Path inProcessDirectory, Path failureDirectory, Path successDirectory, boolean isActiveOnUI) {
        return dataModel.getInstance(ImportScheduleImpl.class).init(name, active, scheduleExpression, applicationName, importerName, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory, isActiveOnUI);
    }

    private ImportScheduleImpl init(String name, boolean active, ScheduleExpression scheduleExpression, String applicationName, String importerName, String destinationName, Path importDirectory, String pathMatcher, Path inProcessDirectory, Path failureDirectory, Path successDirectory, boolean activeInUI) {
        this.name = name;
        this.active = active;
        this.scheduleExpression = scheduleExpression;
        this.cronString = scheduleExpression.toString();
        this.destinationName = destinationName;
        this.importerName = importerName;
        this.importDirectory = importDirectory;
        this.inProcessDirectory = inProcessDirectory;
        this.failureDirectory = failureDirectory;
        this.successDirectory = successDirectory;
        this.pathMatcher = pathMatcher;
        this.applicationName = applicationName;
        this.activeInUI = activeInUI;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isActive() {
        return !this.isDeleted() && this.active;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public DestinationSpec getDestination() {
        if (destination == null) {
            if (fileImportService.getImportFactory(importerName).isPresent()) {
                String destinationName = fileImportService.getImportFactory(importerName)
                        .orElseThrow(() -> new IllegalArgumentException("No such file importer: " + importerName))
                        .getDestinationName();
                destination = messageService.getDestinationSpec(destinationName).get();
            }
        }
        return destination;
    }

    @Override
    public Path getImportDirectory() {
        return importDirectory;
    }

    @Override
    public String getPathMatcher() {
        return pathMatcher;
    }

    @Override
    public Path getInProcessDirectory() {
        return inProcessDirectory;
    }

    @Override
    public Path getSuccessDirectory() {
        return successDirectory;
    }

    @Override
    public Path getFailureDirectory() {
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
    public String getImporterName() {
        return importerName;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        if (fileImportService.getImportFactory(importerName).isPresent()) {
            return fileImportService.getImportFactory(importerName).get().getPropertySpecs();
        }
        return Collections.emptyList();
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

    public boolean activeInUI() {
        return activeInUI;
    }

    public void setActiveInUI(boolean isActiveOnUI) {
        this.activeInUI = active;
    }
    
    @Override
    public void setProperty(String name, Object value) {
        FileImporterPropertyImpl fileImporterProperty = properties.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .map(FileImporterPropertyImpl.class::cast)
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
        if (fileImportService.getImportFactory(importerName).isPresent()) {
            return properties.stream()
                    .collect(Collectors.toMap(FileImporterProperty::getName, FileImporterProperty::getValue));
        }
        return Collections.emptyMap();
    }

    @Override
    public void delete() {
        if (id == 0) {
            return;
        }
        setObsoleteTime(Instant.now(clock)); // mark obsolete
        doUpdate();
    }

    public Instant getObsoleteTime() {
        return obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
        eventService.postEvent(EventType.IMPORT_SCHEDULE_DELETED.topic(), this);
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
    public void setImportDirectory(Path importDirectory) {
        this.importDirectory = importDirectory;
    }

    @Override
    public void setFailureDirectory(Path failureDirectory) {
        this.failureDirectory = failureDirectory;
    }

    @Override
    public void setSuccessDirectory(Path successDirectory) {
        this.successDirectory = successDirectory;
    }

    @Override
    public void setProcessingDirectory(Path inProcessDirectory) {
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
        /*Optional<FileImporterFactory> optional = fileImportService.getImportFactory(importerName);
        if (optional.isPresent()) {
            FileImporterFactory importerFactory = optional.get();
            return importerFactory.getApplicationName();
        }
        return null;*/
        return applicationName;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean isDeleted() {
        return (this.obsoleteTime != null);
    }

    @Override
    public boolean isImporterAvailable() {
        return fileImportService.getImportFactory(importerName).isPresent();
    }

    @Override
    public Finder<FileImportOccurrence> getFileImportOccurrences() {
        return DefaultFinder.of(FileImportOccurrence.class, Where.where("importScheduleId").isEqualTo(getId()), dataModel);
    }

    @Override
    public Optional<FileImportOccurrence> getFileImportOccurrence(long occurrenceId) {
        return dataModel.mapper(FileImportOccurrence.class).getOptional(occurrenceId);
    }

    @Override
    public long getVersion() {
        return this.version;
    }


    @Override
    public FileImportOccurrenceImpl createFileImportOccurrence(Path file, Clock clock) {
        if (!Files.exists(file)) {
            throw new IllegalArgumentException();
        }
        Path relativeFilePath = fileImportService.getBasePath().relativize(file);
        return FileImportOccurrenceImpl.create(fileImportService, fileUtils, dataModel, fileNameCollisionresolver, thesaurus, clock, this, relativeFilePath);
    }

    @Override
    public void update() {
        save();
        eventService.postEvent(EventType.IMPORT_SCHEDULE_UPDATED.topic(), this);
    }

    void save() {

        Optional<FileImporterFactory> optional = fileImportService.getImportFactory(importerName);
        optional.ifPresent(factory -> factory.validateProperties(properties));
        /*Map<String, Object> propertiesWithValuesMap =  properties
                .stream()
                .filter(p->!p.useDefault())
                .collect(Collectors.toMap(FileImporterProperty::getName, FileImporterProperty::getValue));

        optional.ifPresent(factory -> factory.getRequiredProperties()
                .forEach(propertyName -> checkRequiredProperty(propertyName,propertiesWithValuesMap)));
*/
        if (id == 0) {
            persist();
        } else {
            doUpdate();
        }
        propertiesDirty = false;
    }

    private void persist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.IMPORT_SCHEDULE_CREATED.topic(), this);
    }

    private void doUpdate() {

        if (propertiesDirty) {
            properties.stream().map(FileImporterPropertyImpl.class::cast).forEach(FileImporterPropertyImpl::save);
        }
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportScheduleImpl that = (ImportScheduleImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
