package com.elster.jupiter.fileimport.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FolderScanningJobTest {

    private FolderScanningJob folderScanningJob;

    @Mock
    private FolderScanner scanner;
    @Mock
    private FileHandler handler;
    @Mock
    private Path file1, file2;

    @Before
    public void setUp() {

        folderScanningJob = new FolderScanningJob(scanner, handler);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testPassesFilesFromTheScannerToTheHandler() {
        when(scanner.getFiles()).thenReturn(Arrays.asList(file1, file2).stream());

        folderScanningJob.run();

        verify(handler).handle(file1);
        verify(handler).handle(file2);

    }

    @Test
    public void testDoesntPassAnythingToTheHandlerIfThereAreNoFiles() {
        when(scanner.getFiles()).thenReturn(Collections.<Path>emptyList().stream());

        folderScanningJob.run();

        verify(handler, never()).handle(any(Path.class));

    }

}
