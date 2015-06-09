package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.MessageSeeds;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileImportOccurrenceImplTest {

    private static final long ID = 5564;
    private static final String FILE_NAME = "fileName";
    private static final String CONTENTS = "CONTENTS";
    private static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
    private static final String FAILURE_MESSAGE = "FAILURE_MESSAGE";
    private static final String SUCCESS_WITH_FAILURE_MESSAGE = "SUCCESS_WITH_FAILURE_MESSAGE";
    @Mock
    private DataMapper<FileImportOccurrence> fileImportFactory;

    private Path sourceFilePath;
    private Path inProcessFilePath;
    private Path successFilePath;
    private Path failureFilePath;
    private Path sourceDirectory, inProcessDirectory, successDirectory, failureDirectory, basePath;
    @Mock
    private ImportSchedule importSchedule;

    @Mock
    private DefaultFileSystem fileSystem;

    private FileNameCollisionResolver fileNameCollisionResolver;

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;

    @Mock
    private FileImportService fileImportService;


    @Mock
    private Clock clock;




    private Logger logger;
    private LogRecorder logRecorder;

    private java.nio.file.FileSystem testFileSystem;


    @Mock
    private NlsMessageFormat importStarted, importFinished;


    @Before
    public void setUp() throws IOException {

        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logRecorder = new LogRecorder(Level.ALL);
        logger.addHandler(logRecorder);

        testFileSystem = Jimfs.newFileSystem(Configuration.windows());

        when(fileSystem.getInputStream(any(Path.class))).thenReturn(contentsAsStream());
        when(fileSystem.move(any(Path.class), any(Path.class))).thenCallRealMethod();
        when(fileSystem.exists(any(Path.class))).thenCallRealMethod();
        when(fileSystem.newDirectoryStream(any(Path.class), any(String.class))).thenCallRealMethod();

        fileNameCollisionResolver = new SimpleFileNameCollisionResolver(fileSystem);
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

        Files.createFile(basePath.resolve(sourceDirectory).resolve(FILE_NAME));

        sourceFilePath = sourceDirectory.resolve(FILE_NAME);
        inProcessFilePath = inProcessDirectory.resolve(FILE_NAME);
        successFilePath = successDirectory.resolve(FILE_NAME);
        failureFilePath = failureDirectory.resolve(FILE_NAME);

        when(clock.instant()).thenReturn(Instant.now());
        when(fileImportService.getBasePath()).thenReturn(basePath);
        when(dataModel.mapper(FileImportOccurrence.class)).thenReturn(fileImportFactory);
        when(importSchedule.getId()).thenReturn(ID);
        when(importSchedule.getInProcessDirectory()).thenReturn(inProcessDirectory);
        when(importSchedule.getSuccessDirectory()).thenReturn(successDirectory);
        when(importSchedule.getFailureDirectory()).thenReturn(failureDirectory);

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
    public void tearDown() {
    }

    @Test
    public void testCreateKeepsReferenceToImportSchedule() {
        FileImportOccurrenceImpl fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        fileImportOccurrence.setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        assertThat(fileImportOccurrence.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test
    public void testCreateKeepsReferenceToFile() {

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        assertThat(fileImportOccurrence.getFileName()).isEqualTo(FILE_NAME);
    }

    @Test
    public void testGetContents() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);

        InputStream contents = fileImportOccurrence.getContents();

        assertThat(contents).hasContentEqualTo(contentsAsStream());
    }

    @Test
    public void testMovedToProcessing() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        assertThat(!Files.exists(sourceFilePath));
        assertThat(Files.exists(inProcessFilePath));
        assertThat(!Files.exists(successFilePath));
        assertThat(!Files.exists(failureFilePath));
    }

    @Test
    public void testMarkSuccessMovedToSuccessFolder() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        fileImportOccurrence.markSuccess(SUCCESS_MESSAGE);

        assertThat(!Files.exists(sourceFilePath));
        assertThat(!Files.exists(inProcessFilePath));
        assertThat(Files.exists(successFilePath));
        assertThat(!Files.exists(failureFilePath));
    }

    @Test
    public void testMarkSuccessClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(Path.class))).thenReturn(spiedStream);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();
        fileImportOccurrence.getContents();

        fileImportOccurrence.markSuccess(SUCCESS_MESSAGE);

        verify(spiedStream).close();
    }

    @Test
    public void testMarkFailureMovedToFailureFolder() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();


        fileImportOccurrence.markFailure(FAILURE_MESSAGE);

        assertThat(!Files.exists(sourceFilePath));
        assertThat(!Files.exists(inProcessFilePath));
        assertThat(!Files.exists(successFilePath));
        assertThat(!Files.exists(failureFilePath));
    }

    @Test
    public void testMarkFailureClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(Path.class))).thenReturn(spiedStream);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();
        fileImportOccurrence.getContents();


        fileImportOccurrence.markFailure(FAILURE_MESSAGE);

        verify(spiedStream).close();
    }

    @Test
    public void testMarkSuccessWithFailureMovedToSuccessFolder() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        fileImportOccurrence.markSuccessWithFailures(SUCCESS_WITH_FAILURE_MESSAGE);

        assertThat(Files.exists(sourceFilePath));
        assertThat(!Files.exists(inProcessFilePath));
        assertThat(!Files.exists(successFilePath));
        assertThat(!Files.exists(failureFilePath));
    }

    @Test
    public void testMarkSuccessWithFailureMovedClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(Path.class))).thenReturn(spiedStream);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, sourceFilePath);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();
        fileImportOccurrence.getContents();


        fileImportOccurrence.markSuccessWithFailures(SUCCESS_WITH_FAILURE_MESSAGE);

        verify(spiedStream).close();
    }


}
