package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;

import java.security.Principal;

@Component(name = "com.elster.jupiter.appserver.console", service = {AppServiceConsoleService.class}, property = {"name=" + Bus.COMPONENTNAME + ".console", "osgi.command.scope=jupiter", "osgi.command.function=create", "osgi.command.function=executeSubscription", "osgi.command.function=activateFileImport"}, immediate = true)
public class AppServiceConsoleService {

    public void executeSubscription(final String subscriberName, final String destinationName, final int threads) {
        final Optional<AppServer> activated = Bus.getAppServer();
        if (!activated.isPresent()) {
            System.out.println("Cannot execute subscriptions from anonymous app server.");
            return;
        }
        Bus.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<SubscriberSpec> subscriberSpec = Bus.getMessageService().getSubscriberSpec(destinationName, subscriberName);
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
        Optional<ImportSchedule> found = Bus.getFileImportService().getImportSchedule(id);
        if (!found.isPresent()) {
            System.out.println("ImportSchedule not found.");
            return;
        }
        final ImportSchedule importSchedule = found.get();
        Bus.getTransactionService().execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                doActivateFileImport(appServerToActivateOn, importSchedule);
            }
        });
    }

    private void doActivateFileImport(AppServer appServerToActivateOn, ImportSchedule importSchedule) {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = new ImportScheduleOnAppServerImpl(importSchedule, appServerToActivateOn);
        importScheduleOnAppServer.save();
    }


    private AppServer getAppServerForActivation(String appServerName) {
        final Optional<AppServer> current = Bus.getAppServer();
        AppServer appServerToActivateOn;
        if (current.isPresent() && current.get().getName().equals(appServerName)) {
            appServerToActivateOn = current.get();
        } else {
            Optional<AppServer> found = Bus.getOrmClient().getAppServerFactory().get(appServerName);
            appServerToActivateOn = found.orNull();
        }
        return appServerToActivateOn;
    }

    public void create(String name, String cronString) {
        Bus.getThreadPrincipalService().set(new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        });
        try {
            Bus.getAppServerCreator().createAppServer(name, Bus.getCronExpressionParser().parse(cronString));
        } finally {
            Bus.getThreadPrincipalService().set(null);
        }
    }


}
