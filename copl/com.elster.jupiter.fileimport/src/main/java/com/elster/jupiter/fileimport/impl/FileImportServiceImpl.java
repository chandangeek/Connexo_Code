package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
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
import com.elster.jupiter.util.Only;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.fileimport", service = {InstallService.class, FileImportService.class}, property = {"name=" + FileImportService.COMPONENT_NAME}, immediate = true)
public class FileImportServiceImpl implements InstallService, FileImportService {

    private static final Logger LOGGER = Logger.getLogger(FileImportServiceImpl.class.getName());
    private static final String COMPONENTNAME = "FIS";

    private volatile DefaultFileSystem defaultFileSystem;
    private volatile LogService logService;
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
        new InstallerImpl(dataModel).install();
    }

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
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
            }
        });
        try {
            List<ImportSchedule> importSchedules = importScheduleFactory().find();
            int poolSize = Math.max(1, (int) Math.log(importSchedules.size()));
            cronExpressionScheduler = new CronExpressionScheduler(clock, poolSize);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Could not start Import schedules, please check if FIM is installed properly.");
            throw e;
		}
    }

    @Override
    public void schedule(ImportSchedule importSchedule) {
        cronExpressionScheduler.submit(new ImportScheduleJob(new Only(), defaultFileSystem, jsonService, importSchedule, transactionService, thesaurus));
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
        return new DefaultImportScheduleBuilder(messageService, dataModel, cronExpressionParser, getFileNameCollisionResolver(), defaultFileSystem, thesaurus);
    }

    @Override
    public MessageHandler createMessageHandler(FileImporter fileImporter) {
        return new StreamImportMessageHandler(dataModel, jsonService, fileImporter);
    }

    public FileNameCollisionResolver getFileNameCollisionResolver() {
        return new SimpleFileNameCollisionResolver(defaultFileSystem);
    }

}
