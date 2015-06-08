package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.json.JsonService;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class StreamImportMessageHandlerTest {

    private static final byte[] PAYLOAD = "PAYLOAD".getBytes();
    private static final long FILE_IMPORT_ID = 17L;
    private static final String IMPORTER_NAME = "IMPORTER1";
    private StreamImportMessageHandler streamImportMessageHandler;

    private FileImportMessage fileImportMessage;

    @Mock
    private FileImporter fileImporter;
    @Mock
    private Message message;
    @Mock
    private FileImportService fileImportService;

    @Mock
    private Thesaurus thesaurus;

    @Mock
    private JsonService jsonService;
    @Mock
    private FileImportOccurrence fileImportOccurrence;

    @Mock
    private ImportSchedule importSchedule;

    @Mock
    private FileImporterFactory fileImporterFactory;

    @Mock
    private Clock clock;

    @Mock
    FileImporter importer;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;

    @Before
    public void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
        when(fileImportOccurrence.getId()).thenReturn(FILE_IMPORT_ID);
        fileImportMessage = new FileImportMessage(fileImportOccurrence);
        when(message.getPayload()).thenReturn(PAYLOAD);
        when(jsonService.deserialize(aryEq(PAYLOAD), eq(FileImportMessage.class))).thenReturn(fileImportMessage);
        when(dataModel.mapper(FileImportOccurrence.class).getOptional(FILE_IMPORT_ID)).thenReturn(Optional.of(fileImportOccurrence));
        when(importSchedule.getImporterName()).thenReturn(IMPORTER_NAME);
        when(fileImportOccurrence.getImportSchedule()).thenReturn(importSchedule);
        when(fileImportService.getImportFactory(IMPORTER_NAME)).thenReturn(Optional.of(fileImporterFactory));
        when(fileImportService.getFileImportOccurrence(Matchers.anyLong())).thenReturn(Optional.of(fileImportOccurrence));
        when(fileImportOccurrence.getImportSchedule().getImporterProperties()).thenReturn(new ArrayList<>());
        when(fileImporterFactory.createImporter(Matchers.anyMap())).thenReturn(fileImporter);
        streamImportMessageHandler = new StreamImportMessageHandler(jsonService, thesaurus, clock, fileImportService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testProcessPassesFileImportToFileImporter() {
        streamImportMessageHandler.process(message);
        verify(fileImporter).process(fileImportOccurrence);
    }

}
