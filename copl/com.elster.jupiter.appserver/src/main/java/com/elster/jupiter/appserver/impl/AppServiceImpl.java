package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.InvalidateCacheRequest;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Component(name = "com.elster.jupiter.appserver", service = {InstallService.class, AppService.class}, property = {"name=" + Bus.COMPONENTNAME, "osgi.command.scope=jupiter", "osgi.command.function=create", "osgi.command.function=executeSubscription", "osgi.command.function=activateFileImport"}, immediate = true)
public class AppServiceImpl implements ServiceLocator, InstallService, AppService, Subscriber {

    private static final String APPSERVER_NAME = "com.elster.jupiter.appserver.name";
    private static final String ALL_SERVERS = "AllServers";
    private static final String COMPONENT_NAME = "componentName";
    private static final String TABLE_NAME = "tableName";
    private static final String ID = "id";
    private static final String BATCH_EXECUTOR = "batch executor";

    private volatile OrmClient ormClient;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile LogService logService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ComponentContext context;
    private volatile Publisher publisher;
    private volatile CacheService cacheService;
    private volatile JsonService jsonService;
    private volatile FileImportService fileImportService;
    private volatile TaskService taskService;
    private volatile UserService userService;

    private AppServerImpl appServer;
    private List<SubscriberExecutionSpec> subscriberExecutionSpecs = Collections.emptyList();
    private SubscriberSpec appServerSubscriberSpec;
    private SubscriberSpec allServerSubscriberSpec;
    private List<Runnable> deactivateTasks = new ArrayList<>();

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    public void activate(ComponentContext context) {
        try {
            this.context = context;
            tryActivate(context);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void tryActivate(ComponentContext context) {
        Bus.setServiceLocator(this);
        String appServerName = context.getBundleContext().getProperty(APPSERVER_NAME);
        if (appServerName != null) {
            activateAs(appServerName);
        } else {
            activateAnonymously();
        }
    }

    private void activateAnonymously() {
        getLogService().log(LogService.LOG_WARNING, "AppServer started anonymously.");
    }

    private void activateAs(String appServerName) {
        Optional<AppServer> foundAppServer = Bus.getOrmClient().getAppServerFactory().get(appServerName);
        if (!foundAppServer.isPresent()) {
            getLogService().log(LogService.LOG_ERROR, "AppServer with name " + appServerName + " not found.");
            activateAnonymously();
            return;
        }
        appServer = (AppServerImpl) foundAppServer.get();
        subscriberExecutionSpecs = appServer.getSubscriberExecutionSpecs();

        launchFileImports();
        launchTaskService();
        listenForMessagesToAppServer();
        listenForMesssagesToAllServers();
        listenForInvalidateCacheRequests();
    }

    private void launchTaskService() {
        if (appServer.isRecurrentTaskActive()) {
            User batchExecutor = getUserService().findUser(BATCH_EXECUTOR).get();
            getTaskService().launch();
        }
    }

    private void launchFileImports() {
        List<ImportScheduleOnAppServer> importScheduleOnAppServers = getOrmClient().getImportScheduleOnAppServerFactory().find("appServer", appServer);
        for (ImportScheduleOnAppServer importScheduleOnAppServer : importScheduleOnAppServers) {
            getFileImportService().schedule(importScheduleOnAppServer.getImportSchedule());
        }
    }

    private void listenForInvalidateCacheRequests() {
        new EventHandler<InvalidateCacheRequest>(InvalidateCacheRequest.class) {
            public void onEvent(InvalidateCacheRequest request, Object... eventDetails) {
                Properties properties = new Properties();
                properties.put(COMPONENT_NAME, request.getComponentName());
                properties.put(TABLE_NAME, request.getTableName());
                AppServerCommand command = new AppServerCommand(Command.INVALIDATE_CACHE, properties);

                allServerSubscriberSpec.getDestination().message(getJsonService().serialize(command)).send();
            }
        }.register(context.getBundleContext());

        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put(Subscriber.TOPIC, new Class[] { InvalidateCacheRequest.class });
        context.getBundleContext().registerService(Subscriber.class, this, dictionary);
    }

    private void listenForMesssagesToAllServers() {
        Optional<SubscriberSpec> subscriberSpec = getMessageService().getSubscriberSpec(ALL_SERVERS, messagingName());
        if (subscriberSpec.isPresent()) {
            allServerSubscriberSpec = subscriberSpec.get();
            final ExecutorService executorService = new CancellableTaskExecutorService(1);
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(allServerSubscriberSpec, new CommandHandler()));
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
        Optional<SubscriberSpec> subscriberSpec = getMessageService().getSubscriberSpec(messagingName(), messagingName());
        if (subscriberSpec.isPresent()) {
            appServerSubscriberSpec = subscriberSpec.get();
            final ExecutorService executorService = new CancellableTaskExecutorService(1);
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(appServerSubscriberSpec, new CommandHandler()));
            deactivateTasks.add(new Runnable() {
                @Override
                public void run() {
                    cancellableTask.cancel(false);
                    executorService.shutdownNow();
                }
            });
        }
    }

    public void deactivate(ComponentContext context) {
        this.context = null;
        for (Runnable deactivateTask : deactivateTasks) {
            deactivateTask.run();
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        try {
            DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter Application Server");
            for (TableSpecs each : TableSpecs.values()) {
                each.addTo(dataModel);
            }
            this.ormClient = new OrmClientImpl(dataModel);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public AppServer createAppServer(final String name, final CronExpression cronExpression) {
        return getTransactionService().execute(new Transaction<AppServer>() {
            @Override
            public AppServer perform() {
                AppServer server = new AppServerImpl(name, cronExpression);
                getOrmClient().getAppServerFactory().persist(server);
                QueueTableSpec defaultQueueTableSpec = getMessageService().getQueueTableSpec("MSG_RAWQUEUETABLE").get();
                DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(messagingName(), 60);
                destinationSpec.subscribe(messagingName());
                Optional<DestinationSpec> allServersTopic = getMessageService().getDestinationSpec(ALL_SERVERS);
                if (allServersTopic.isPresent()) {
                    allServersTopic.get().subscribe(messagingName());
                }
                return server;
            }
        });
    }

    public void executeSubscription(final String subscriberName, final String destinationName, final int threads) {
        if (appServer == null) {
            System.out.println("Cannot execute subscriptions from anonymous app server.");
            return;
        }
        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<SubscriberSpec> subscriberSpec = getMessageService().getSubscriberSpec(destinationName, subscriberName);
                if (!subscriberSpec.isPresent()) {
                    System.out.println("Subscriber not found.");
                }
                appServer.createSubscriberExecutionSpec(subscriberSpec.get(), threads);
            }
        });
    }

    private String messagingName() {
        return appServer.messagingName();
    }

    @Override
    public void install() {
        try {
            getOrmClient().install();
            getTransactionService().execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    User user = getUserService().createUser(BATCH_EXECUTOR, "User to execute batch tasks.");
                    user.save();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        QueueTableSpec defaultQueueTableSpec = getMessageService().getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        defaultQueueTableSpec.createDestinationSpec(ALL_SERVERS, 60);
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
    public LogService getLogService() {
        return logService;
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

    public CacheService getCacheService() {
        return cacheService;
    }

    @Reference
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public AppServer getAppServer() {
        return appServer;
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

    @Override
    public JsonService getJsonService() {
        return jsonService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public FileImportService getFileImportService() {
        return fileImportService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    public void create(String name, String cronString) {
        getThreadPrincipalService().set(new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        });
        try {
            createAppServer(name, getCronExpressionParser().parse(cronString));
        } finally {
            getThreadPrincipalService().set(null);
        }
    }

    public void activateFileImport(long id, String appServerName) {
        final AppServer appServerToActivateOn = getAppServerForActivation(appServerName);
        if (appServerToActivateOn == null) {
            System.out.println("AppServer not found.");
            return;
        }
        final ImportSchedule importSchedule = getFileImportService().getImportSchedule(id);
        getTransactionService().execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                doActivateFileImport(appServerToActivateOn, importSchedule);
            }
        });
    }

    private AppServer getAppServerForActivation(String appServerName) {
        AppServer appServerToActivateOn = null;
        if (appServer != null && appServer.getName().equals(appServerName)) {
            appServerToActivateOn = appServer;
        } else {
            Optional<AppServer> found = getOrmClient().getAppServerFactory().get(appServerName);
            appServerToActivateOn = found.orNull();
        }
        return appServerToActivateOn;
    }

    private void doActivateFileImport(AppServer appServerToActivateOn, ImportSchedule importSchedule) {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = new ImportScheduleOnAppServerImpl(importSchedule, appServerToActivateOn);
        importScheduleOnAppServer.save();
    }

    @Override
    public void handle(Object event, Object... eventDetails) {
        if (event instanceof InvalidateCacheRequest) {
            InvalidateCacheRequest invalidateCacheRequest = (InvalidateCacheRequest) event;
            Properties properties = new Properties();
            properties.put(COMPONENT_NAME, invalidateCacheRequest.getComponentName());
            properties.put(TABLE_NAME, invalidateCacheRequest.getTableName());
            AppServerCommand command = new AppServerCommand(Command.INVALIDATE_CACHE, properties);

            allServerSubscriberSpec.getDestination().message(getJsonService().serialize(command)).send();
        }

    }

    public TaskService getTaskService() {
        return taskService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private class CommandHandler implements MessageHandler {

        @Override
        public void process(Message message) throws SQLException {
            AppServerCommand command = getJsonService().deserialize(message.getPayload(), AppServerCommand.class);
            switch (command.getCommand()) {
                case STOP:
                    stop();
                    break;
                case INVALIDATE_CACHE:
                    Properties properties = command.getProperties();
                    String componentName = properties.getProperty(COMPONENT_NAME);
                    String tableName = properties.getProperty(TABLE_NAME);
                    cacheService.refresh(componentName, tableName);
                    break;
                case FILEIMPORT_ACTIVATED:
                    String idAsString = command.getProperties().getProperty(ID);
                    ImportSchedule importSchedule = getFileImportService().getImportSchedule(Long.valueOf(idAsString));
                    getFileImportService().schedule(importSchedule);
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
                            System.out.println("Stopping AppServer");

                            context.getBundleContext().getBundle(0).stop();
                            System.out.println("Stopped");
                        } catch (BundleException e) {
                            e.printStackTrace();
                        }
                    }
                });
        stoppingThread.start();
        Thread.currentThread().interrupt();

    }

}
