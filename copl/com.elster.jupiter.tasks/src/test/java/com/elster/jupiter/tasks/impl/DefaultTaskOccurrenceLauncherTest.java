package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultTaskOccurrenceLauncherTest {

    private static final String SERIALIZED1 = "S1";
    private static final String SERIALIZED2 = "S2";
    private DefaultTaskOccurrenceLauncher defaultTaskOccurrenceLauncher;
    
    @Mock
    DueTaskFetcher dueTaskFetcher;
    @Mock
    private RecurrentTask task1, task2;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private JsonService jsonService;
    @Mock
    private TaskOccurrence taskOccurrence1, taskOccurrence2;
    @Mock
    private Clock clock;
    @Mock
    private DestinationSpec destination1, destination2;
    @Mock
    private MessageBuilder builder1, builder2;

    @Before
    public void setUp() {
        defaultTaskOccurrenceLauncher = new DefaultTaskOccurrenceLauncher(dueTaskFetcher);

        when(dueTaskFetcher.dueTasks()).thenReturn(Arrays.asList(task1, task2));
        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(serviceLocator.getClock()).thenReturn(clock);
        when(task1.createTaskOccurrence()).thenReturn(taskOccurrence1);
        when(task2.createTaskOccurrence()).thenReturn(taskOccurrence2);
        when(task1.getDestination()).thenReturn(destination1);
        when(task2.getDestination()).thenReturn(destination2);
        when(destination1.message(SERIALIZED1)).thenReturn(builder1);
        when(destination2.message(SERIALIZED2)).thenReturn(builder2);
        when(jsonService.serialize(any(TaskOccurrenceMessage.class))).thenReturn(SERIALIZED1, SERIALIZED2);

        Bus.setServiceLocator(serviceLocator);

    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testRun() {
        defaultTaskOccurrenceLauncher.run();

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionService).execute(transactionCaptor.capture());

        verify(task1, never()).save(); // never save outside transaction

        transactionCaptor.getValue().perform();

        verify(destination1).message(SERIALIZED1);
        verify(builder1).send();
        verify(task1).save(); // does save within transaction

        verify(destination2).message(SERIALIZED2);
        verify(builder2).send();
        verify(task2).save(); // does save within transaction

    }

}
