package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.PeriodicalScheduleExpressionParser;
import com.elster.jupiter.time.TemporalExpressionParser;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.CompositeScheduleExpressionParser;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.fileimport", service = {InstallService.class, FileImportService.class}, property = {"name=" + FileImportService.COMPONENT_NAME}, immediate = true)
public class FileImportServiceImpl implements InstallService, FileImportService {

    private static final Logger LOGGER = Logger.getLogger(FileImportServiceImpl.class.getName());
    private static final String COMPONENTNAME = "FIS";

    private volatile FileUtilsImpl fileUtils;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile CompositeScheduleExpressionParser scheduleExpressionParser;
    private volatile Clock clock;
    private volatile TransactionService transactionService;
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile QueryService queryService;
    private volatile FileSystem fileSystem;

    private List<FileImporterFactory> importerFactories = new CopyOnWriteArrayList<>();


    private CronExpressionScheduler cronExpressionScheduler;
    private Path basePath;

    public FileImportServiceImpl(){

    }

    @Inject
    public FileImportServiceImpl(OrmService ormService, MessageService messageService, NlsService nlsService,  QueryService queryService, Clock clock, UserService userService, JsonService jsonService, TransactionService transactionService, CronExpressionParser cronExpressionParser, FileSystem fileSystem) {
        setOrmService(ormService);
        setMessageService(messageService);
        setNlsService(nlsService);
        setQueryService(queryService);
        setClock(clock);
        setUserService(userService);
        setJsonService(jsonService);
        setTransactionService(transactionService);
        setScheduleExpressionParser(cronExpressionParser);
        setFileSystem(fileSystem);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }


    @Override
    public void install() {
        new InstallerImpl(dataModel, messageService, thesaurus, userService).install();
        createScheduler();
    }

