package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataMapper;
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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileImportImplTest {

    private static final long ID = 5564;
    private static final String FILE_NAME = "fileName";
    private static final String CONTENTS = "CONTENTS";
    @Mock
    private OrmClient ormClient;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private DataMapper<FileImport> fileImportFactory;
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

    @Before
    public void setUp() {

        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(serviceLocator.getFileNameCollisionResollver()).thenReturn(fileNameCollisionResolver);
        when(file.toPath()).thenReturn(path);
        when(ormClient.getFileImportFactory()).thenReturn(fileImportFactory);
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
        when(serviceLocator.getFileSystem()).thenReturn(fileSystem);
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

        Bus.setServiceLocator(serviceLocator);
    }

    private ByteArrayInputStream contentsAsStream() {
        return new ByteArrayInputStream(CONTENTS.getBytes());
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testCreateKeepsReferenceToImportSchedule() {
        FileImport fileImport = FileImportImpl.create(importSchedule, file);
        fileImport.prepareProcessing();

        assertThat(fileImport.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test
    public void testCreateKeepsReferenceToFile() {
        when(path.getFileName()).thenReturn(path);

        FileImport fileImport = FileImportImpl.create(importSchedule, file);
        fileImport.prepareProcessing();

        assertThat(fileImport.getFileName()).isEqualTo("newPath");
    }

    @Test
    public void testGetContents() {
        FileImport fileImport = FileImportImpl.create(importSchedule, file);

        InputStream contents = fileImport.getContents();

        assertThat(contents).hasContentEqualTo(contentsAsStream());
    }

    @Test
    public void testMovedToProcessing() {
        FileImport fileImport = FileImportImpl.create(importSchedule, file);
        fileImport.prepareProcessing();

        verify(fileSystem).move(path, newPath);
    }

    @Test
    public void testMarkSuccessMovedToSuccessFolder() {
        FileImport fileImport = FileImportImpl.create(importSchedule, file);
        fileImport.prepareProcessing();

        Mockito.reset(fileSystem);

        fileImport.markSuccess();

        verify(fileSystem).move(newPath, successPath);
    }

    @Test
    public void testMarkSuccessClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(File.class))).thenReturn(spiedStream);

        FileImport fileImport = FileImportImpl.create(importSchedule, file);
        fileImport.prepareProcessing();
        fileImport.getContents();

        Mockito.reset(fileSystem);

        fileImport.markSuccess();

        verify(spiedStream).close();
    }

    @Test
    public void testMarkFailureMovedToFailureFolder() {
        FileImport fileImport = FileImportImpl.create(importSchedule, file);
        fileImport.prepareProcessing();

        Mockito.reset(fileSystem);

        fileImport.markFailure();

        verify(fileSystem).move(newPath, failurePath);
    }

    @Test
    public void testMarkFailureClosesStream() throws IOException {
        ByteArrayInputStream spiedStream = spy(contentsAsStream());
        when(fileSystem.getInputStream(any(File.class))).thenReturn(spiedStream);

        FileImport fileImport = FileImportImpl.create(importSchedule, file);
        fileImport.prepareProcessing();
        fileImport.getContents();

        Mockito.reset(fileSystem);

        fileImport.markFailure();

        verify(spiedStream).close();
    }


}
