package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ImportSchedule implementation.
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_IMPORT_SCHEDULE + "}")
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
class ImportScheduleImpl implements ImportSchedule {

    private final JsonService jsonService;
    private static final Logger LOGGER = Logger.getLogger(ImportScheduleImpl.class.getName());
    private long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String destinationName;

    private transient DestinationSpec destination;


    private Path importDirectory;
    private Path inProcessDirectory;
    private Path successDirectory;
    private Path failureDirectory;
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
    private final DataModel dataModel;
    private final ScheduleExpressionParser scheduleExpressionParser;
    private final FileNameCollisionResolver fileNameCollisionresolver;
    private final FileSystem fileSystem;
    private final Thesaurus thesaurus;
    private final FileImportService fileImportService;
    boolean propertiesDirty;

    private List<FileImporterProperty> properties = new ArrayList<>();
    private boolean active;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    private Instant obsoleteTime;



    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @Pattern(regexp="^$|[a-zA-Z0-9\\-' '_]+", groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Constants.INVALID_CHARS +"}")
    private String name;

    @SuppressWarnings("unused")
    @Inject
	ImportScheduleImpl(DataModel dataModel, FileImportService fileImportService, MessageService messageService, ScheduleExpressionParser scheduleExpressionParser, FileNameCollisionResolver fileNameCollisionresolver, FileSystem fileSystem,JsonService jsonService, Thesaurus thesaurus) {
        this.messageService = messageService;
        this.dataModel = dataModel;
        this.jsonService = jsonService;
        this.scheduleExpressionParser = scheduleExpressionParser;
        this.fileNameCollisionresolver = fileNameCollisionresolver;
        this.fileSystem = fileSystem;
        this.thesaurus = thesaurus;
        this.fileImportService = fileImportService;
    }

    static ImportScheduleImpl from(DataModel dataModel, String name, boolean active, ScheduleExpression scheduleExpression, String applicationName, String importerName, String destination,
                                   Path importDirectory, String pathMatcher, Path inProcessDirectory, Path failureDirectory, Path successDirectory) {
        return dataModel.getInstance(ImportScheduleImpl.class).init(name, active, scheduleExpression, applicationName, importerName, destination, importDirectory, pathMatcher, inProcessDirectory, failureDirectory, successDirectory);
    }

    private ImportScheduleImpl init( String name, boolean active, ScheduleExpression scheduleExpression, String applicationName, String importerName, String destinationName, Path importDirectory, String pathMatcher, Path inProcessDirectory, Path failureDirectory, Path successDirectory) {
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
            String destinationName = fileImportService.getImportFactory(importerName)
                    .orElseThrow(() -> new IllegalArgumentException("No such file importer: " + importerName))
                    .getDestinationName();
            destination = messageService.getDestinationSpec(destinationName).get();
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
    public String getImporterName(){
        return importerName;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return fileImportService.getImportFactory(importerName).orElseThrow(() -> new IllegalArgumentException("No such file importer: " + importerName)).getPropertySpecs();
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
        setObsoleteTime(Instant.now()); // mark obsolete
        update();
    }

    public Instant getObsoleteTime() {
        return obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
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
    public Finder<FileImportOccurrence> getFileImportOccurrences() {
        return DefaultFinder.of(FileImportOccurrence.class,  Where.where("importScheduleId").isEqualTo(getId()), dataModel);
    }

    @Override
    public Optional<FileImportOccurrence>  getFileImportOccurrence(long occurrenceId) {
        return dataModel.mapper(FileImportOccurrence.class).getOptional(occurrenceId);
    }


    @Override
    public FileImportOccurrence createFileImportOccurrence(Path file, Clock clock) {
        if (!Files.exists(file)) {
            throw new IllegalArgumentException();
        }
        Path relativeFilePath = fileImportService.getBasePath().relativize(file);
        return FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionresolver, thesaurus, clock, this,relativeFilePath );
    }

    public Logger getLogger(FileImportOccurrence occurrence) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createFileImportLogHandler().asHandler());
        return logger;
    }


    private void checkRequiredProperty(String propertyName, Map<String, Object> properties) {
        if (!properties.containsKey(propertyName)) {
            throw new MissingRequiredProperty(thesaurus, propertyName);
        }
    }


    @Override
    public void save() {

        Optional<FileImporterFactory> optional = fileImportService.getImportFactory(importerName);
        optional.ifPresent(factory->factory.validateProperties(properties));
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
