package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
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

import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.appserver", service = {InstallService.class, AppService.class}, property = {"name=" + AppService.COMPONENT_NAME}, immediate = true)
public class AppServiceImpl implements InstallService, AppService, Subscriber {

    private static final Logger LOGGER = Logger.getLogger(AppServiceImpl.class.getName());

    private static final String APPSERVER_NAME = "com.elster.jupiter.appserver.name";
    private static final String COMPONENT_NAME_KEY = "componentName";
    private static final String TABLE_NAME = "tableName";
    private static final String ID = "id";

    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile BundleContext context;
    private volatile JsonService jsonService;
    private volatile FileImportService fileImportService;
    private volatile TaskService taskService;
    private volatile UserService userService;
    private volatile ThreadFactory threadFactory;
    private volatile Thesaurus thesaurus;

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
                bind(MessageService.class).toInstance(messageService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(CronExpressionParser.class).toInstance(cronExpressionParser);
                bind(JsonService.class).toInstance(jsonService);
                bind(FileImportService.class).toInstance(fileImportService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });

        if (dataModel.isInstalled()) {
            tryActivate(context);
        }
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
        MessageSeeds.APPSERVER_STARTED_ANONYMOUSLY.log(LOGGER, thesaurus);
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
        threadFactory = new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(thesaurus), this);

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
        return new Class<?>[]{InvalidateCacheRequest.class};
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    private void listenForInvalidateCacheRequests() {
        context.registerService(Subscriber.class, this, null);
    }

    private void listenForMesssagesToAllServers() {
        Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(ALL_SERVERS, messagingName());
        if (subscriberSpec.isPresent()) {
            allServerSubscriberSpec = subscriberSpec.get();
            final ExecutorService executorService = new CancellableTaskExecutorService(1, threadFactory);
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(allServerSubscriberSpec, new CommandHandler(), transactionService, thesaurus));
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
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(appServerSubscriberSpec, new CommandHandler(), transactionService, thesaurus));
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
        new Installer(userService, dataModel, messageService, thesaurus).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "MSG", "NLS");
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

    @Override
    public Optional<AppServer> getAppServer() {
        return Optional.<AppServer>fromNullable(appServer);
    }

    @Override
    public List<SubscriberExecutionSpec> getSubscriberExecutionSpecs() {
        return ImmutableList.copyOf(subscriberExecutionSpecs);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(AppService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public void handle(Object notification, Object... notificationDetails) {
        if (notification instanceof InvalidateCacheRequest) {
            InvalidateCacheRequest invalidateCacheRequest = (InvalidateCacheRequest) notification;
            Properties properties = new Properties();
            properties.put(COMPONENT_NAME_KEY, invalidateCacheRequest.getComponentName());
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
                    String componentName = properties.getProperty(COMPONENT_NAME_KEY);
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
