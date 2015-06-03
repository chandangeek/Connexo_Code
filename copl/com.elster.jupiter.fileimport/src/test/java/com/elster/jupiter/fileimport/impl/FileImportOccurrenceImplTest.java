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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final Path BASE_PATH = Paths.get("/");
    @Mock
    private DataMapper<FileImportOccurrence> fileImportFactory;
    @Mock
    private File file, newFile, successFile, failureFile;
    @Mock
    private Path inProcessDirectory, successDirectory, failureDirectory, basePath;
    @Mock
    private Path path, newPath, inProcessPath, successPath, failurePath;
    @Mock
    private ImportSchedule importSchedule;
    @Mock
    private FileSystem fileSystem;
    @Mock
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

    @Mock
    private NlsMessageFormat importStarted, importFinished;


    @Before
    public void setUp() {

        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logRecorder = new LogRecorder(Level.ALL);
        logger.addHandler(logRecorder);


        when(clock.instant()).thenReturn(Instant.now());
        when(fileImportService.getBasePath()).thenReturn(basePath);

        when(basePath.resolve(inProcessPath)).thenReturn(inProcessPath);
        when(basePath.resolve(successPath)).thenReturn(successPath);
        when(basePath.resolve(failurePath)).thenReturn(failurePath);

        when(basePath.resolve(inProcessDirectory)).thenReturn(inProcessPath);
        when(basePath.resolve(successDirectory)).thenReturn(successPath);
        when(basePath.resolve(failureDirectory)).thenReturn(failurePath);


        when(basePath.resolve(path)).thenReturn(path);


        when(path.toFile()).thenReturn(file);

        when(dataModel.mapper(FileImportOccurrence.class)).thenReturn(fileImportFactory);
        when(importSchedule.getId()).thenReturn(ID);
        when(importSchedule.getInProcessDirectory()).thenReturn(inProcessDirectory);
        when(importSchedule.getSuccessDirectory()).thenReturn(successDirectory);
        when(importSchedule.getFailureDirectory()).thenReturn(failureDirectory);

        when(inProcessPath.resolve(path)).thenReturn(inProcessPath);
        when(failurePath.resolve(path)).thenReturn(failurePath);
        when(successPath.resolve(path)).thenReturn(successPath);

        when(successPath.resolve(newPath)).thenReturn(successPath);
        when(failurePath.resolve(newPath)).thenReturn(failurePath);
        when(newPath.resolve(newPath)).thenReturn(newPath);

        when(path.getFileName()).thenReturn(path);

        when(file.toPath()).thenReturn(path);
        when(file.getPath()).thenReturn("./test.xml");
        when(file.exists()).thenReturn(true);
        when(newFile.exists()).thenReturn(true);
        when(fileSystem.getInputStream(any(File.class))).thenReturn(contentsAsStream());

        when(fileNameCollisionResolver.resolve(inProcessPath)).thenReturn(inProcessPath);

        when(newPath.toFile()).thenReturn(newFile);
        when(newFile.toPath()).thenReturn(newPath);
        when(newPath.getFileName()).thenReturn(newPath);

        when(fileNameCollisionResolver.resolve(successPath)).thenReturn(successPath);

        when(successPath.toFile()).thenReturn(successFile);
        when(successFile.toPath()).thenReturn(successPath);
        when(successPath.getFileName()).thenReturn(successPath);
        when(successPath.resolve(newPath)).thenReturn(newPath);

        when(fileNameCollisionResolver.resolve(failurePath)).thenReturn(failurePath);

        when(failurePath.toFile()).thenReturn(failureFile);
        when(failureFile.toPath()).thenReturn(failurePath);
        when(failurePath.getFileName()).thenReturn(failurePath);
        when(failurePath.resolve(newPath)).thenReturn(failurePath);



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
        FileImportOccurrenceImpl fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        assertThat(fileImportOccurrence.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test
    public void testCreateKeepsReferenceToFile() {
        when(path.getFileName()).thenReturn(path);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        assertThat(fileImportOccurrence.getFileName()).isEqualTo("path");
    }

    @Test
    public void testGetContents() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);

        InputStream contents = fileImportOccurrence.getContents();

        assertThat(contents).hasContentEqualTo(contentsAsStream());
    }

    @Test
    public void testMovedToProcessing() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        verify(fileSystem).move(path, inProcessPath);
    }

    @Test
    public void testMarkSuccessMovedToSuccessFolder() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markSuccess(SUCCESS_MESSAGE);

        verify(fileSystem).move(path, successPath);
    }

    @Test
    public void testMarkSuccessClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(File.class))).thenReturn(spiedStream);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();
        fileImportOccurrence.getContents();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markSuccess(SUCCESS_MESSAGE);

        verify(spiedStream).close();
    }

    @Test
    public void testMarkFailureMovedToFailureFolder() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markFailure(FAILURE_MESSAGE);

        verify(fileSystem).move(path, failurePath);
    }

    @Test
    public void testMarkFailureClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(File.class))).thenReturn(spiedStream);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock, importSchedule, file.toPath());
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();
        fileImportOccurrence.getContents();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markFailure(FAILURE_MESSAGE);

        verify(spiedStream).close();
    }


}
