package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.TaskServiceAlreadyLaunched;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;

import com.google.common.base.Optional;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.concurrent.TimeUnit;

@Component(name = "com.elster.jupiter.tasks", service = {TaskService.class, InstallService.class}, property = "name=" + Bus.COMPONENTNAME, immediate = true)
public class TaskServiceImpl implements TaskService, ServiceLocator, InstallService {

    private DueTaskFetcher dueTaskFetcher = new DueTaskFetcher();
    private volatile Clock clock;
    private volatile MessageService messageService;
    private volatile OrmClient ormClient;
    private volatile LogService logService;
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile JsonService jsonService;

    private Thread schedulerThread;

    public void activate(ComponentContext context) {
        Bus.setServiceLocator(this);
    }

    @Override
    public MessageHandler createMessageHandler(TaskExecutor taskExecutor) {
        return new TaskExecutionMessageHandler(taskExecutor);
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
    public Clock getClock() {
        return clock;
    }

    @Override
    public CronExpressionParser getCronExpressionParser() {
        return cronExpressionParser;
    }

    @Override
    public JsonService getJsonService() {
        return jsonService;
    }

    @Override
    public LogService getLogService() {
        return logService;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public Optional<RecurrentTask> getRecurrentTask(long id) {
        return getOrmClient().getRecurrentTaskFactory().get(id);
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public void install() {
        new InstallerImpl().install();
    }

    @Override
    public boolean isLaunched() {
        return schedulerThread != null;
    }

    @Override
    public void launch() {
        if (isLaunched()) {
            throw new TaskServiceAlreadyLaunched();
        }
        TaskOccurrenceLauncher taskOccurrenceLauncher = new DefaultTaskOccurrenceLauncher(getDueTaskFetcher());
        TaskScheduler taskScheduler = new TaskScheduler(taskOccurrenceLauncher, 1, TimeUnit.MINUTES);
        schedulerThread = new Thread(taskScheduler);
        schedulerThread.setName("SchedulerThread");
        schedulerThread.start();
    }

    @Override
    public RecurrentTaskBuilder newBuilder() {
        return new DefaultRecurrentTaskBuilder(getCronExpressionParser());
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter Tasks");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    void setDueTaskFetcher(DueTaskFetcher dueTaskFetcher) {
        this.dueTaskFetcher = dueTaskFetcher;
    }

    private DueTaskFetcher getDueTaskFetcher() {
        return dueTaskFetcher;
    }
}
