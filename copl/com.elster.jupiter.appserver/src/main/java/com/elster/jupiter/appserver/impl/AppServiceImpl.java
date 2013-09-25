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
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.InvalidateCacheRequest;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.appserver", service = {InstallService.class, AppService.class}, property = {"name=" + Bus.COMPONENTNAME}, immediate = true)
public class AppServiceImpl implements ServiceLocator, InstallService, AppService, Subscriber {

    private static final Logger logger = Logger.getLogger(AppServiceImpl.class.getName());

    private static final String APPSERVER_NAME = "com.elster.jupiter.appserver.name";
    private static final String COMPONENT_NAME = "componentName";
    private static final String TABLE_NAME = "tableName";
    private static final String ID = "id";
    private static final String BATCH_EXECUTOR = "batch executor";
    private AppServerCreator appServerCreator = new DefaultAppServerCreator();

    private volatile OrmClient ormClient;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile LogService logService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile BundleContext context;
    private volatile Publisher publisher;
    private volatile CacheService cacheService;
    private volatile JsonService jsonService;
    private volatile FileImportService fileImportService;
    private volatile TaskService taskService;
    private volatile UserService userService;

    private AppServerImpl appServer;
    private List<SubscriberExecutionSpec> subscriberExecutionSpecs = Collections.emptyList();
    private SubscriberSpec allServerSubscriberSpec;
    private final List<Runnable> deactivateTasks = new ArrayList<>();

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        tryActivate(context);
    }

    private void tryActivate(BundleContext context) {
        Bus.setServiceLocator(this);
        String appServerName = context.getProperty(APPSERVER_NAME);
        if (appServerName != null) {
            activateAs(appServerName);
        } else {
            activateAnonymously();
        }
    }

    private void activateAnonymously() {
        logger.log(Level.WARNING, "AppServer started anonymously.");
    }

    private void activateAs(String appServerName) {
        Optional<AppServer> foundAppServer = Bus.getOrmClient().getAppServerFactory().get(appServerName);
        if (!foundAppServer.isPresent()) {
            logger.log(Level.SEVERE, "AppServer with name " + appServerName + " not found.");
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
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put(Subscriber.TOPIC, new Class[]{InvalidateCacheRequest.class});
        context.registerService(Subscriber.class, this, dictionary);
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
            SubscriberSpec appServerSubscriberSpec = subscriberSpec.get();
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
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter Application Server");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Override
    public AppServer createAppServer(final String name, final CronExpression cronExpression) {
        return getAppServerCreator().createAppServer(name, cronExpression);
    }

    private String messagingName() {
        return appServer.messagingName();
    }

    @Override
    public void install() {
        try {
            getOrmClient().install();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            User user = getUserService().createUser(BATCH_EXECUTOR, "User to execute batch tasks.");
            user.save();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            QueueTableSpec defaultQueueTableSpec = getMessageService().getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            defaultQueueTableSpec.createDestinationSpec(ALL_SERVERS, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public AppServerCreator getAppServerCreator() {
        return appServerCreator;
    }

    void setAppServerCreator(AppServerCreator appServerCreator) {
        this.appServerCreator = appServerCreator;
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
        public void process(Message message) {
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
                    ImportSchedule importSchedule = getFileImportService().getImportSchedule(Long.valueOf(idAsString)).get();
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

                            context.getBundle(0).stop();
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
