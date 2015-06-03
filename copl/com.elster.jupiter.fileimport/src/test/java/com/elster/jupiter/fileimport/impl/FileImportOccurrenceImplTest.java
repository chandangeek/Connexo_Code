package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.fileimport.FileImportOccurrence;
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
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileImportOccurrenceImplTest {

    private static final long ID = 5564;
    private static final String FILE_NAME = "fileName";
    private static final String CONTENTS = "CONTENTS";
    @Mock
    private DataMapper<FileImportOccurrence> fileImportFactory;
    @Mock
    private File file, newFile, successFile, failureFile, inProcessDirectory, successDirectory, failureDirectory;
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


//        when(serviceLocator.getFileNameCollisionResollver()).thenReturn(fileNameCollisionResolver);
        when(file.toPath()).thenReturn(path);
        when(dataModel.mapper(FileImportOccurrence.class)).thenReturn(fileImportFactory);
        when(importSchedule.getId()).thenReturn(ID);
        when(importSchedule.getInProcessDirectory()).thenReturn(inProcessDirectory);
        when(importSchedule.getSuccessDirectory()).thenReturn(successDirectory);
        when(importSchedule.getFailureDirectory()).thenReturn(failureDirectory);
        when(inProcessDirectory.toPath()).thenReturn(inProcessPath);
        when(inProcessPath.resolve(path)).thenReturn(inProcessPath);
        when(successPath.resolve(newPath)).thenReturn(successPath);
        when(successDirectory.toPath()).thenReturn(successPath);
        when(failurePath.resolve(newPath)).thenReturn(failurePath);
        when(failureDirectory.toPath()).thenReturn(failurePath);
        when(path.getFileName()).thenReturn(path);
        when(file.getPath()).thenReturn("./test.xml");
        when(file.exists()).thenReturn(true);
        when(newFile.exists()).thenReturn(true);
//        when(serviceLocator.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.getInputStream(any(File.class))).thenReturn(contentsAsStream());
        when(fileNameCollisionResolver.resolve(inProcessPath)).thenReturn(newPath);
        when(newPath.toFile()).thenReturn(newFile);
        when(newFile.toPath()).thenReturn(newPath);
        when(newPath.getFileName()).thenReturn(newPath);
        when(fileNameCollisionResolver.resolve(successPath)).thenReturn(successPath);
        when(successPath.toFile()).thenReturn(successFile);
        when(successFile.toPath()).thenReturn(successPath);
        when(successPath.getFileName()).thenReturn(successPath);
        when(fileNameCollisionResolver.resolve(failurePath)).thenReturn(failurePath);
        when(failurePath.toFile()).thenReturn(failureFile);
        when(failureFile.toPath()).thenReturn(failurePath);
        when(failurePath.getFileName()).thenReturn(failurePath);


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
        FileImportOccurrenceImpl fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        assertThat(fileImportOccurrence.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test
    public void testCreateKeepsReferenceToFile() {
        when(path.getFileName()).thenReturn(path);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        assertThat(fileImportOccurrence.getFileName()).isEqualTo("newPath");
    }

    @Test
    public void testGetContents() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);

        InputStream contents = fileImportOccurrence.getContents();

        assertThat(contents).hasContentEqualTo(contentsAsStream());
    }

    @Test
    public void testMovedToProcessing() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        verify(fileSystem).move(path, newPath);
    }

    @Test
    public void testMarkSuccessMovedToSuccessFolder() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markSuccess();

        verify(fileSystem).move(newPath, successPath);
    }

    @Test
    public void testMarkSuccessClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(File.class))).thenReturn(spiedStream);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();
        fileImportOccurrence.getContents();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markSuccess();

        verify(spiedStream).close();
    }

    @Test
    public void testMarkFailureMovedToFailureFolder() {
        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markFailure();

        verify(fileSystem).move(newPath, failurePath);
    }

    @Test
    public void testMarkFailureClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(File.class))).thenReturn(spiedStream);

        FileImportOccurrence fileImportOccurrence = FileImportOccurrenceImpl.create(fileSystem, dataModel, fileNameCollisionResolver, thesaurus, importSchedule, file);
        ((FileImportOccurrenceImpl)fileImportOccurrence).setLogger(logger);
        fileImportOccurrence.prepareProcessing();
        fileImportOccurrence.getContents();

        Mockito.reset(fileSystem);

        fileImportOccurrence.markFailure();

        verify(spiedStream).close();
    }


}
