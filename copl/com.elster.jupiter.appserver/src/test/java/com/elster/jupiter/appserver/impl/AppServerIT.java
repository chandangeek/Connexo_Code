/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.Expects;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fileimport.impl.FileImportServiceImpl;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppServerIT {

    public static final String NAME = "name";
    public static final String IMPORTER_NAME = "importer";
    public static final String DESTINATION = "destination";
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private FileImporterFactory fileImporterFactory;
    @Mock
    private Group group;
    @Mock
    private HttpService httpService;

    private WebServicesService webServicesService;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private TransactionService transactionService;
    private AppService appService;
    private MessageService messageService;
    private JsonService jsonService;
    private FileImportService fileImportService;
    private EndPointConfigurationService endPointConfigurationService;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(httpService);
        }
    }

    @Before
    public void setUp() throws SQLException {
        when(fileImporterFactory.getName()).thenReturn(IMPORTER_NAME);
        when(fileImporterFactory.getDestinationName()).thenReturn(DESTINATION);
        when(fileImporterFactory.getApplicationName()).thenReturn("AppName");

        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new WebServicesModule(),
                    new AppServiceModule(),
                    new FileImportModule(),
                    new TaskModule(),
                    new EventsModule(),
                    new DataVaultModule(),
                    new UserModule(),
                    new WebServicesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            endPointConfigurationService = injector.getInstance(EndPointConfigurationService.class);
            webServicesService = injector.getInstance(WebServicesService.class);
            appService = injector.getInstance(AppService.class);
            return null;
        });
        messageService = injector.getInstance(MessageService.class);
        jsonService = injector.getInstance(JsonService.class);
        fileImportService = injector.getInstance(FileImportService.class);
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCreateSimpleAppServer() {
        AppServer appServer = null;
        try (TransactionContext context = transactionService.getContext()) {
            appServer = appService.createAppServer(NAME, injector.getInstance(CronExpressionParser.class).parse("0 * * * * ? *").get());
            context.commit();
        }

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isFalse();

        Optional<AppServer> foundAppServer = appService.findAppServer(NAME);

        assertThat(foundAppServer).isPresent();
        appServer = foundAppServer.get();

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isFalse();

    }

    @Test
    public void testCreateSimpleActiveAppServer() {
        AppServer appServer = null;
        try (TransactionContext context = transactionService.getContext()) {
            appServer = appService.createAppServer(NAME, injector.getInstance(CronExpressionParser.class).parse("0 * * * * ? *").get());
            appServer.activate();
            context.commit();
        }

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isTrue();

        Optional<AppServer> foundAppServer = appService.findAppServer(NAME);

        assertThat(foundAppServer).isPresent();
        appServer = foundAppServer.get();

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isTrue();

        MessageService messageService = injector.getInstance(MessageService.class);
        DestinationSpec destinationSpec = messageService.getDestinationSpec(((AppServerImpl) appServer).messagingName()).get();
        Message message = destinationSpec.getSubscribers().get(0).receive();
        AppServerCommand command = injector.getInstance(JsonService.class).deserialize(message.getPayload(), AppServerCommand.class);
        assertThat(command.getCommand()).isEqualTo(Command.CONFIG_CHANGED);
    }

    @Test
    public void testCreateActiveAppServerWithSubscriberExecutionSpecs() {
        SubscriberSpec subscriber = null;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.createQueueTableSpec("QTS", "RAW", false);
            queueTableSpec.activate();
            DestinationSpec destination = queueTableSpec.createDestinationSpec(DESTINATION, 60);
            destination.activate();
            subscriber = destination.subscribe(new SimpleTranslationKey("subscriber", "Don't care about translation"), "TST", Layer.DOMAIN);
            context.commit();
        }

        AppServer appServer = null;
        try (TransactionContext context = transactionService.getContext()) {
            appServer = appService.createAppServer(NAME, injector.getInstance(CronExpressionParser.class).parse("0 * * * * ? *").get());
            try (AppServer.BatchUpdate batchUpdate = appServer.forBatchUpdate()) {
                batchUpdate.createActiveSubscriberExecutionSpec(subscriber, 2);
                batchUpdate.activate();
            }
            context.commit();
        }

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isTrue();
        assertThat(appServer.getSubscriberExecutionSpecs()).hasSize(1);
        SubscriberExecutionSpec subscriberExecutionSpec = appServer.getSubscriberExecutionSpecs().get(0);
        assertThat(subscriberExecutionSpec.getSubscriberSpec()).isEqualTo(subscriber);
        assertThat(subscriberExecutionSpec.getThreadCount()).isEqualTo(2);

        Optional<AppServer> foundAppServer = appService.findAppServer(NAME);

        assertThat(foundAppServer).isPresent();
        appServer = foundAppServer.get();

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isTrue();
        assertThat(appServer.getSubscriberExecutionSpecs()).hasSize(1);
        subscriberExecutionSpec = appServer.getSubscriberExecutionSpecs().get(0);
        assertThat(subscriberExecutionSpec.getSubscriberSpec()).isEqualTo(subscriber);
        assertThat(subscriberExecutionSpec.getThreadCount()).isEqualTo(2);

        DestinationSpec destinationSpec = messageService.getDestinationSpec(((AppServerImpl) appServer).messagingName()).get();
        SubscriberSpec subscriberSpec = destinationSpec.getSubscribers().get(0);
        Message message = subscriberSpec.receive();
        AppServerCommand command = jsonService.deserialize(message.getPayload(), AppServerCommand.class);
        assertThat(command.getCommand()).isEqualTo(Command.CONFIG_CHANGED);
        Expects.expect(subscriberSpec::receive)
                .toTimeOutAfter(50, TimeUnit.MILLISECONDS)
                .andCancelWith(subscriberSpec::cancel);
    }

    @Test
    public void testCreateActiveAppServerWithImportSchedule() {
        ((FileImportServiceImpl) fileImportService).addFileImporter(fileImporterFactory);

        SubscriberSpec subscriber = null;
        DestinationSpec destination = null;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.createQueueTableSpec("QTS", "RAW", false);
            queueTableSpec.activate();
            destination = queueTableSpec.createDestinationSpec(DESTINATION, 60);
            destination.activate();
            subscriber = destination.subscribe(new SimpleTranslationKey("subscriber", "Don't care about translation"), "TST", Layer.DOMAIN);
            context.commit();
        }

        ImportSchedule importSchedule = null;
        try (TransactionContext context = transactionService.getContext()) {
            importSchedule = fileImportService.newBuilder()
                    .setImporterName(IMPORTER_NAME)
                    .setDestination(DESTINATION)
                    .setImportDirectory(Paths.get("/test1"))
                    .setFailureDirectory(Paths.get("/test2"))
                    .setProcessingDirectory(Paths.get("/test3"))
                    .setSuccessDirectory(Paths.get("/test4"))
                    .setPathMatcher("*.*")
                    .setScheduleExpression(PeriodicalScheduleExpression.every(1).minutes().at(0).build())
                    .setName("importSchedule")
                    .create();
            context.commit();
        }

        AppServer appServer = null;
        try (TransactionContext context = transactionService.getContext()) {
            appServer = appService.createAppServer(NAME, injector.getInstance(CronExpressionParser.class).parse("0 * * * * ? *").get());
            try (AppServer.BatchUpdate batchUpdate = appServer.forBatchUpdate()) {
                batchUpdate.addImportScheduleOnAppServer(importSchedule);
                batchUpdate.activate();
            }
            context.commit();
        }

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isTrue();
        assertThat(appServer.getImportSchedulesOnAppServer()).hasSize(1);
        ImportScheduleOnAppServer importScheduleOnAppServer = appServer.getImportSchedulesOnAppServer().get(0);
        assertThat(importScheduleOnAppServer.getImportSchedule()).contains(importSchedule);

        Optional<AppServer> foundAppServer = appService.findAppServer(NAME);

        assertThat(foundAppServer).isPresent();
        appServer = foundAppServer.get();

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isTrue();
        assertThat(appServer.getImportSchedulesOnAppServer()).hasSize(1);
        importScheduleOnAppServer = appServer.getImportSchedulesOnAppServer().get(0);
        assertThat(importScheduleOnAppServer.getImportSchedule()).contains(importSchedule);

        DestinationSpec destinationSpec = messageService.getDestinationSpec(((AppServerImpl) appServer).messagingName()).get();
        SubscriberSpec subscriberSpec = destinationSpec.getSubscribers().get(0);
        Message message = subscriberSpec.receive();
        AppServerCommand command = jsonService.deserialize(message.getPayload(), AppServerCommand.class);
        assertThat(command.getCommand()).isEqualTo(Command.CONFIG_CHANGED);
        Expects.expect(subscriberSpec::receive)
                .toTimeOutAfter(50, TimeUnit.MILLISECONDS)
                .andCancelWith(subscriberSpec::cancel);
    }


    @Test
    public void testFullUpdate() {
        ((FileImportServiceImpl) fileImportService).addFileImporter(fileImporterFactory);

        SubscriberSpec subscriber1 = null;
        DestinationSpec destination1 = null;
        SubscriberSpec subscriber2 = null;
        DestinationSpec destination2 = null;
        SubscriberSpec subscriber3 = null;
        DestinationSpec destination3 = null;
        try (TransactionContext context = transactionService.getContext()) {
            QueueTableSpec queueTableSpec = messageService.createQueueTableSpec("QTS", "RAW", false);
            queueTableSpec.activate();
            destination1 = queueTableSpec.createDestinationSpec(DESTINATION + '1', 60);
            destination1.activate();
            subscriber1 = destination1.subscribe(new SimpleTranslationKey("subscriber1", "Subscriber One"), "TST", Layer.DOMAIN);
            destination2 = queueTableSpec.createDestinationSpec(DESTINATION + '2', 60);
            destination2.activate();
            subscriber2 = destination2.subscribe(new SimpleTranslationKey("subscriber2", "Subscriber Two"), "TST", Layer.DOMAIN);
            destination3 = queueTableSpec.createDestinationSpec(DESTINATION + '3', 60);
            destination3.activate();
            subscriber3 = destination3.subscribe(new SimpleTranslationKey("subscriber3", "Subscriber Three"), "TST", Layer.DOMAIN);
            context.commit();
        }

        ImportSchedule importSchedule1;
        ImportSchedule importSchedule2;
        ImportSchedule importSchedule3;
        try (TransactionContext context = transactionService.getContext()) {
            importSchedule1 = fileImportService.newBuilder()
                    .setImporterName(IMPORTER_NAME)
                    .setDestination(DESTINATION)
                    .setImportDirectory(Paths.get("/test1"))
                    .setFailureDirectory(Paths.get("/test2"))
                    .setProcessingDirectory(Paths.get("/test3"))
                    .setSuccessDirectory(Paths.get("/test4"))
                    .setPathMatcher("*.*")
                    .setScheduleExpression(PeriodicalScheduleExpression.every(1).minutes().at(0).build())
                    .setName("importSchedule1")
                    .create();
            importSchedule2 = fileImportService.newBuilder()
                    .setImporterName(IMPORTER_NAME)
                    .setDestination(DESTINATION)
                    .setImportDirectory(Paths.get("/test1"))
                    .setFailureDirectory(Paths.get("/test2"))
                    .setProcessingDirectory(Paths.get("/test3"))
                    .setSuccessDirectory(Paths.get("/test4"))
                    .setPathMatcher("*.*")
                    .setScheduleExpression(PeriodicalScheduleExpression.every(1).minutes().at(0).build())
                    .setName("importSchedule2")
                    .create();
            importSchedule3 = fileImportService.newBuilder()
                    .setImporterName(IMPORTER_NAME)
                    .setDestination(DESTINATION)
                    .setImportDirectory(Paths.get("/test1"))
                    .setFailureDirectory(Paths.get("/test2"))
                    .setProcessingDirectory(Paths.get("/test3"))
                    .setSuccessDirectory(Paths.get("/test4"))
                    .setPathMatcher("*.*")
                    .setScheduleExpression(PeriodicalScheduleExpression.every(1).minutes().at(0).build())
                    .setName("importSchedule3")
                    .create();
            context.commit();
        }

        AppServer appServer;
        try (TransactionContext context = transactionService.getContext()) {
            appServer = appService.createAppServer(NAME, injector.getInstance(CronExpressionParser.class).parse("0 * * * * ? *").get());
            try (AppServer.BatchUpdate batchUpdate = appServer.forBatchUpdate()) {
                batchUpdate.addImportScheduleOnAppServer(importSchedule1);
                batchUpdate.addImportScheduleOnAppServer(importSchedule2);
                batchUpdate.createActiveSubscriberExecutionSpec(subscriber1, 1);
                batchUpdate.createInactiveSubscriberExecutionSpec(subscriber2, 2);
                batchUpdate.activate();
            }
            context.commit();
        }

        Optional<AppServer> foundAppServer = appService.findAppServer(NAME);

        assertThat(foundAppServer).isPresent();
        appServer = foundAppServer.get();

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isTrue();
        assertThat(appServer.getImportSchedulesOnAppServer()).hasSize(2);
        ImportScheduleOnAppServer importScheduleOnAppServer1 = appServer.getImportSchedulesOnAppServer().get(0);
        assertThat(importScheduleOnAppServer1.getImportSchedule()).contains(importSchedule1);
        ImportScheduleOnAppServer importScheduleOnAppServer2 = appServer.getImportSchedulesOnAppServer().get(1);
        assertThat(importScheduleOnAppServer2.getImportSchedule()).contains(importSchedule2);
        assertThat(appServer.getSubscriberExecutionSpecs()).hasSize(2);
        SubscriberExecutionSpec subscriberExecutionSpec1 = appServer.getSubscriberExecutionSpecs().get(0);
        assertThat(subscriberExecutionSpec1.getSubscriberSpec()).isEqualTo(subscriber1);
        assertThat(subscriberExecutionSpec1.getThreadCount()).isEqualTo(1);
        assertThat(subscriberExecutionSpec1.isActive()).isTrue();
        SubscriberExecutionSpec subscriberExecutionSpec2 = appServer.getSubscriberExecutionSpecs().get(1);
        assertThat(subscriberExecutionSpec2.getSubscriberSpec()).isEqualTo(subscriber2);
        assertThat(subscriberExecutionSpec2.getThreadCount()).isEqualTo(2);
        assertThat(subscriberExecutionSpec2.isActive()).isFalse();

        DestinationSpec destinationSpec = messageService.getDestinationSpec(((AppServerImpl) appServer).messagingName()).get();
        SubscriberSpec subscriberSpec = destinationSpec.getSubscribers().get(0);
        Message message = subscriberSpec.receive();
        AppServerCommand command = jsonService.deserialize(message.getPayload(), AppServerCommand.class);
        assertThat(command.getCommand()).isEqualTo(Command.CONFIG_CHANGED);
        Expects.expect(subscriberSpec::receive)
                .toTimeOutAfter(50, TimeUnit.MILLISECONDS)
                .andCancelWith(subscriberSpec::cancel);

        // Ok then now for the update we'll remove the first subscriberExecutionSpec and the first importScheduleOnAppServer
        // we'll also change the second subscriberExecutionSpec's threadCount and the second importScheduleOnAppServer's
        // well also add a new subscriberExecutionSpec and a new importScheduleOnAppServer

        try (TransactionContext context = transactionService.getContext()) {

            appServer = appService.findAppServer(NAME).get();

            try (AppServer.BatchUpdate batchUpdate = appServer.forBatchUpdate()) {
                batchUpdate.setThreadCount(appServer.getSubscriberExecutionSpecs().get(1), 7);
                batchUpdate.activate(appServer.getSubscriberExecutionSpecs().get(1));
                batchUpdate.removeSubscriberExecutionSpec(appServer.getSubscriberExecutionSpecs().get(0));
                batchUpdate.createActiveSubscriberExecutionSpec(subscriber3, 3);
                batchUpdate.removeImportScheduleOnAppServer(appServer.getImportSchedulesOnAppServer().get(0));
                batchUpdate.addImportScheduleOnAppServer(importSchedule3);
                batchUpdate.deactivate();
            }

            context.commit();
        }

        appServer = appService.findAppServer(NAME).get();

        assertThat(appServer).isNotNull();
        assertThat(appServer.getName()).isEqualTo(NAME);
        assertThat(appServer.isActive()).isFalse();
        assertThat(appServer.getImportSchedulesOnAppServer()).hasSize(2);
        assertThat(appServer.getImportSchedulesOnAppServer().stream()
                        .map(ImportScheduleOnAppServer::getImportSchedule)
                        .flatMap(Functions.asStream())
                        .collect(Collectors.toSet())
        ).isEqualTo(ImmutableSet.of(importSchedule2, importSchedule3));
        List<? extends SubscriberExecutionSpec> subscriberExecutionSpecs = new ArrayList<>(appServer.getSubscriberExecutionSpecs());
        subscriberExecutionSpecs.sort(Comparator.comparing(SubscriberExecutionSpec::getSubscriberSpec, Comparator.comparing(SubscriberSpec::getName)));
        assertThat(subscriberExecutionSpecs).hasSize(2);
        subscriberExecutionSpec1 = subscriberExecutionSpecs.get(0);
        assertThat(subscriberExecutionSpec1.getSubscriberSpec()).isEqualTo(subscriber2);
        assertThat(subscriberExecutionSpec1.getThreadCount()).isEqualTo(7);
        assertThat(subscriberExecutionSpec1.isActive()).isTrue();
        subscriberExecutionSpec2 = subscriberExecutionSpecs.get(1);
        assertThat(subscriberExecutionSpec2.getSubscriberSpec()).isEqualTo(subscriber3);
        assertThat(subscriberExecutionSpec2.getThreadCount()).isEqualTo(3);
        assertThat(subscriberExecutionSpec2.isActive()).isTrue();

        destinationSpec = messageService.getDestinationSpec(((AppServerImpl) appServer).messagingName()).get();
        subscriberSpec = destinationSpec.getSubscribers().get(0);
        message = subscriberSpec.receive();
        command = jsonService.deserialize(message.getPayload(), AppServerCommand.class);
        assertThat(command.getCommand()).isEqualTo(Command.CONFIG_CHANGED);
        Expects.expect(subscriberSpec::receive)
                .toTimeOutAfter(50, TimeUnit.MILLISECONDS)
                .andCancelWith(subscriberSpec::cancel);

    }

}
