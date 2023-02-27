/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFileHandlerTest {
    private static final String SERIALIZED = "serialized";
    private DefaultFileHandler fileHandler;

    @Mock
    private ServerImportSchedule importSchedule;
    @Mock
    private ServerFileImportOccurrence fileImportOccurrence;
    @Mock
    private DestinationSpec destination;
    private final TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private Clock clock;
    @Mock
    private FileImportService fileImportService;

    private FileSystem testFileSystem;

    @Before
    public void setUp() {
        testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        when(clock.instant()).thenReturn(Instant.now());
        when(importSchedule.createFileImportOccurrence(any(Path.class), any(Clock.class))).thenReturn(fileImportOccurrence);
        when(importSchedule.getDestination()).thenReturn(destination);
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);
        when(destination.message(SERIALIZED)).thenReturn(messageBuilder);
        when(fileImportService.getAppServerName()).thenReturn(Optional.of("appServerName"));

        fileHandler = new DefaultFileHandler(importSchedule, jsonService, transactionService, clock, fileImportService);
    }

    @After
    public void tearDown() throws IOException {
        testFileSystem.close();
    }

    @Test
    public void testHandleCreatesFileImportAndPostsMessage() {
        when(fileImportOccurrence.getStatus()).thenReturn(Status.PROCESSING);
        Path file = testFileSystem.getPath("./test.txt");
        fileHandler.handle(file);

        verify(importSchedule).createFileImportOccurrence(file, clock);
        verify(messageBuilder).send();
    }

    @Test
    public void testFailedBeforeProcessing() {
        when(fileImportOccurrence.getStatus()).thenReturn(Status.FAILURE);

        Path file = testFileSystem.getPath("./test.txt");
        fileHandler.handle(file);

        verify(importSchedule).createFileImportOccurrence(file, clock);
        verify(messageBuilder, never()).send();
    }
}
