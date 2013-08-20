package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskOccurrenceLauncherTest {

    private static final String DS_NAME1 = "DSName1";
    private static final String DS_NAME2 = "DSName2";

    private TaskOccurrenceLauncher launcher;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private DestinationSpec destinationSpec1, destinationSpec2;
    @Mock
    private DueTaskFetcher dueTaskFetcher;
    @Mock
    private RecurrentTask recurrentTask1, recurrentTask2;
    @Mock
    private Clock clock;
    @Mock
    private TaskOccurrence taskOccurrence1, taskOccurrence2;
    @Mock
    private TransactionService transactionService;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private JsonService jsonService;

    @Before
    public void setUp() {
        Bus.setServiceLocator(serviceLocator);

        when(serviceLocator.getClock()).thenReturn(clock);

        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
        when(serviceLocator.getMessageService().getDestinationSpec(DS_NAME1)).thenReturn(Optional.of(destinationSpec1));
        when(serviceLocator.getMessageService().getDestinationSpec(DS_NAME2)).thenReturn(Optional.of(destinationSpec2));
        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(dueTaskFetcher.dueTasks()).thenReturn(Arrays.asList(recurrentTask1, recurrentTask2));
        when(recurrentTask1.getDestination()).thenReturn(destinationSpec1);
        when(recurrentTask1.createTaskOccurrence(clock)).thenReturn(taskOccurrence1);
        when(taskOccurrence1.getId()).thenReturn(1L);
        when(taskOccurrence2.getId()).thenReturn(2L);
        when(recurrentTask2.getDestination()).thenReturn(destinationSpec2);
        when(recurrentTask2.createTaskOccurrence(clock)).thenReturn(taskOccurrence2);
        when(destinationSpec1.message(anyString())).thenReturn(messageBuilder);
        when(destinationSpec2.message(anyString())).thenReturn(messageBuilder);

        when(transactionService.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction) invocationOnMock.getArguments()[0]).perform();
            }
        });
        when(jsonService.serialize(any(TaskOccurrenceMessage.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return '{' + String.valueOf(((TaskOccurrenceMessage) invocationOnMock.getArguments()[0]).taskOccurrenceId) + '}';
            }
        });

        launcher = new DefaultTaskOccurrenceLauncher(dueTaskFetcher);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testRun() throws Exception {
        launcher.run();

        verify(destinationSpec1).message("{1}");
        verify(destinationSpec2).message("{2}");
        verify(recurrentTask1).save();
        verify(recurrentTask2).save();
    }
}
