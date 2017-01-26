package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.RecurrentTaskFilterSpecification;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskFinder;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.TaskServiceAlreadyLaunched;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.tasks.security.Privileges;
import com.elster.jupiter.time.PeriodicalScheduleExpressionParser;
import com.elster.jupiter.time.TemporalExpressionParser;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_3SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.CompositeScheduleExpressionParser;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.tasks",
           service = { TaskService.class, TranslationKeyProvider.class, MessageSeedProvider.class },
           property = "name=" + TaskService.COMPONENTNAME, immediate = true)
public class TaskServiceImpl implements TaskService, TranslationKeyProvider, MessageSeedProvider {

    private DueTaskFetcher dueTaskFetcher;
    private volatile Clock clock;
    private volatile MessageService messageService;
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile CompositeScheduleExpressionParser scheduleExpressionParser;
    private volatile JsonService jsonService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

    private Thread schedulerThread;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public TaskServiceImpl() {
        super();
    }

    // For unit test purposes only
    @Inject
    public TaskServiceImpl(OrmService ormService, Clock clock, MessageService messageService, QueryService queryService, TransactionService transactionService, CronExpressionParser cronExpressionParser, JsonService jsonService, NlsService nlsService, ThreadPrincipalService threadPrincipalService, UserService userService, UpgradeService upgradeService) {
        this();
        this.setOrmService(ormService);
        this.setClock(clock);
        this.setMessageService(messageService);
        this.setQueryService(queryService);
        this.setTransactionService(transactionService);
        this.setScheduleExpressionParser(cronExpressionParser);
        this.setJsonService(jsonService);
        this.setNlsService(nlsService);
        this.setThreadPrincipalService(threadPrincipalService);
        this.setUserService(userService);
        this.setUpgradeService(upgradeService);
        this.activate();
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(ScheduleExpressionParser.class).toInstance(scheduleExpressionParser);
                bind(JsonService.class).toInstance(jsonService);
                bind(QueryService.class).toInstance(queryService);
                bind(MessageService.class).toInstance(messageService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(TaskService.class).toInstance(TaskServiceImpl.this);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME), dataModel, InstallerImpl.class,
            ImmutableMap.of(
                Version.version(10, 3), V10_3SimpleUpgrader.class
            )
        );
    }

    @Override
    public MessageHandler createMessageHandler(TaskExecutor taskExecutor) {
        return new TaskExecutionMessageHandler(dataModel, taskExecutor, jsonService, transactionService);
    }

    @Deactivate
    public void deactivate() {
        doShutDown();
    }

    private void doShutDown() {
        if (schedulerThread != null) {
            schedulerThread.interrupt();
            try {
                schedulerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            schedulerThread = null;
        }
    }

    @Override
    public Query<? extends RecurrentTask> getTaskQuery() {
        return queryService.wrap(dataModel.query(RecurrentTask.class));
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
    public List<RecurrentTask> getRecurrentTasks() {
        return dataModel.stream(RecurrentTask.class).select();
    }

    @Override
    public TaskFinder getTaskFinder(RecurrentTaskFilterSpecification filterSpecification, int start, int limit) {
        return new RecurrentTaskFinder(dataModel, filterSpecification, start, limit);
    }

    @Override
    public Optional<RecurrentTask> getRecurrentTask(long id) {
        return dataModel.mapper(RecurrentTask.class).getOptional(id);
    }

    @Override
    public QueryExecutor<TaskOccurrence> getTaskOccurrenceQueryExecutor() {
        return dataModel.query(TaskOccurrence.class);
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
        TaskOccurrenceLauncher taskOccurrenceLauncher = new DefaultTaskOccurrenceLauncher(threadPrincipalService, transactionService, getDueTaskFetcher());
        TaskScheduler taskScheduler = new TaskScheduler(taskOccurrenceLauncher, 1, TimeUnit.MINUTES, factory -> Executors.newScheduledThreadPool(1, factory));
        schedulerThread = new Thread(threadPrincipalService.withContextAdded(taskScheduler, () -> "TaskService"));
        schedulerThread.setName("SchedulerThread");
        schedulerThread.start();
    }

    @Override
    public void shutDown() {
        doShutDown();
    }

    @Override
    public RecurrentTaskBuilder newBuilder() {
        return new DefaultRecurrentTaskBuilder(dataModel, scheduleExpressionParser);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setScheduleExpressionParser(CronExpressionParser cronExpressionParser) {
        CompositeScheduleExpressionParser scheduleExpressionParser = new CompositeScheduleExpressionParser();
        scheduleExpressionParser.add(PeriodicalScheduleExpressionParser.INSTANCE);
        scheduleExpressionParser.add(new TemporalExpressionParser());
        scheduleExpressionParser.add(cronExpressionParser);
        scheduleExpressionParser.add(Never.NEVER);
        this.scheduleExpressionParser = scheduleExpressionParser;
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
    public void setUserService(UserService userService) {
        this.userService = userService;
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    void setDueTaskFetcher(DueTaskFetcher dueTaskFetcher) {
        this.dueTaskFetcher = dueTaskFetcher;
    }

    private DueTaskFetcher getDueTaskFetcher() {
        if (dueTaskFetcher == null) {
            dueTaskFetcher = new DueTaskFetcher(dataModel, messageService, scheduleExpressionParser, clock);
        }
        return dueTaskFetcher;
    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(TaskStatus.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}
