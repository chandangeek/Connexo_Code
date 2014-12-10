package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.fileimport.MessageSeeds;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import java.nio.file.Files;
import java.time.Clock;
import java.util.Optional;

import com.google.inject.AbstractModule;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.fileimport", service = {InstallService.class, FileImportService.class}, property = {"name=" + FileImportService.COMPONENT_NAME}, immediate = true)
public class FileImportServiceImpl implements InstallService, FileImportService {

    private static final Logger LOGGER = Logger.getLogger(FileImportServiceImpl.class.getName());
    private static final String COMPONENTNAME = "FIS";

    private volatile DefaultFileSystem defaultFileSystem;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile Clock clock;
    private volatile TransactionService transactionService;
    private volatile JsonService jsonService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    private CronExpressionScheduler cronExpressionScheduler;

    @Override
    public void install() {
        new InstallerImpl(dataModel, thesaurus).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS");
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
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
        defaultFileSystem = new DefaultFileSystem(thesaurus);
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(FileNameCollisionResolver.class).toInstance(getFileNameCollisionResolver());
                bind(FileSystem.class).toInstance(defaultFileSystem);
                bind(MessageService.class).toInstance(messageService);
                bind(DataModel.class).toInstance(dataModel);
                bind(CronExpressionParser.class).toInstance(cronExpressionParser);
                bind(JsonService.class).toInstance(jsonService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });
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
    public void schedule(ImportSchedule importSchedule) {
        cronExpressionScheduler.submit(new ImportScheduleJob(path -> !Files.isDirectory(path), defaultFileSystem, jsonService, importSchedule, transactionService, thesaurus));
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
        return new DefaultImportScheduleBuilder(dataModel);
    }

    @Override
    public MessageHandler createMessageHandler(FileImporter fileImporter) {
        return new StreamImportMessageHandler(dataModel, jsonService, fileImporter);
    }

    public FileNameCollisionResolver getFileNameCollisionResolver() {
        return new SimpleFileNameCollisionResolver(defaultFileSystem);
    }

}
