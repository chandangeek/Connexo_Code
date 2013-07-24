package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.appserver.UnknownAppServerNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.base.Optional;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@Component(name = "com.elster.jupiter.appserver" , service = { InstallService.class, AppService.class }, property = { "name=" + Bus.COMPONENTNAME, "osgi.command.scope=jupiter", "osgi.command.function=create" }, immediate=true )
public class AppServiceImpl implements ServiceLocator, InstallService, AppService {

    private static final String APPSERVER_NAME = "com.elster.jupiter.appserver.name";

    private volatile OrmClient ormClient;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile LogService logService;

    private AppServer appServer;
    private List<SubscriberExecutionSpec> subscriberExecutionSpecs = Collections.emptyList();

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    public void activate(ComponentContext context) {
        try {
			Bus.setServiceLocator(this);
			String appServerName = (String) context.getBundleContext().getProperty(APPSERVER_NAME);
			if (appServerName != null) {
			    Optional<AppServer> foundAppServer = Bus.getOrmClient().getAppServerFactory().get(appServerName);
			    if (!foundAppServer.isPresent()) {
			        throw new UnknownAppServerNameException(appServerName);
			    }
			    appServer = foundAppServer.get();
			    subscriberExecutionSpecs = Bus.getOrmClient().getSubscriberExecutionSpecFactory().find("appServer", appServer);

			} else {
			    getLogService().log(Level.WARNING.intValue(), "AppServer started anonymously.");
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
            throw e;
		}
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
                return server;
            }
        });
    }

    @Override
    public void install() {
        Bus.getOrmClient().install();
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

    public void create(String name, String cronString) {
        createAppServer(name, getCronExpressionParser().parse(cronString));
    }
}
