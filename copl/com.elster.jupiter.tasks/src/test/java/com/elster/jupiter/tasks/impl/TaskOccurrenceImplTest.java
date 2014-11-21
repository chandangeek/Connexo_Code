package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskOccurrenceImplTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private RecurrentTask recurrentTask;

    @Before
    public void setUp() {
        when(dataModel.getInstance(TaskOccurrenceImpl.class)).thenAnswer(invocation -> new TaskOccurrenceImpl(dataModel));
        when(dataModel.getInstance(TaskLogEntryImpl.class)).thenAnswer(invocation -> new TaskLogEntryImpl());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreatedLogHandlerTriggersLogEntriesBeingAdded() {
        TaskOccurrenceImpl taskOccurrence = TaskOccurrenceImpl.createScheduled(dataModel, recurrentTask, Instant.EPOCH);

        Handler handler = taskOccurrence.createTaskLogHandler().asHandler();

        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(handler);

        logger.log(Level.WARNING, "Whoops");

// TODO get log entries        assertThat(recurrentTask.get)
    }

}