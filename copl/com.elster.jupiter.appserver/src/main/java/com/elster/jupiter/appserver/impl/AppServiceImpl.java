package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.*;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
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
import com.elster.jupiter.util.Registration;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import static com.elster.jupiter.util.conditions.Where.where;

import com.elster.jupiter.util.streams.Functions;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.appserver", service = {InstallService.class, AppService.class}, property = {"name=" + AppService.COMPONENT_NAME}, immediate = true)
public class AppServiceImpl implements InstallService, IAppService, Subscriber {

    private static final Logger LOGGER = Logger.getLogger(AppServiceImpl.class.getName());

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
    private volatile Thesaurus thesaurus;

    private volatile AppServerImpl appServer;
    private volatile List<? extends SubscriberExecutionSpec> subscriberExecutionSpecs = Collections.emptyList();
    private SubscriberSpec allServerSubscriberSpec;
    private final List<Runnable> deactivateTasks = new ArrayList<>();
    private List<CommandListener> commandListeners = new CopyOnWriteArrayList<>();
    private final ThreadGroup threadGroup;
    private QueryService queryService;

    public AppServiceImpl() {
        threadGroup = new ThreadGroup("AppServer message listeners");
    }

    @Inject
    AppServiceImpl(OrmService ormService, NlsService nlsService, TransactionService transactionService, MessageService messageService, CronExpressionParser cronExpressionParser, JsonService jsonService, FileImportService fileImportService, TaskService taskService, UserService userService, QueryService queryService, BundleContext bundleContext) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setTransactionService(transactionService);
        setMessageService(messageService);
        setCronExpressionParser(cronExpressionParser);
        setJsonService(jsonService);
        setFileImportService(fileImportService);
        setTaskService(taskService);
        setUserService(userService);
        setQueryService(queryService);

