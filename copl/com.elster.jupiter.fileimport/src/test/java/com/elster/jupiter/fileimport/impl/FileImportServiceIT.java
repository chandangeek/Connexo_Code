/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileImportServiceIT {
    private static final long ID = 5564;
    private static final String FILE_NAME = "fileName";
    private static final String CONTENTS = "CONTENTS";
    private static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
    private static final String FAILURE_MESSAGE = "FAILURE_MESSAGE";
    private static final String SUCCESS_WITH_FAILURE_MESSAGE = "SUCCESS_WITH_FAILURE_MESSAGE";
    private static final String DESTINATION_NAME = "DESTINATION";
    private static final String IMPORTER_NAME = "TEST_IMPORTER";
    private ImportScheduleImpl importSchedule;
    private static final Instant NOW = Instant.ofEpochMilli(10L);
    private FileImportServiceImpl fileImportService;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private LicenseService licenseService;

    @Mock
    private DataMapper<FileImportOccurrence> fileImportFactory;

    private Path sourceFilePath;
    private Path inProcessFilePath;
    private Path successFilePath;
    private Path failureFilePath;
    private Path sourceDirectory, inProcessDirectory, successDirectory, failureDirectory, basePath;

    @Mock
    private FileUtilsImpl fileUtils;

    private FileNameCollisionResolver fileNameCollisionResolver;

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private User user;

    private JsonService jsonService;
    private TransactionService transactionService;
    private Clock clock;
    private MessageService messageService;
    private Injector injector;

    private Logger logger;
    private LogRecorder logRecorder;

    private FileSystem testFileSystem;

    private Predicate<Path> filter = path -> !Files.isDirectory(path);

    @Mock
    private NlsMessageFormat importStarted, importFinished;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    @Mock
    FileImporterFactory fileImporterFactory;
    @Mock
    ScheduleExpression scheduleExpression;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    private DestinationSpec destination;
    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private TimeService timeService;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TimeService.class).toInstance(timeService);
            bind(LicenseService.class).toInstance(licenseService);
            bind(FileImportService.class).to(FileImportServiceImpl.class).in(Scopes.SINGLETON);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws IOException {
        when(userService.createUser(any(), any())).thenReturn(user);

        testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        try {
            injector = Guice.createInjector(
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new H2OrmModule(),
                    new ThreadSecurityModule(),
                    new DomainUtilModule(),
                    new PubSubModule(),
                    new UtilModule(Clock.fixed(NOW, ZoneId.systemDefault()), testFileSystem),
                    new TransactionModule(),
                    new NlsModule(),
                    new UserModule(),
                    new BasicPropertiesModule(),
                    new MockModule(),
                    new DataVaultModule(),
                    new EventsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.run(() -> {
            fileImportService = injector.getInstance(FileImportServiceImpl.class);
            clock = injector.getInstance(Clock.class);
            messageService = injector.getInstance(MessageService.class);
            jsonService = injector.getInstance(JsonService.class);
        });

        transactionService.run(() -> {
            queueTableSpec = messageService.createQueueTableSpec(DESTINATION_NAME, "raw", null, true);
            destination = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 0);
            destination.activate();
            subscriberSpec = destination.subscribe(new SimpleTranslationKey(DESTINATION_NAME, DESTINATION_NAME), "TST", Layer.DOMAIN);
        });

        assertThat(messageService.getDestinationSpec(DESTINATION_NAME).get()).isEqualTo(destination);

        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logRecorder = new LogRecorder(Level.ALL);
        logger.addHandler(logRecorder);

        when(fileUtils.getInputStream(any(Path.class))).thenReturn(contentsAsStream());
        when(fileUtils.move(any(Path.class), any(Path.class))).thenCallRealMethod();
        when(fileUtils.exists(any(Path.class))).thenCallRealMethod();
        when(fileUtils.newDirectoryStream(any(Path.class), any(String.class))).thenCallRealMethod();

        fileNameCollisionResolver = new SimpleFileNameCollisionResolver(fileUtils, testFileSystem);
        Path root = testFileSystem.getRootDirectories().iterator().next();

        basePath = Files.createDirectory(root.resolve("baseImportPath"));

        inProcessDirectory = testFileSystem.getPath("process");
        failureDirectory = testFileSystem.getPath("failure");
        successDirectory = testFileSystem.getPath("success");
        sourceDirectory = testFileSystem.getPath("source");

        Files.createDirectory(basePath.resolve(inProcessDirectory));
        Files.createDirectory(basePath.resolve(failureDirectory));
        Files.createDirectory(basePath.resolve(successDirectory));
        Files.createDirectory(basePath.resolve(sourceDirectory));

        sourceFilePath = sourceDirectory.resolve(FILE_NAME);
        inProcessFilePath = inProcessDirectory.resolve(FILE_NAME);
        successFilePath = successDirectory.resolve(FILE_NAME);
        failureFilePath = failureDirectory.resolve(FILE_NAME);

        fileImportService.setBasePath(basePath);

        when(dataModel.mapper(FileImportOccurrence.class)).thenReturn(fileImportFactory);

        fileImportService.addFileImporter(fileImporterFactory);

        when(fileImporterFactory.getDestinationName()).thenReturn(DESTINATION_NAME);
        when(fileImporterFactory.getName()).thenReturn(IMPORTER_NAME);
        when(fileImporterFactory.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(fileImporterFactory.getApplicationName()).thenReturn("SYS");
        when(fileImporterFactory.requiresTransaction()).thenReturn(true);

        fileImportService.setAppServerInfoProvider(() -> Optional.of("appServerName"));

        transactionService.run(() -> {
            importSchedule = (ImportScheduleImpl) fileImportService.newBuilder()
                    .setName("IMPORT_SCHEDULE1")
                    .setDestination(DESTINATION_NAME)
                    .setPathMatcher("*")
                    .setImportDirectory(sourceDirectory)
                    .setFailureDirectory(failureDirectory)
                    .setSuccessDirectory(successDirectory)
                    .setProcessingDirectory(inProcessDirectory)
                    .setImporterName(IMPORTER_NAME)
                    .setScheduleExpression(scheduleExpression)
                    .create();
        });

        when(thesaurus.getFormat(MessageSeeds.FILE_IMPORT_STARTED)).thenReturn(importStarted);
        when(importStarted.format(anyVararg())).thenAnswer(invocation -> {
            return MessageFormat.format(MessageSeeds.FILE_IMPORT_STARTED.getDefaultFormat(), "");//invocation.getArguments()[0]);//, invocation.getArguments()[1], invocation.getArguments()[2], invocation.getArguments()[3]);
        });
        when(thesaurus.getFormat(MessageSeeds.FILE_IMPORT_FINISHED)).thenReturn(importFinished);
        when(importFinished.format(anyVararg())).thenAnswer(invocation -> {
            return MessageFormat.format(MessageSeeds.FILE_IMPORT_FINISHED.getDefaultFormat(), "");// invocation.getArguments()[0]);//, invocation.getArguments()[1]);
        });
    }

    private ByteArrayInputStream contentsAsStream() {
        return new ByteArrayInputStream(CONTENTS.getBytes());
    }

    @After
    public void tearDown() throws Exception {
        testFileSystem.close();
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testImportFileWithSuccess() throws IOException {
        Path file = basePath.resolve(sourceDirectory).resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        when(fileImporterFactory.createImporter(any())).thenReturn(fileImportOccurrence -> fileImportOccurrence.markSuccess(SUCCESS_MESSAGE));
        FolderScanningJob folderScanningJob = new FolderScanningJob(
                new PollingFolderScanner(filter, fileUtils, fileImportService.getBasePath().resolve(sourceDirectory), importSchedule.getPathMatcher(), this.thesaurus),
                new DefaultFileHandler(importSchedule, jsonService, transactionService, clock, fileImportService),
                fileImportService);

        folderScanningJob.run();

        Message received = subscriberSpec.receive();
        StreamImportMessageHandler importMessageHandler = (StreamImportMessageHandler) fileImportService.createMessageHandler();
        transactionService.run(() -> importMessageHandler.process(received));

        assertThat(Files.exists(fileImportService.getBasePath().resolve(sourceFilePath))).isFalse();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(successFilePath))).isTrue();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(failureFilePath))).isFalse();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(inProcessFilePath))).isFalse();
    }

    @Test
    public void testImportFileWithFailure() throws IOException {
        Path file = basePath.resolve(sourceDirectory).resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        when(fileImporterFactory.createImporter(any())).thenReturn(fileImportOccurrence -> fileImportOccurrence.markFailure(FAILURE_MESSAGE));
        FolderScanningJob folderScanningJob = new FolderScanningJob(
                new PollingFolderScanner(filter, fileUtils, fileImportService.getBasePath().resolve(sourceDirectory), importSchedule.getPathMatcher(), this.thesaurus),
                new DefaultFileHandler(importSchedule, jsonService, transactionService, clock, fileImportService),
                fileImportService);

        folderScanningJob.run();

        Message received = subscriberSpec.receive();
        StreamImportMessageHandler importMessageHandler = (StreamImportMessageHandler) fileImportService.createMessageHandler();
        transactionService.run(() -> importMessageHandler.process(received));

        assertThat(Files.exists(fileImportService.getBasePath().resolve(sourceFilePath))).isFalse();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(successFilePath))).isFalse();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(failureFilePath))).isTrue();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(inProcessFilePath))).isFalse();
    }

    @Test
    public void testImportFileSuccessWithFailures() throws IOException {
        Path file = basePath.resolve(sourceDirectory).resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        when(fileImporterFactory.createImporter(any())).thenReturn(fileImportOccurrence -> fileImportOccurrence.markSuccessWithFailures(SUCCESS_WITH_FAILURE_MESSAGE));
        FolderScanningJob folderScanningJob = new FolderScanningJob(
                new PollingFolderScanner(filter, fileUtils, fileImportService.getBasePath().resolve(sourceDirectory), importSchedule.getPathMatcher(), this.thesaurus),
                new DefaultFileHandler(importSchedule, jsonService, transactionService, clock, fileImportService),
                fileImportService);

        folderScanningJob.run();

        Message received = subscriberSpec.receive();
        StreamImportMessageHandler importMessageHandler = (StreamImportMessageHandler) fileImportService.createMessageHandler();
        transactionService.run(() -> importMessageHandler.process(received));

        assertThat(Files.exists(fileImportService.getBasePath().resolve(sourceFilePath))).isFalse();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(successFilePath))).isTrue();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(failureFilePath))).isFalse();
        assertThat(Files.exists(fileImportService.getBasePath().resolve(inProcessFilePath))).isFalse();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_THE_SAME_AS_IMPORT_FOLDER + "}", property = "importDirectory")
    public void createWithTheSameImportAndProcessFoldersTest() {
        transactionService.run(() -> fileImportService.newBuilder()
                .setName("IMPORT_SCHEDULE_1")
                .setDestination(DESTINATION_NAME)
                .setPathMatcher("*")
                .setImportDirectory(testFileSystem.getPath("source"))
                .setFailureDirectory(testFileSystem.getPath("failure"))
                .setSuccessDirectory(testFileSystem.getPath("success"))
                .setProcessingDirectory(testFileSystem.getPath("source"))
                .setImporterName(IMPORTER_NAME)
                .setScheduleExpression(scheduleExpression)
                .create());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_THE_SAME_AS_IMPORT_FOLDER + "}", property = "importDirectory")
    public void createWithTheSameImportAndSuccessFoldersTest() {
        transactionService.run(() -> fileImportService.newBuilder()
                .setName("IMPORT_SCHEDULE_2")
                .setDestination(DESTINATION_NAME)
                .setPathMatcher("*")
                .setImportDirectory(testFileSystem.getPath("source"))
                .setFailureDirectory(testFileSystem.getPath("failure"))
                .setSuccessDirectory(testFileSystem.getPath("source"))
                .setProcessingDirectory(testFileSystem.getPath("process"))
                .setImporterName(IMPORTER_NAME)
                .setScheduleExpression(scheduleExpression)
                .create());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_THE_SAME_AS_IMPORT_FOLDER + "}", property = "importDirectory")
    public void createWithTheSameImportAndFailureFoldersTest() {
        transactionService.run(() -> fileImportService.newBuilder()
                .setName("IMPORT_SCHEDULE_3")
                .setDestination(DESTINATION_NAME)
                .setPathMatcher("*")
                .setImportDirectory(testFileSystem.getPath("source"))
                .setFailureDirectory(testFileSystem.getPath("source"))
                .setSuccessDirectory(testFileSystem.getPath("success"))
                .setProcessingDirectory(testFileSystem.getPath("process"))
                .setImporterName(IMPORTER_NAME)
                .setScheduleExpression(scheduleExpression)
                .create());
    }
}
