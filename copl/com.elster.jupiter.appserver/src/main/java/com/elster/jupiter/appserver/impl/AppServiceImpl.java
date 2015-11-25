package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportFolderForAppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Registration;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.Functions;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.not;

@Component(name = "com.elster.jupiter.appserver", service = {InstallService.class, AppService.class, Subscriber.class, PrivilegesProvider.class, TopicHandler.class}, property = {"name=" + AppService.COMPONENT_NAME}, immediate = true)
public class AppServiceImpl implements InstallService, IAppService, Subscriber, PrivilegesProvider, TopicHandler {

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
    private volatile EventService eventService;
    private volatile ThreadPrincipalService threadPrincipalService;

    private volatile AppServerImpl appServer;
    private volatile List<? extends SubscriberExecutionSpec> subscriberExecutionSpecs = Collections.emptyList();
    private final List<Runnable> deactivateTasks = new ArrayList<>();
    private List<CommandListener> commandListeners = new CopyOnWriteArrayList<>();
    private final ThreadGroup threadGroup;
    private QueryService queryService;
    @GuardedBy("reconfigureLock")
    private Set<ImportSchedule> servedImportSchedules = new HashSet<>();
    private final Object reconfigureLock = new Object();

    public AppServiceImpl() {
        threadGroup = new ThreadGroup("AppServer message listeners");
    }

    @Inject
    AppServiceImpl(OrmService ormService, NlsService nlsService, TransactionService transactionService, MessageService messageService, CronExpressionParser cronExpressionParser, JsonService jsonService, FileImportService fileImportService, TaskService taskService, UserService userService, QueryService queryService, BundleContext bundleContext, ThreadPrincipalService threadPrincipalService) {
        this();
        setThreadPrincipalService(threadPrincipalService);
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
                    bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
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
        listenForMessagesToAllServers();
    }

    private void launchTaskService() {
        if (appServer.isRecurrentTaskActive()) {
            getTaskService().launch();
            deactivateTasks.add(() -> getTaskService().shutDown());
        }
    }

    private void launchFileImports() {
        Optional<ImportFolderForAppServer> appServerImportFolder = dataModel.mapper(ImportFolderForAppServer.class).getOptional(appServer.getName());
        if (!appServerImportFolder.isPresent()) {
            LOGGER.log(Level.WARNING, "AppServer with name " + appServer.getName() + " has no import folder configured.");
            return;
        }
        if (!appServerImportFolder.get().getImportFolder().isPresent()) {
            LOGGER.log(Level.WARNING, "AppServer with name " + appServer.getName() + " import folder is configured but cannot be resolved as path.");
            return;
        }
        appServerImportFolder.flatMap(ImportFolderForAppServer::getImportFolder)
                .ifPresent(fileImportService::setBasePath);

        synchronized (reconfigureLock) {
            importSchedulesOnCurrentAppServer()
                    .forEach(this::serveImportSchedule);
        }

        deactivateTasks.add(fileImportService::unscheduleAll);
    }

    private void unserveImportSchedule(ImportSchedule importSchedule) {
        fileImportService.unschedule(importSchedule);
        servedImportSchedules.remove(importSchedule);
    }

    private void serveImportSchedule(ImportSchedule importSchedule) {
        fileImportService.schedule(importSchedule);
        servedImportSchedules.add(importSchedule);
    }

    private void reconfigure() {
        Optional<ImportFolderForAppServer> appServerImportFolder = dataModel.mapper(ImportFolderForAppServer.class).getOptional(appServer.getName());
        if (appServerImportFolder.isPresent() && appServerImportFolder.get().getImportFolder().isPresent()) {
            appServerImportFolder.flatMap(ImportFolderForAppServer::getImportFolder)
                    .ifPresent(fileImportService::setBasePath);
        } else if (!appServerImportFolder.isPresent()) {
            LOGGER.log(Level.WARNING, "AppServer with name " + appServer.getName() + " has no import folder configured.");
        } else if (!appServerImportFolder.get().getImportFolder().isPresent()) {
            LOGGER.log(Level.WARNING, "AppServer with name " + appServer.getName() + " import folder is configured but cannot be resolved as path.");
        }

        Set<ImportSchedule> shouldServe = importSchedulesOnCurrentAppServer().collect(Collectors.toSet());
        synchronized (reconfigureLock) {
            servedImportSchedules.stream()
                    .filter(not(shouldServe::contains))
                    .collect(Collectors.toSet()) // we must collect, since unserve will modify the underlying collection
                    .forEach(this::unserveImportSchedule);
            shouldServe.stream()
                    .filter(not(servedImportSchedules::contains))
                    .forEach(this::serveImportSchedule);
        }
    }

    private Stream<ImportSchedule> importSchedulesOnCurrentAppServer() {
        if (!appServer.isActive()) {
            return Stream.empty();
        }
        return dataModel.mapper(ImportScheduleOnAppServer.class).find("appServer", appServer).stream()
                .map(ImportScheduleOnAppServer::getImportSchedule)
                .flatMap(Functions.asStream())
                .filter(ImportSchedule::isActive)
                .filter(not(ImportSchedule::isObsolete));
    }

