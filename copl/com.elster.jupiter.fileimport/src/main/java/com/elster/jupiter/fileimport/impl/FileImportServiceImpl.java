package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.List;

@Component(name = "com.elster.jupiter.fileimport", service = {InstallService.class, FileImportService.class}, property = {"name=" + Bus.COMPONENTNAME}, immediate = true)
public class FileImportServiceImpl implements InstallService, ServiceLocator, FileImportService {

    private volatile LogService logService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile OrmClient ormClient;
    private volatile Clock clock;
    private volatile TransactionService transactionService;
    private volatile JsonService jsonService;

    private Thread thread;
    private CronExpressionScheduler cronExpressionScheduler;

    @Override
    public void install() {
        new InstallerImpl().install();
    }

    @Override
    public LogService getLogService() {
        return logService;
    }

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public CronExpressionParser getCronExpressionParser() {
        return cronExpressionParser;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
    }

    @Override
    public JsonService getJsonService() {
        return jsonService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter File Import");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void activate(ComponentContext context) {
        Bus.setServiceLocator(this);
        try {
            List<ImportSchedule> importSchedules = getOrmClient().getImportScheduleFactory().find();
            int poolSize = Math.max(1, (int) Math.log(importSchedules.size()));
            cronExpressionScheduler = new CronExpressionScheduler(poolSize);
        } catch (RuntimeException e) {
			e.printStackTrace();
            getLogService().log(LogService.LOG_ERROR, "Could not start Import schedules, please check if FIM is installed properly.");
		}
    }

    @Override
    public void schedule(ImportSchedule importSchedule) {
        cronExpressionScheduler.submit(new ImportScheduleJob(importSchedule));
    }

    @Override
    public ImportSchedule getImportSchedule(long id) {
        return getOrmClient().getImportScheduleFactory().get(id).get();
    }

    public void deactivate(ComponentContext context) {
        thread.interrupt();
        cronExpressionScheduler.shutdown();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bus.setServiceLocator(null);
    }

    @Override
    public ImportScheduleBuilder newBuilder() {
        return new DefaultImportScheduleBuilder();
    }

    @Override
    public MessageHandler createMessageHandler(FileImporter fileImporter) {
        return new StreamImportMessageHandler(fileImporter);
    }
}
