package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskExecutionMessageHandlerTest {

    private static final byte[] PAYLOAD = "PAYLOAD".getBytes();
    private static final long ID = 5641;

    private TaskExecutionMessageHandler taskExecutionMessageHandler;

    private TaskOccurrenceMessage taskOccurrenceMessage;

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private JsonService jsonService;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private Message message;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private OrmClient ormClient;
    @Mock
    private DataMapper<TaskOccurrence> taskOccurrenceFactory;
    @Mock
    private SQLException sqlException;

    @Before
    public void setUp() {

        taskExecutionMessageHandler = new TaskExecutionMessageHandler(taskExecutor);

        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getTaskOccurrenceFactory()).thenReturn(taskOccurrenceFactory);
        when(message.getPayload()).thenReturn(PAYLOAD);
        when(taskOccurrence.getId()).thenReturn(ID);
        taskOccurrenceMessage = new TaskOccurrenceMessage(taskOccurrence);
        when(taskOccurrenceFactory.get(ID)).thenReturn(Optional.of(taskOccurrence));
        when(jsonService.deserialize(PAYLOAD, TaskOccurrenceMessage.class)).thenReturn(taskOccurrenceMessage);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testPropagateTaskOccurrence() {

        taskExecutionMessageHandler.process(message);

        verify(taskExecutor).execute(taskOccurrence);

    }

}