        if (!dataModel.isInstalled()) {
            install();
        }
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext context) {
        try {
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
                    bind(AppService.class).toInstance(AppServiceImpl.this);
                    bind(IAppService.class).toInstance(AppServiceImpl.this);
                }
            });

            if (dataModel.isInstalled()) {
                tryActivate(context);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void tryActivate(BundleContext context) {
        String appServerName = determineAppServerName(context);
        if (appServerName != null) {
            activateAs(appServerName);
        } else {
            activateAnonymously();
        }
    }

    private String determineAppServerName(BundleContext context) {
        return getAppServerNameFromProperty(context).orElseGet(() -> {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return null;
            }
        });
    }

    private Optional<String> getAppServerNameFromProperty(BundleContext context) {
        return Optional.ofNullable(context.getProperty(SERVER_NAME_PROPERTY_NAME));
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

        launchFileImports();
        launchTaskService();
        listenForMessagesToAppServer();
        listenForMesssagesToAllServers();
        listenForInvalidateCacheRequests();
    }

    private void launchTaskService() {
        if (appServer.isRecurrentTaskActive()) {
            getTaskService().launch();
            deactivateTasks.add(() -> getTaskService().shutDown());
        }
    }

    private void launchFileImports() {
        Optional<ImportFolderForAppServer> appServerImportFolder = dataModel.mapper(ImportFolderForAppServer.class).getOptional(appServer.getName());
        if(!appServerImportFolder.isPresent()){
            LOGGER.log(Level.WARNING, "AppServer with name " + appServer.getName() + " has no import folder configured.");
            return;
        }
        if(!appServerImportFolder.get().getImportFolder().isPresent()){
            LOGGER.log(Level.WARNING, "AppServer with name " + appServer.getName() + " import folder is configured but cannot be resolved as path.");
            return;
        }
        appServerImportFolder.flatMap(ImportFolderForAppServer::getImportFolder)
                .ifPresent(path -> fileImportService.setBasePath(path));

        List<ImportScheduleOnAppServer> importScheduleOnAppServers = dataModel.mapper(ImportScheduleOnAppServer.class).find("appServer", appServer);
        importScheduleOnAppServers.stream()
                .map(ImportScheduleOnAppServer::getImportSchedule)
                .flatMap(Functions.asStream())
                .filter(is -> is.getObsoleteTime() == null)
                .forEach(fileImportService::schedule);
    }

    @Override
    public Class<?>[] getClasses() {
        return new Class<?>[]{InvalidateCacheRequest.class};
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    private void listenForInvalidateCacheRequests() {
        context.registerService(Subscriber.class, this, null);
    }

    private void listenForMesssagesToAllServers() {
        Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(ALL_SERVERS, messagingName());
        if (subscriberSpec.isPresent()) {
            allServerSubscriberSpec = subscriberSpec.get();
            final ExecutorService executorService = new CancellableTaskExecutorService(1, new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(thesaurus), this, () -> "All Server messages"));
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(allServerSubscriberSpec, new CommandHandler(), transactionService, thesaurus));
            deactivateTasks.add(() -> {
                cancellableTask.cancel(false);
                executorService.shutdownNow();
            });
        }
    }

    private void listenForMessagesToAppServer() {
        Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(messagingName(), messagingName());
        if (subscriberSpec.isPresent()) {
            SubscriberSpec appServerSubscriberSpec = subscriberSpec.get();
            final ExecutorService executorService = new CancellableTaskExecutorService(1, new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(thesaurus), this, () -> "This AppServer messages"));
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(appServerSubscriberSpec, new CommandHandler(), transactionService, thesaurus));
            deactivateTasks.add(() -> {
                cancellableTask.cancel(false);
                executorService.shutdownNow();
            });
        }
    }

    @Deactivate
    public void deactivate() {
        this.context = null;
        stopAppServer();
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

    @Override
    public void stopAppServer() {
        for (Runnable deactivateTask : deactivateTasks) {
            deactivateTask.run();
        }
        appServer = null;
        subscriberExecutionSpecs = Collections.emptyList();
        deactivateTasks.clear();
    }

    @Override
    public void startAsAppServer(String appServerName) {
        activateAs(appServerName);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
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
        return Optional.ofNullable(appServer);
    }

    @Override
    public List<SubscriberExecutionSpec> getSubscriberExecutionSpecs() {
        return ImmutableList.copyOf(subscriberExecutionSpecs);
    }

    @Override
    public List<AppServer> findAppServers() {
        return dataModel.mapper(AppServer.class).find();
    }

    @Override
    public Optional<AppServer> findAppServer(String name) {
        List<AppServer> appServers = dataModel.mapper(AppServer.class).select(where("name").isEqualToIgnoreCase(name));
        return appServers.isEmpty() ? Optional.<AppServer>empty() : Optional.of(appServers.get(0));
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

    @Override
    public Registration addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
        return () -> commandListeners.remove(commandListener);
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
                    break;
                case CONFIG_CHANGED:
                    appServer = (AppServerImpl) findAppServer(appServer.getName()).orElse(null);
                    subscriberExecutionSpecs = appServer == null ? Collections.emptyList() : appServer.getSubscriberExecutionSpecs();
                    if (appServer == null) {
                        stopAppServer();
                    }
                default:
            }
            commandListeners.forEach(listener -> listener.notify(command));
        }
    }

    public void stop() {
        Thread stoppingThread = new Thread(
                () -> {
                    try {
                        context.getBundle(0).stop();
                    } catch (BundleException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                });
        stoppingThread.start();
        Thread.currentThread().interrupt();
    }

    @Override
    public Map<AppServer, Optional<Path>> getAllImportDirectories() {
        return dataModel.mapper(ImportFolderForAppServer.class)
                .find()
                .stream()
                .collect(Collectors.toMap(ImportFolderForAppServer::getAppServer, ImportFolderForAppServer::getImportFolder));
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Query<AppServer> getAppServerQuery() {
        return queryService.wrap(dataModel.query(AppServer.class));
    }
}