    private void createScheduler() {
        if (dataModel.isInstalled()) {
            try {
                List<ImportSchedule> importSchedules = importScheduleFactory().find();
                int poolSize = Math.max(1, (int) Math.log(importSchedules.size()));
                cronExpressionScheduler = new CronExpressionScheduler(clock, poolSize);
            } catch (RuntimeException e) {
                MessageSeeds.FAILED_TO_START_IMPORT_SCHEDULES.log(LOGGER, thesaurus);
            }
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS", "USR");
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setScheduleExpressionParser(CronExpressionParser cronExpressionParser) {
        CompositeScheduleExpressionParser scheduleExpressionParser = new CompositeScheduleExpressionParser();
        scheduleExpressionParser.add(PeriodicalScheduleExpressionParser.INSTANCE);
        scheduleExpressionParser.add(new TemporalExpressionParser());
        scheduleExpressionParser.add(cronExpressionParser);
        scheduleExpressionParser.add(Never.NEVER);
        this.scheduleExpressionParser = scheduleExpressionParser;
        this.cronExpressionParser = cronExpressionParser;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }
    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService= queryService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Jupiter File Import");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(FileImportService.COMPONENT_NAME, Layer.DOMAIN);
        fileUtils = new FileUtilsImpl(thesaurus);
    }


    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFileImporter(FileImporterFactory fileImporterFactory) {
        importerFactories.add(fileImporterFactory);
    }

    public void removeFileImporter(FileImporterFactory fileImporterFactory) {
        importerFactories.remove(fileImporterFactory);
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(FileNameCollisionResolver.class).toInstance(getFileNameCollisionResolver());
                bind(FileUtils.class).toInstance(fileUtils);
                bind(FileSystem.class).toInstance(fileSystem);
                bind(UserService.class).toInstance(userService);
                bind(MessageService.class).toInstance(messageService);
                bind(DataModel.class).toInstance(dataModel);
                bind(ScheduleExpressionParser.class).toInstance(scheduleExpressionParser);
                bind(CronExpressionParser.class).toInstance(cronExpressionParser);
                bind(QueryService.class).toInstance(queryService);
                bind(JsonService.class).toInstance(jsonService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(FileImportService.class).toInstance(FileImportServiceImpl.this);
            }
        });
        createScheduler();
    }

    @Override
    public void schedule(ImportSchedule importSchedule) {
        if(importSchedule.getObsoleteTime() == null)
            cronExpressionScheduler.submit(new ImportScheduleJob(path -> !Files.isDirectory(path), fileUtils, jsonService,
                this, importSchedule.getId(), transactionService, thesaurus, cronExpressionParser, clock));
    }
    @Override
    public void unSchedule(ImportSchedule importSchedule) {
        cronExpressionScheduler.unschedule(importSchedule.getId(), false);
    }

    @Override
    public Optional<ImportSchedule> getImportSchedule(long id) {
        return importScheduleFactory().getOptional(id);
    }


    private DataMapper<ImportSchedule> importScheduleFactory() {
        return dataModel.mapper(ImportSchedule.class);
    }

    @Deactivate
    public void deactivate() {
        cronExpressionScheduler.shutdown();
    }

    @Override
    public ImportScheduleBuilder newBuilder() {
        return new DefaultImportScheduleBuilder(dataModel, this, thesaurus);
    }

    @Override
    public MessageHandler createMessageHandler() {
        return new StreamImportMessageHandler(jsonService, thesaurus, clock, this);
    }

    public FileNameCollisionResolver getFileNameCollisionResolver() {
        return new SimpleFileNameCollisionResolver(fileUtils, fileSystem);
    }

    public Optional<FileImporterFactory> getImportFactory(String name) {

        return importerFactories.stream().filter(factory -> factory.getName().equals(name))
                .findAny();
    }

    @Override
    public List<FileImporterFactory> getAvailableImporters(String applicationName) {
        return importerFactories
                .stream()
                .filter(i -> "SYS".equals(applicationName) || i.getApplicationName().equals(applicationName))
                .collect(Collectors.toList());
    }


    @Override
    public Query<ImportSchedule> getImportSchedulesQuery() {

        return queryService.wrap(dataModel.query(ImportSchedule.class));
    }

    @Override
    public Finder<ImportSchedule> findImportSchedules(String applicationName) {
        Condition condition = Condition.TRUE;
        if(!"SYS".equalsIgnoreCase(applicationName))
            condition = condition.and(Where.where("applicationName").isEqualToIgnoreCase(applicationName));
        condition = condition.and(Where.where("obsoleteTime").isNull());
        return DefaultFinder.of(ImportSchedule.class, condition, dataModel);
    }

    @Override
    public Finder<ImportSchedule> findAllImportSchedules(String applicationName) {
        Condition condition = Condition.TRUE;
        if(!"SYS".equalsIgnoreCase(applicationName))
            condition = condition.and(Where.where("applicationName").isEqualToIgnoreCase(applicationName));
        return DefaultFinder.of(ImportSchedule.class, condition, dataModel);
    }

    @Override
    public FileImportOccurrenceFinderBuilder getFileImportOccurrenceFinderBuilder(String applicationName, Long importScheduleId) {
        Condition condition = Condition.TRUE;
        if(!"SYS".equalsIgnoreCase(applicationName))
            condition = condition.and(Where.where("importSchedule.applicationName").isEqualToIgnoreCase(applicationName));
        if(importScheduleId != null)
            condition = condition.and(Where.where("importScheduleId").isEqualTo(importScheduleId));
        return new FileImportOccurrenceFinderBuilderImpl(dataModel, condition);
    }

    @Override
    public Optional<FileImportOccurrence> getFileImportOccurrence(Long id){
        Optional<FileImportOccurrence> fileImportOccurence = dataModel.mapper(FileImportOccurrence.class).getOptional(id);
        fileImportOccurence.map(FileImportOccurrenceImpl.class::cast).ifPresent(fio -> fio.setClock(clock));
        return fileImportOccurence;
    }

    @Override
    public List<PropertySpec> getPropertiesSpecsForImporter(String importerName) {
        return getImportFactory(importerName)
                .map(FileImporterFactory::getPropertySpecs)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<ImportSchedule> getImportSchedules() {
        return dataModel.mapper(ImportSchedule.class).select(where("obsoleteTime").isNull());
    }

    @Override
    public Optional<ImportSchedule> getImportSchedule(String name) {
        return getImportSchedulesQuery()
                .select(where("name").isEqualTo(name).and(Where.where("obsoleteTime").isNull()))
                .stream()
                .findFirst();
    }

    @Override
    public void setBasePath(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public Path getBasePath() {
        if(this.basePath == null)
            this.basePath = fileSystem.getPath("/");
        return this.basePath;
    }



}
