package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFileHandlerTest {

    private static final String SERIALIZED = "serialized";
    private DefaultFileHandler fileHandler;

    @Mock
    private ImportSchedule importSchedule;
    @Mock
    private FileImportOccurrence fileImportOccurrence;
    @Mock
    private DestinationSpec destination;
    @Mock
    private TransactionService transactionService;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private Clock clock;

    @Before
    public void setUp() {

        when(clock.instant()).thenReturn(Instant.now());
        when(importSchedule.createFileImportOccurrence(any(Path.class), any(Clock.class))).thenReturn(fileImportOccurrence);
        when(importSchedule.getDestination()).thenReturn(destination);
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);
        when(destination.message(SERIALIZED)).thenReturn(messageBuilder);

        when(transactionService.execute(any())).thenAnswer(invocationOnMock ->
                ((Transaction<?>) invocationOnMock.getArguments()[0]).perform());

        fileHandler = new DefaultFileHandler(importSchedule, jsonService, transactionService, clock);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testHandleCreatesFileImport() {

        Path file = Paths.get("./test.txt");
        fileHandler.handle(file);
        verify(importSchedule).createFileImportOccurrence(file, clock);
    }

    @Test
    public void testHandlePostsMessage() {

        Path file = Paths.get("./test.txt");
        fileHandler.handle(file);

        verify(messageBuilder).send();

    }


}
