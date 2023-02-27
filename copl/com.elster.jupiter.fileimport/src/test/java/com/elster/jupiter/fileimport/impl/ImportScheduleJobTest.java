/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleJobTest {
    private static final long ID = 354156L;
    private static final String SERIALIZED = "SERIALIZED";
    private ImportScheduleJob importScheduleJob;

    @Mock
    private ServerImportSchedule importSchedule;
    @Mock
    private ScheduleExpression scheduleExpression;
    @Mock
    private FileUtils fileUtils;
    @Mock
    private DirectoryStream<Path> directoryStream;
    private final TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private ServerFileImportOccurrence fileImportOccurrence;
    @Mock
    private JsonService jsonService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destination;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private Clock clock;
    private FileSystem testFileSystem;
    private Path path;
    private Path basePath;
    private Path importDir;

    @Before
    public void setUp() throws IOException {
        testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        basePath = testFileSystem.getRootDirectories().iterator().next();
        importDir = basePath.resolve("Import");
        Files.createDirectory(importDir);
        path = importDir.resolve("file");
        Files.createFile(path);

        when(importSchedule.getScheduleExpression()).thenReturn(scheduleExpression);
        when(importSchedule.getImportDirectory()).thenReturn(importDir);
        when(importSchedule.getId()).thenReturn(1L);
        when(importSchedule.isActive()).thenReturn(true);
        when(fileImportService.getImportSchedule(1L)).thenReturn(Optional.of(importSchedule));
        when(fileImportService.getBasePath()).thenReturn(basePath);
        when(fileImportService.getAppServerName()).thenReturn(Optional.of("appServerName"));
        when(fileUtils.newDirectoryStream(importDir, null)).thenReturn(directoryStream);
        when(fileImportOccurrence.getId()).thenReturn(ID);

        importScheduleJob = new ImportScheduleJob(path -> true, fileUtils, jsonService, fileImportService, importSchedule.getId(), transactionService, thesaurus, cronExpressionParser,clock);
    }

    @After
    public void tearDown() throws IOException {
        testFileSystem.close();
    }

    @Test
    public void testKeepsScheduleExpression() {
        assertThat(importScheduleJob.getSchedule()).isEqualTo(scheduleExpression);
    }

    @Test
    public void testRun() throws Exception {
        doAnswer(invocationOnMock -> {
            Consumer<? super Path> consumer = (Consumer<? super Path>) invocationOnMock.getArguments()[0];
            consumer.accept(path);
            return Void.TYPE;
        }).when(directoryStream).forEach(Matchers.any());
        when(importSchedule.createFileImportOccurrence(path, clock)).thenReturn(fileImportOccurrence);
        when(fileImportOccurrence.getStatus()).thenReturn(Status.PROCESSING);
        when(importSchedule.isActive()).thenReturn(true);
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);
        when(importSchedule.getDestination()).thenReturn(destination);

        importScheduleJob.run();

        verify(destination).message(SERIALIZED);
        verify(directoryStream).close();//COMU-1864
    }
}
