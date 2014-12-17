package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

@Component(name = "com.elster.jupiter.appserver.console", service = {AppServiceConsoleService.class},
        property = {"name=" + "APS" + ".console",
                "osgi.command.scope=appserver",
                "osgi.command.function=create",
                "osgi.command.function=serve",
                "osgi.command.function=stopServing",
                "osgi.command.function=activate",
                "osgi.command.function=deactivate",
                "osgi.command.function=activateFileImport",
                "osgi.command.function=appServers",
                "osgi.command.function=identify",
                "osgi.command.function=stopAppServer",
                "osgi.command.function=become",
                "osgi.command.function=setLocale",
                "osgi.command.function=setTimeZone",
                "osgi.command.function=getLocale",
                "osgi.command.function=getLocales",
                "osgi.command.function=getTimeZone",
                "osgi.command.function=getTimeZones",
                "osgi.command.function=report"}, immediate = true)
public class AppServiceConsoleService {

    private volatile IAppService appService;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MessageHandlerLauncherService messageHandlerLauncherService;

    public void serve(final String appServerName, final String subscriberName, final String destinationName, final int threads) {
        final Optional<AppServer> found = findAppServer(appServerName);
        if (!found.isPresent()) {
            System.out.println("AppServer not found.");
            return;
        }
        final AppServer appServer = found.get();
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(destinationName, subscriberName);
                if (!subscriberSpec.isPresent()) {
                    System.out.println("Subscriber not found.");
                }
                SubscriberExecutionSpec subscriberExecutionSpec = appServer.getSubscriberExecutionSpecs().stream()
                        .filter(subscriber -> subscriber.getSubscriberSpec().getDestination().getName().equals(destinationName))
                        .filter(subscriber -> subscriber.getSubscriberSpec().getName().equals(subscriberName))
                        .findFirst()
                        .map(SubscriberExecutionSpec.class::cast)
                        .orElseGet(() -> appServer.createSubscriberExecutionSpec(subscriberSpec.get(), threads));
                if (subscriberExecutionSpec.getThreadCount() != threads) {
                    appServer.setThreadCount(subscriberExecutionSpec, threads);
                }
            }
        });
    }

    private Optional<AppServer> findAppServer(String appServerName) {
        Optional<AppServer> current = appService.getAppServer();
        if (current.isPresent() && current.get().getName().equals(appServerName)) {
            return current;
        }
        return appService.findAppServer(appServerName);
    }

    public void stopServing(final String appServerName, final String subscriberName, final String destinationName) {
        final Optional<AppServer> found = findAppServer(appServerName);
        if (!found.isPresent()) {
            System.out.println("AppServer not found.");
            return;
        }
        final AppServer activated = found.get();
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(destinationName, subscriberName);
                if (!subscriberSpec.isPresent()) {
                    System.out.println("Subscriber not found.");
                }
                SubscriberExecutionSpec subscriberExecutionSpec = activated.getSubscriberExecutionSpecs().stream()
                        .filter(subscriber -> subscriber.getSubscriberSpec().getDestination().getName().equals(destinationName))
                        .filter(subscriber -> subscriber.getSubscriberSpec().getName().equals(subscriberName))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
                activated.removeSubscriberExecutionSpec(subscriberExecutionSpec);
            }
        });
    }

    public void activateFileImport(long id, String appServerName) {
        Optional<AppServer> foundAppServer = findAppServer(appServerName);
        if (!foundAppServer.isPresent()) {
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
                doActivateFileImport(foundAppServer.get(), importSchedule);
            }
        });
    }

    private void doActivateFileImport(AppServer appServerToActivateOn, ImportSchedule importSchedule) {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = ImportScheduleOnAppServerImpl.from(getDataModel(), fileImportService, importSchedule, appServerToActivateOn);
        importScheduleOnAppServer.save();
    }


