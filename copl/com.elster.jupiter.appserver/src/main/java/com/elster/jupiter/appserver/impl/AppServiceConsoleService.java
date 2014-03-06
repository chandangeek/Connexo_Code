package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;

@Component(name = "com.elster.jupiter.appserver.console", service = {AppServiceConsoleService.class}, property = {"name=" + "APS" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=create", "osgi.command.function=executeSubscription", "osgi.command.function=activateFileImport"}, immediate = true)
public class AppServiceConsoleService {

    private volatile AppService appService;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile ThreadPrincipalService threadPrincipalService;

    public void executeSubscription(final String subscriberName, final String destinationName, final int threads) {
        final Optional<AppServer> activated = appService.getAppServer();
        if (!activated.isPresent()) {
            System.out.println("Cannot execute subscriptions from anonymous app server.");
            return;
        }
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(destinationName, subscriberName);
                if (!subscriberSpec.isPresent()) {
                    System.out.println("Subscriber not found.");
                }
                activated.get().createSubscriberExecutionSpec(subscriberSpec.get(), threads);
            }
        });
    }

    public void activateFileImport(long id, String appServerName) {
        final AppServer appServerToActivateOn = getAppServerForActivation(appServerName);
        if (appServerToActivateOn == null) {
            System.out.println("AppServer not found.");
            return;
        }
        Optional<ImportSchedule> found = fileImportService.getImportSchedule(id);
        if (!found.isPresent()) {
            System.out.println("ImportSchedule not found.");
            return;
        }
        final ImportSchedule importSchedule = found.get();
        transactionService.execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                doActivateFileImport(appServerToActivateOn, importSchedule);
            }
        });
    }

    private void doActivateFileImport(AppServer appServerToActivateOn, ImportSchedule importSchedule) {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = ImportScheduleOnAppServerImpl.from(getDataModel(), fileImportService, importSchedule, appServerToActivateOn);
        importScheduleOnAppServer.save();
    }


    private AppServer getAppServerForActivation(String appServerName) {
        final Optional<AppServer> current = appService.getAppServer();
        AppServer appServerToActivateOn;
        if (current.isPresent() && current.get().getName().equals(appServerName)) {
            appServerToActivateOn = current.get();
        } else {
            Optional<AppServer> found = getDataModel().mapper(AppServer.class).getOptional(appServerName);
            appServerToActivateOn = found.orNull();
        }
        return appServerToActivateOn;
    }

    private DataModel getDataModel() {
        return ((AppServiceImpl) appService).getDataModel();
    }

    public void create(String name, String cronString) {
        threadPrincipalService.set(new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        });
        try (TransactionContext context = transactionService.getContext()) {
            appService.createAppServer(name, cronExpressionParser.parse(cronString));
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
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
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }
}
