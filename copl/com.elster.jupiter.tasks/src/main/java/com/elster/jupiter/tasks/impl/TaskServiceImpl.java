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
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.*;
import org.osgi.service.log.LogService;

import java.util.concurrent.TimeUnit;

@Component(name = "com.elster.jupiter.tasks", service = {TaskService.class, InstallService.class}, property = "name=" + TaskService.COMPONENTNAME, immediate = true)
public class TaskServiceImpl implements TaskService, InstallService {

    private DueTaskFetcher dueTaskFetcher;
    private volatile Clock clock;
    private volatile MessageService messageService;
    private volatile LogService logService;
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile JsonService jsonService;

    private Thread schedulerThread;
    private volatile DataModel dataModel;

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(CronExpressionParser.class).toInstance(cronExpressionParser);
                bind(JsonService.class).toInstance(jsonService);
                bind(QueryService.class).toInstance(queryService);
                bind(MessageService.class).toInstance(messageService);
                bind(TransactionService.class).toInstance(transactionService);
            }
        });
    }

    @Override
    public MessageHandler createMessageHandler(TaskExecutor taskExecutor) {
        return new TaskExecutionMessageHandler(dataModel, taskExecutor, jsonService);
    }

    @Deactivate
    public void deactivate() {
    	if (schedulerThread != null) {
    		schedulerThread.interrupt();
    		try {
    			schedulerThread.join();
    		} catch (InterruptedException e) {
    			Thread.currentThread().interrupt();
    		}
        }
    }

    @Override
    public Optional<RecurrentTask> getRecurrentTask(long id) {
        return dataModel.mapper(RecurrentTask.class).getOptional(id);
    }

    @Override
    public void install() {
        new InstallerImpl(dataModel).install();
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
        TaskOccurrenceLauncher taskOccurrenceLauncher = new DefaultTaskOccurrenceLauncher(transactionService, jsonService, getDueTaskFetcher());
        TaskScheduler taskScheduler = new TaskScheduler(taskOccurrenceLauncher, 1, TimeUnit.MINUTES);
        schedulerThread = new Thread(taskScheduler);
        schedulerThread.setName("SchedulerThread");
        schedulerThread.start();
    }

    @Override
    public RecurrentTaskBuilder newBuilder() {
        return new DefaultRecurrentTaskBuilder(dataModel, cronExpressionParser);
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
        DataModel dataModel = ormService.newDataModel(TaskService.COMPONENTNAME, "Jupiter Tasks");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
        this.dataModel = dataModel;
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
        if (dueTaskFetcher == null) {
            dueTaskFetcher = new DueTaskFetcher(dataModel, messageService, cronExpressionParser, clock);
        }
        return dueTaskFetcher;
    }
}
