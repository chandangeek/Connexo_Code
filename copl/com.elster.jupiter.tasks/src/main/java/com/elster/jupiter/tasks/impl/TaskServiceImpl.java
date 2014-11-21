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
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.TaskServiceAlreadyLaunched;
import com.elster.jupiter.time.TemporalExpressionParser;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.tasks", service = {TaskService.class, InstallService.class}, property = "name=" + TaskService.COMPONENTNAME, immediate = true)
public class TaskServiceImpl implements TaskService, InstallService {

    private DueTaskFetcher dueTaskFetcher;
    private volatile Clock clock;
    private volatile MessageService messageService;
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile JsonService jsonService;

    private Thread schedulerThread;
    private volatile DataModel dataModel;

    // For OSGi framework
    public TaskServiceImpl() {
        super();
    }

    // For unit test purposes only
    @Inject
    public TaskServiceImpl(OrmService ormService, Clock clock, MessageService messageService, QueryService queryService, TransactionService transactionService, CronExpressionParser cronExpressionParser, JsonService jsonService) {
        this();
        this.setOrmService(ormService);
        this.setClock(clock);
        this.setMessageService(messageService);
        this.setQueryService(queryService);
        this.setTransactionService(transactionService);
        this.setCronExpressionParser(cronExpressionParser);
        this.setJsonService(jsonService);
        this.activate();
        this.install();
    }

    @Activate
    public void activate() {
        CompositeScheduleExpressionParser scheduleExpressionParser = new CompositeScheduleExpressionParser();
        scheduleExpressionParser.add(new TemporalExpressionParser());
        scheduleExpressionParser.add(cronExpressionParser);
        scheduleExpressionParser.add(Never.NEVER);
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(ScheduleExpressionParser.class).toInstance(scheduleExpressionParser);
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
    public List<TaskOccurrence> getOccurrences(RecurrentTask recurrentTask, Range<Instant> period) {
        return dataModel.query(TaskOccurrence.class)
                .select(
                        where("recurrentTaskId").isEqualTo(recurrentTask.getId())
                                .and(where("triggerTime").in(period))
                );
    }

    @Override
    public Optional<TaskOccurrence> getOccurrence(Long id) {
        return dataModel.mapper(TaskOccurrence.class).getOptional(id);
    }

    @Override
    public Optional<RecurrentTask> getRecurrentTask(String name) {
        return dataModel.stream(RecurrentTask.class).filter(Where.where("name").isEqualTo(name)).findFirst();
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
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM");
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
        TaskOccurrenceLauncher taskOccurrenceLauncher = new DefaultTaskOccurrenceLauncher(transactionService, getDueTaskFetcher());
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
