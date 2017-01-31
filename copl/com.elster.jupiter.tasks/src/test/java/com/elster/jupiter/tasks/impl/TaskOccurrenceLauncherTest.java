/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
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

import java.time.Clock;
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

    @Mock
    private DestinationSpec destinationSpec1, destinationSpec2;
    @Mock
    private DueTaskFetcher dueTaskFetcher;
    @Mock
    private RecurrentTaskImpl recurrentTask1, recurrentTask2;
    @Mock
    private Clock clock;
    @Mock
    private TaskOccurrenceImpl taskOccurrence1, taskOccurrence2;
    @Mock
    private TransactionService transactionService;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private JsonService jsonService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;

    @Before
    public void setUp() {
//        when(serviceLocator.getClock()).thenReturn(clock);
//        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
//        when(serviceLocator.getMessageService().getDestinationSpec(DS_NAME1)).thenReturn(Optional.of(destinationSpec1));
//        when(serviceLocator.getMessageService().getDestinationSpec(DS_NAME2)).thenReturn(Optional.of(destinationSpec2));
//        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(dueTaskFetcher.dueTasks()).thenReturn(Arrays.asList(recurrentTask1, recurrentTask2));
        when(recurrentTask1.getDestination()).thenReturn(destinationSpec1);
        when(recurrentTask1.createScheduledTaskOccurrence()).thenReturn(taskOccurrence1);
        when(taskOccurrence1.getId()).thenReturn(1L);
        when(taskOccurrence2.getId()).thenReturn(2L);
        when(recurrentTask2.getDestination()).thenReturn(destinationSpec2);
        when(recurrentTask2.createScheduledTaskOccurrence()).thenReturn(taskOccurrence2);
        when(destinationSpec1.message(anyString())).thenReturn(messageBuilder);
        when(destinationSpec2.message(anyString())).thenReturn(messageBuilder);

        when(transactionService.execute(any())).thenAnswer(new Answer<Object>() {
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

        launcher = new DefaultTaskOccurrenceLauncher(threadPrincipalService, transactionService, dueTaskFetcher);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRun() throws Exception {
        launcher.run();

        verify(recurrentTask1).launchOccurrence();
        verify(recurrentTask2).launchOccurrence();
    }
}
