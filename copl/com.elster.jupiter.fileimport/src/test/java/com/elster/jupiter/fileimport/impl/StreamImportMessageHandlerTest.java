/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamImportMessageHandlerTest {
    private static final byte[] PAYLOAD = "PAYLOAD".getBytes();
    private static final long FILE_IMPORT_ID = 17L;
    private static final String IMPORTER_NAME = "IMPORTER1";
    private StreamImportMessageHandler streamImportMessageHandler;

    @Mock
    private FileImporter fileImporter;
    @Mock
    private Message message;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private JsonService jsonService;
    @Mock
    private ServerFileImportOccurrence fileImportOccurrence;
    @Mock
    private ImportSchedule importSchedule;
    @Mock
    private FileImporterFactory fileImporterFactory;
    @Mock
    private Clock clock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;

    @Before
    public void setUp() throws SQLException {
        when(clock.instant()).thenReturn(Instant.now());
        when(fileImportOccurrence.getId()).thenReturn(FILE_IMPORT_ID);
        when(fileImportOccurrence.getStatus()).thenReturn(Status.PROCESSING);
        when(fileImportOccurrence.getLogger()).thenReturn(Logger.getAnonymousLogger());
        FileImportMessage fileImportMessage = new FileImportMessage(fileImportOccurrence, "AppServerName");
        when(message.getPayload()).thenReturn(PAYLOAD);
        when(jsonService.deserialize(aryEq(PAYLOAD), eq(FileImportMessage.class))).thenReturn(fileImportMessage);
        when(dataModel.mapper(FileImportOccurrence.class).getOptional(FILE_IMPORT_ID)).thenReturn(Optional.of(fileImportOccurrence));
        when(importSchedule.getImporterName()).thenReturn(IMPORTER_NAME);
        when(fileImportOccurrence.getImportSchedule()).thenReturn(importSchedule);
        when(fileImportService.getImportFactory(IMPORTER_NAME)).thenReturn(Optional.of(fileImporterFactory));
        when(fileImportService.getFileImportOccurrence(Matchers.anyLong())).thenReturn(Optional.of(fileImportOccurrence));
        when(importSchedule.getImporterProperties()).thenReturn(new ArrayList<>());
        when(fileImporterFactory.requiresTransaction()).thenReturn(true);
        when(fileImporterFactory.createImporter(Matchers.anyMapOf(String.class, Object.class))).thenReturn(fileImporter);
        streamImportMessageHandler = new StreamImportMessageHandler(jsonService, thesaurus, fileImportService, transactionService);
    }

    @Test
    public void testProcessPassesFileImportToFileImporter() {
        streamImportMessageHandler.process(message);
        verify(fileImporter).process(fileImportOccurrence);
        verify(fileImportOccurrence, never()).markFailure(anyString());
        verify(fileImportOccurrence).save();
    }

    @Test
    public void testHandlerException() {
        doThrow(new RuntimeException("fooled you!!")).when(fileImporter).process(fileImportOccurrence);

        streamImportMessageHandler.process(message);
        verify(fileImportOccurrence).markFailure("fooled you!!");
        verify(fileImportOccurrence).save();
    }
}
