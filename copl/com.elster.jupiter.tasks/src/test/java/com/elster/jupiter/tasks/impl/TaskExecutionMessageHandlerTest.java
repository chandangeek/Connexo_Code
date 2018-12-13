/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.json.JsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskExecutionMessageHandlerTest {

    private static final byte[] PAYLOAD = "PAYLOAD".getBytes();
    private static final long ID = 5641;

    private TaskExecutionMessageHandler taskExecutionMessageHandler;

    private TaskOccurrenceMessage taskOccurrenceMessage;

    @Mock
    private JsonService jsonService;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private Message message;
    @Mock
    private TaskOccurrenceImpl taskOccurrence;
    @Mock
    private DataMapper<TaskOccurrence> taskOccurrenceFactory;
    @Mock
    private SQLException sqlException;
    @Mock
    private DataModel dataModel;

    private TransactionVerifier transactionService;

    @Before
    public void setUp() {

        transactionService = new TransactionVerifier(taskOccurrence, taskExecutor);
        taskExecutionMessageHandler = new TaskExecutionMessageHandler(dataModel, taskExecutor, jsonService, transactionService);

        when(message.getPayload()).thenReturn(PAYLOAD);
        when(taskOccurrence.getId()).thenReturn(ID);
        taskOccurrenceMessage = new TaskOccurrenceMessage(taskOccurrence);
        when(taskOccurrenceFactory.getOptional(ID)).thenReturn(Optional.of(taskOccurrence));
        when(taskOccurrenceFactory.getExisting(ID)).thenReturn(taskOccurrence);
        when(jsonService.deserialize(PAYLOAD, TaskOccurrenceMessage.class)).thenReturn(taskOccurrenceMessage);
        when(dataModel.mapper(TaskOccurrence.class)).thenReturn(taskOccurrenceFactory);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPropagateTaskOccurrence() {

        taskExecutionMessageHandler.process(message);

        verify(taskExecutor).execute(taskOccurrence);
        verify(taskOccurrence).start();

    }

    @Test
    public void testOnMessageDelete() {

        taskExecutionMessageHandler.onMessageDelete(message);

        verify(taskExecutor, transactionService.notInTransaction()).postExecute(taskOccurrence);
        verify(taskOccurrence, transactionService.inTransaction()).hasRun(true);
    }

    @Test(expected = RuntimeException.class)
    public void testOnMessageDeleteFailure() {

        doThrow(new RuntimeException()).when(taskExecutor).postExecute(taskOccurrence);
        try {
            taskExecutionMessageHandler.onMessageDelete(message);
        } finally {

            verify(taskExecutor, transactionService.notInTransaction()).postExecute(taskOccurrence);
            verify(taskOccurrence, transactionService.inTransaction()).hasRun(false);
        }
    }

}
