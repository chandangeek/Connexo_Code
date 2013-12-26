package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.appserver", service = {InstallService.class, AppService.class}, property = {"name=" + "APS"}, immediate = true)
public class AppServiceImpl implements InstallService, AppService, Subscriber {

    private static final Logger LOGGER = Logger.getLogger(AppServiceImpl.class.getName());

    private static final String APPSERVER_NAME = "com.elster.jupiter.appserver.name";
    private static final String COMPONENT_NAME = "componentName";
    private static final String TABLE_NAME = "tableName";
    private static final String ID = "id";
    private static final String BATCH_EXECUTOR = "batch executor";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile LogService logService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile BundleContext context;
    private volatile Publisher publisher;
    private volatile JsonService jsonService;
    private volatile FileImportService fileImportService;
    private volatile TaskService taskService;
    private volatile UserService userService;
    private volatile ThreadFactory threadFactory;

    private AppServerImpl appServer;
    private List<SubscriberExecutionSpec> subscriberExecutionSpecs = Collections.emptyList();
    private SubscriberSpec allServerSubscriberSpec;
    private final List<Runnable> deactivateTasks = new ArrayList<>();

    @Activate
    public void activate(BundleContext context) {
        this.context = context;

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(AppServerCreator.class).to(DefaultAppServerCreator.class);
            }
        });

        tryActivate(context);
    }

    private void tryActivate(BundleContext context) {
        String appServerName = context.getProperty(APPSERVER_NAME);
        if (appServerName != null) {
            activateAs(appServerName);
        } else {
            activateAnonymously();
        }
    }

    private void activateAnonymously() {
        LOGGER.log(Level.WARNING, "AppServer started anonymously.");
    }

    private void activateAs(String appServerName) {
        Optional<AppServer> foundAppServer = dataModel.mapper(AppServer.class).getOptional(appServerName);
        if (!foundAppServer.isPresent()) {
            LOGGER.log(Level.SEVERE, "AppServer with name " + appServerName + " not found.");
            activateAnonymously();
            return;
        }
        appServer = (AppServerImpl) foundAppServer.get();
        subscriberExecutionSpecs = appServer.getSubscriberExecutionSpecs();

        ThreadGroup threadGroup = new ThreadGroup("AppServer message listeners");
        threadFactory = new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(), this);

        launchFileImports();
        launchTaskService();
        listenForMessagesToAppServer();
        listenForMesssagesToAllServers();
        listenForInvalidateCacheRequests();
    }

    private void launchTaskService() {
        if (appServer.isRecurrentTaskActive()) {
            getTaskService().launch();
        }
    }

    private void launchFileImports() {
        List<ImportScheduleOnAppServer> importScheduleOnAppServers = dataModel.mapper(ImportScheduleOnAppServer.class).find("appServer", appServer);
        for (ImportScheduleOnAppServer importScheduleOnAppServer : importScheduleOnAppServers) {
            fileImportService.schedule(importScheduleOnAppServer.getImportSchedule());
        }
    }

    @Override
    public Class<?>[] getClasses() {
    	return new Class<?>[] {InvalidateCacheRequest.class};
    }
    
    private void listenForInvalidateCacheRequests() {
        context.registerService(Subscriber.class, this, null);
    }

    private void listenForMesssagesToAllServers() {
        Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(ALL_SERVERS, messagingName());
        if (subscriberSpec.isPresent()) {
            allServerSubscriberSpec = subscriberSpec.get();
            final ExecutorService executorService = new CancellableTaskExecutorService(1, threadFactory);
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(allServerSubscriberSpec, new CommandHandler(), transactionService));
            deactivateTasks.add(new Runnable() {
                @Override
                public void run() {
                    cancellableTask.cancel(false);
                    executorService.shutdownNow();
                }
            });
        }
    }

    private void listenForMessagesToAppServer() {
        Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(messagingName(), messagingName());
        if (subscriberSpec.isPresent()) {
            SubscriberSpec appServerSubscriberSpec = subscriberSpec.get();
            final ExecutorService executorService = new CancellableTaskExecutorService(1, threadFactory);
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(appServerSubscriberSpec, new CommandHandler(), transactionService));
            deactivateTasks.add(new Runnable() {
                @Override
                public void run() {
                    cancellableTask.cancel(false);
                    executorService.shutdownNow();
                }
            });
        }
    }

    @Deactivate
    public void deactivate() {
        this.context = null;
        for (Runnable deactivateTask : deactivateTasks) {
            deactivateTask.run();
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel("APS", "Jupiter Application Server");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Override
    public AppServer createAppServer(final String name, final CronExpression cronExpression) {
        return dataModel.getInstance(AppServerCreator.class).createAppServer(name, cronExpression);
    }

    private String messagingName() {
        return appServer.messagingName();
    }

    @Override
    public void install() {
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        try {
            User user = userService.createUser(BATCH_EXECUTOR, "User to execute batch tasks.");
            user.save();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get();
            defaultQueueTableSpec.createDestinationSpec(ALL_SERVERS, DEFAULT_RETRY_DELAY_IN_SECONDS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public Optional<AppServer> getAppServer() {
        return Optional.<AppServer>fromNullable(appServer);
    }

    @Override
    public List<SubscriberExecutionSpec> getSubscriberExecutionSpecs() {
        return ImmutableList.copyOf(subscriberExecutionSpecs);
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Override
    public void handle(Object event, Object... eventDetails) {
        if (event instanceof InvalidateCacheRequest) {
            InvalidateCacheRequest invalidateCacheRequest = (InvalidateCacheRequest) event;
            Properties properties = new Properties();
            properties.put(COMPONENT_NAME, invalidateCacheRequest.getComponentName());
            properties.put(TABLE_NAME, invalidateCacheRequest.getTableName());
            AppServerCommand command = new AppServerCommand(Command.INVALIDATE_CACHE, properties);

            allServerSubscriberSpec.getDestination().message(jsonService.serialize(command)).send();
        }

    }

    public TaskService getTaskService() {
        return taskService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private class CommandHandler implements MessageHandler {

        @Override
        public void process(Message message) {
            AppServerCommand command = jsonService.deserialize(message.getPayload(), AppServerCommand.class);
            switch (command.getCommand()) {
                case STOP:
                    stop();
                    break;
                case INVALIDATE_CACHE:
                    Properties properties = command.getProperties();
                    String componentName = properties.getProperty(COMPONENT_NAME);
                    String tableName = properties.getProperty(TABLE_NAME);
                    ormService.invalidateCache(componentName, tableName);
                    break;
                case FILEIMPORT_ACTIVATED:
                    String idAsString = command.getProperties().getProperty(ID);
                    ImportSchedule importSchedule = fileImportService.getImportSchedule(Long.valueOf(idAsString)).get();
                    fileImportService.schedule(importSchedule);
                default:
            }
        }
    }

    public void stop() {
        Thread stoppingThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            context.getBundle(0).stop();
                        } catch (BundleException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                });
        stoppingThread.start();
        Thread.currentThread().interrupt();

    }

    DataModel getDataModel() {
        return dataModel;
    }
}