//    private Optional<AppServer> getAppServerForActivation(String appServerName) {
//        final Optional<AppServer> current = appService.getAppServer();
//        AppServer appServerToActivateOn;
//        if (current.isPresent() && current.get().getName().equals(appServerName)) {
//            appServerToActivateOn = current.get();
//        } else {
//            Optional<AppServer> found = getDataModel().mapper(AppServer.class).getOptional(appServerName);
//            appServerToActivateOn = found.orElse(null);
//        }
//        return appServerToActivateOn;
//    }

    private DataModel getDataModel() {
        return appService.getDataModel();
    }

    public void create(String name, String cronString) {
        create(name, cronString, true);
    }

    public void create(String name, String cronString, boolean active) {
        threadPrincipalService.set(new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        });
        try (TransactionContext context = transactionService.getContext()) {
            AppServer appServer = appService.createAppServer(name, cronExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new));
            appServer.setRecurrentTaskActive(active);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void appServers() {
        appService.findAppServers().stream()
                .peek(server -> System.out.println(server.getName() + " active:" + server.isActive() + " tasks:" + server.isRecurrentTaskActive()))
                .flatMap(server -> server.getSubscriberExecutionSpecs().stream())
                .map(se -> "\t" + se.getSubscriberSpec().getDestination().getName() + " " + se.getSubscriberSpec().getName() + " " + se.getThreadCount())
                .forEach(System.out::println);
    }

    public void identify() {
        appService.getAppServer()
                .map(Arrays::asList).orElse(Collections.<AppServer>emptyList()).stream()
                .peek(server -> System.out.println(server.getName() + " active:" + server.isActive() + " tasks:" + server.isRecurrentTaskActive()))
                .flatMap(server -> server.getSubscriberExecutionSpecs().stream())
                .map(se -> "\t" + se.getSubscriberSpec().getDestination().getName() + " " + se.getSubscriberSpec().getName() + " " + se.getThreadCount())
                .forEach(System.out::println);
    }

    public void stopAppServer() {
        appService.stopAppServer();
        messageHandlerLauncherService.appServerStopped();
    }

    public void become(String appServerName) {
        appService.stopAppServer();
        appService.startAsAppServer(appServerName);
        messageHandlerLauncherService.appServerStarted();
    }

    public void activate(String appServerName) {
        AppServer appServer = findAppServer(appServerName).orElseThrow(IllegalArgumentException::new);
        try(TransactionContext context = transactionService.getContext()) {
            appServer.activate();
            context.commit();
        }
    }

    public void deactivate(String appServerName) {
        AppServer appServer = findAppServer(appServerName).orElseThrow(IllegalArgumentException::new);
        try(TransactionContext context = transactionService.getContext()) {
            appServer.deactivate();
            context.commit();
        }
    }

    public void setLocale(String language) {
        Locale.setDefault(new Locale(language));
        System.out.println(Locale.getDefault());
    }

    public void setLocale(String language, String country) {
        Locale.setDefault(new Locale(language, country));
        System.out.println(Locale.getDefault());
    }

    public void setLocale(String language, String country, String variant) {
        Locale.setDefault(new Locale(language, country, variant));
        System.out.println(Locale.getDefault());
    }

    public void setTimeZone(String zone) {
        ZoneId zoneId = ZoneId.of(zone);
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        System.out.println("Time zone is set to " + ZoneId.systemDefault());
        System.out.println("Current time is " + ZonedDateTime.now());
    }

    public void getLocale() {
        System.out.println(Locale.getDefault());
    }

    public void getLocales() {
        Arrays.stream(Locale.getAvailableLocales())
                .map(Locale::toString)
                .sorted()
                .forEach(System.out::println);
    }

    public void getTimeZone() {
        System.out.println("Time zone is set to " + ZoneId.systemDefault());
        System.out.println("Current time is " + ZonedDateTime.now());
    }

    public void getTimeZones() {
        ZoneId.getAvailableZoneIds().stream()
                .map(Object::toString)
                .sorted()
                .forEach(System.out::println);
    }

    public void report() {
        System.out.println("Setup : ");
        System.out.println("==========");
        appService.getAppServer()
                .map(Arrays::asList).orElse(Collections.<AppServer>emptyList()).stream()
                .flatMap(server -> server.getSubscriberExecutionSpecs().stream())
                .map(se -> "\t" + se.getSubscriberSpec().getDestination().getName() + ' ' + se.getSubscriberSpec().getName() + ' ' + se.getThreadCount())
                .forEach(System.out::println);
        System.out.println("Tasks : ");
        System.out.println("==========");
        messageHandlerLauncherService.futureReport().entrySet().stream()
                .map(entry -> "\t" + entry.getKey().getDestination() + ' ' + entry.getKey().getSubscriber() + ' ' + String.valueOf(entry.getValue()))
                .forEach(System.out::println);
        System.out.println("Threads : ");
        System.out.println("==========");
        messageHandlerLauncherService.threadReport().entrySet().stream()
                .map(entry -> "\t" + entry.getKey().getDestination() + ' ' + entry.getKey().getSubscriber() + ' ' + String.valueOf(entry.getValue()))
                .forEach(System.out::println);
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = (IAppService) appService;
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

    @Reference
    public void setMessageHandlerLauncherService(MessageHandlerLauncherService messageHandlerLauncherService) {
        this.messageHandlerLauncherService = messageHandlerLauncherService;
    }
}
