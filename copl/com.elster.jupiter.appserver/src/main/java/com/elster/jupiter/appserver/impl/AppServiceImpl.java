package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.base.Optional;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@Component(name = "com.elster.jupiter.appserver", service = {InstallService.class, AppService.class}, property = {"name=" + Bus.COMPONENTNAME, "osgi.command.scope=jupiter", "osgi.command.function=create", "osgi.command.function=executeSubscription"}, immediate = true)
public class AppServiceImpl implements ServiceLocator, InstallService, AppService {

    private static final String APPSERVER_NAME = "com.elster.jupiter.appserver.name";
    private static final String APP_SERVER = "AppServer";
    private static final String ALL_SERVERS = "AllServers";

    private volatile OrmClient ormClient;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile LogService logService;
    private volatile ThreadPrincipalService threadPrincipalService;

    private AppServer appServer;
    private List<SubscriberExecutionSpec> subscriberExecutionSpecs = Collections.emptyList();

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    public void activate(ComponentContext context) {
        try {
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
        getLogService().log(Level.WARNING.intValue(), "AppServer started anonymously.");
    }

    private void activateAs(String appServerName) {
        Optional<AppServer> foundAppServer = Bus.getOrmClient().getAppServerFactory().get(appServerName);
        if (!foundAppServer.isPresent()) {
            getLogService().log(Level.SEVERE.intValue(), "AppServer with name " + appServerName + " not found.");
            activateAnonymously();
            return;
        }
        appServer = foundAppServer.get();
        subscriberExecutionSpecs = appServer.getSubscriberExecutionSpecs();
    }

    public void deactivate(ComponentContext context) {
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
                DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(messagingName(name), 60);
                destinationSpec.subscribe(messagingName(name), 1);
                Optional<DestinationSpec> allServersTopic = getMessageService().getDestinationSpec(ALL_SERVERS);
                if (allServersTopic.isPresent()) {
                    allServersTopic.get().subscribe(messagingName(name), 1);
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
    
    private String messagingName(String name) {
        return AppServiceImpl.APP_SERVER + '_' + name;
    }

    @Override
    public void install() {
        try {
            getOrmClient().install();
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

    @Override
    public AppServer getAppServer() {
        return appServer;
    }

    @Override
    public List<SubscriberExecutionSpec> getSubscriberExecutionSpecs() {
        return subscriberExecutionSpecs;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
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
}