    @Override
    public Class<?>[] getClasses() {
        return new Class<?>[]{InvalidateCacheRequest.class};
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    private void listenForMessagesToAllServers() {
        Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(ALL_SERVERS, messagingName());
        if (subscriberSpec.isPresent()) {
            final ExecutorService executorService = new CancellableTaskExecutorService(1, new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(thesaurus), this, () -> "All Server messages"));
            final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(subscriberSpec.get(), new CommandHandler(), transactionService, thesaurus));
            deactivateTasks.add(() -> {
                cancellableTask.cancel(false);
                executorService.shutdownNow();
            });
        }
    }

    private void listenForMessagesToAppServer() {
        messageService.getSubscriberSpec(messagingName(), messagingName())
                .ifPresent(this::doListenForMessagesToAppServer);
    }

    private void doListenForMessagesToAppServer(SubscriberSpec subscriberSpec) {
        final ExecutorService executorService = new CancellableTaskExecutorService(1, new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(thesaurus), this, () -> "This AppServer messages"));
        final Future<?> cancellableTask = executorService.submit(new MessageHandlerTask(subscriberSpec, new CommandHandler(), transactionService, thesaurus));
        deactivateTasks.add(() -> {
            cancellableTask.cancel(false);
            executorService.shutdownNow();
        });
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
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
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
    public List<SubscriberExecutionSpec> getSubscriberExecutionSpecsFor(SubscriberSpec subscriberSpec) {
        return dataModel.stream(SubscriberExecutionSpecImpl.class)
                .filter(where("subscriberSpecName").isEqualTo(subscriberSpec.getName()))
                .filter(where("destinationSpecName").isEqualTo(subscriberSpec.getDestination().getName()))
                .collect(Collectors.toList());
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

    @Override
    public Optional<AppServer> findAndLockAppServerByNameAndVersion(String name, long version) {
        return dataModel.mapper(AppServer.class).lockObjectIfVersion(version, name);
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

            Optional<DestinationSpec> allServerDestination = messageService.getDestinationSpec(ALL_SERVERS);
            if (allServerDestination.isPresent()) {
                allServerDestination.get().message(jsonService.serialize(command)).send();
            } else {
                LOGGER.log(Level.SEVERE, "Could not notify other servers of InvalidateCacheRequest. AllServers queue does not exist!");
            }
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

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    private class CommandHandler implements MessageHandler {

        @Override
        public void process(Message message) {
            threadPrincipalService.set(() -> "AppServer Command");
            try {
                doProcess(message);
            } finally {
                threadPrincipalService.clear();
            }
        }

        private void doProcess(Message message) {
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
                    AppServerImpl currentAppServer = appServer;
                    appServer = (AppServerImpl) findAppServer(appServer.getName()).orElse(null);
                    subscriberExecutionSpecs = appServer == null ? Collections.emptyList() : appServer.getSubscriberExecutionSpecs();
                    if (appServer == null) {
                        // AppServer has been deleted. We should stop and remove the AppServer's queues
                        stopAppServer();
                        deleteServerQueues(currentAppServer);
                    } else {
                        reconfigure();
                    }
                default:
            }
            commandListeners.forEach(listener -> listener.notify(command));
        }
    }

    private void deleteServerQueues(AppServerImpl deletedAppServer) {
        messageService.getSubscriberSpec(deletedAppServer.messagingName(), deletedAppServer.messagingName())
                .ifPresent(subscriberSpec -> {
                    subscriberSpec.getDestination().delete();
                });
        messageService.getSubscriberSpec(ALL_SERVERS, deletedAppServer.messagingName())
                .ifPresent(subscriberSpec -> {
                    subscriberSpec.getDestination().unSubscribe(subscriberSpec.getName());
                });
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

    @Override
    public List<AppServer> getImportScheduleAppServers(Long importScheduleId) {
        return dataModel.mapper(ImportScheduleOnAppServer.class)
                .find("importScheduleId", importScheduleId)
                .stream()
                .filter(i -> i.getImportSchedule().isPresent())
                .map(ImportScheduleOnAppServer::getAppServer)
                .collect(Collectors.toList());
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Query<AppServer> getAppServerQuery() {
        return queryService.wrap(dataModel.query(AppServer.class));
    }

    @Override
    public String getModuleName() {
        return AppService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_APPSERVER.getKey(), Privileges.RESOURCE_APPSERVER_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_APPSEVER, Privileges.Constants.VIEW_APPSEVER)));
        return resources;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ImportSchedule updatedSchedule = (ImportSchedule) localEvent.getSource();
        getImportScheduleAppServers(updatedSchedule.getId()).stream()
                .filter(AppServer::isActive)
                .findAny()
                .ifPresent(appServer -> {
                    Optional<DestinationSpec> allServerDestination = messageService.getDestinationSpec(ALL_SERVERS);
                    if (allServerDestination.isPresent()) {
                        allServerDestination.get().message(jsonService.serialize(new AppServerCommand(Command.CONFIG_CHANGED))).send();
                    } else {
                        LOGGER.log(Level.SEVERE, "Could not notify other servers of Config Change. AllServers queue does not exist!");
                    }
                });
    }

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/fileimport/importschedule/UPDATED";
    }
}
