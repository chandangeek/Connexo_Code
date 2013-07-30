package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.consumer.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.concurrent.TimeUnit;

@Component(name = "com.elster.jupiter.tasks", service = {TaskService.class, InstallService.class}, property = "name=" + Bus.COMPONENTNAME, immediate=true)
public class TaskServiceImpl implements TaskService, ServiceLocator, InstallService {

    private volatile Clock clock;
    private volatile MessageService messageService;
    private volatile OrmClient ormClient;
    private volatile LogService logService;
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile JsonService jsonService;

    private Thread schedulerThread;

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public RecurrentTaskBuilder newBuilder() {
        return new DefaultRecurrentTaskBuilder(getCronExpressionParser());
    }

    @Override
    public MessageHandler createMessageHandler(TaskExecutor taskExecutor) {
        return new TaskExecutionMessageHandler(taskExecutor);
    }

    @Override
    public RecurrentTask getRecurrentTask(long id) {
        return getOrmClient().getRecurrentTaskFactory().get(id).get();
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter Tasks");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
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
    public QueryService getQueryService() {
        return queryService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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

    public void activate(ComponentContext context) {
        Bus.setServiceLocator(this);
        TaskOccurrenceLauncher taskOccurrenceLauncher = new LoggingTaskOcurrenceLauncher(new DefaultTaskOccurrenceLauncher(new DueTaskFetcher()));
        TaskScheduler taskScheduler = new TaskScheduler(taskOccurrenceLauncher, 1, TimeUnit.MINUTES);
        schedulerThread = new Thread(taskScheduler);
        schedulerThread.setName("SchedulerThread");
        schedulerThread.start();
    }

    public void deactivate(ComponentContext context) {
        schedulerThread.interrupt();
        try {
            schedulerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Bus.setServiceLocator(null);
    }

    @Override
    public void install() {
        new InstallerImpl().install();

    }
}
