package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
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
    private FileImport fileImport;
    @Mock
    private DestinationSpec destination;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageBuilder messageBuilder;

    @Before
    public void setUp() {

        when(importSchedule.createFileImport(any(File.class))).thenReturn(fileImport);
        when(importSchedule.getDestination()).thenReturn(destination);
        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);
        when(destination.message(SERIALIZED)).thenReturn(messageBuilder);

        when(transactionService.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });

        Bus.setServiceLocator(serviceLocator);

        fileHandler = new DefaultFileHandler(importSchedule);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testHandleCreatesFileImport() {

        File file = new File("./test.txt");
        fileHandler.handle(file);

        verify(importSchedule).createFileImport(file);

    }

    @Test
    public void testHandlePostsMessage() {

        File file = new File("./test.txt");
        fileHandler.handle(file);

        verify(messageBuilder).send();

    }


}
