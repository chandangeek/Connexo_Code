package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecurrentTaskImplTest {

    private static final String PAYLOAD = "payload";
    private static final String NAME = "name";
    private static final Date NOW = new Date(5000000);
    private static final Date NEXT = new Date(6000000);

    private RecurrentTaskImpl recurrentTask;

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private OrmClient ormClient;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private DestinationSpec destination;
    @Mock
    private Clock clock;
    @Mock
    private DataMapper<TaskOccurrence> taskOccurrenceFactory;
    @Mock
    private DataMapper<RecurrentTask> recurrentTaskFactory;

    @Before
    public void setUp() {
        recurrentTask = new RecurrentTaskImpl(NAME, cronExpression, destination, PAYLOAD);

        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(serviceLocator.getClock()).thenReturn(clock);
        when(ormClient.getTaskOccurrenceFactory()).thenReturn(taskOccurrenceFactory);
        when(ormClient.getRecurrentTaskFactory()).thenReturn(recurrentTaskFactory);
        when(clock.now()).thenReturn(NOW);
        when(cronExpression.nextAfter(NOW)).thenReturn(NEXT);


        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testGetDestination() {
        assertThat(recurrentTask.getDestination()).isEqualTo(destination);
    }


    @Test
    public void testUpdateNextExecution() {
        recurrentTask.updateNextExecution();

        assertThat(recurrentTask.getNextExecution()).isEqualTo(NEXT);
    }

    @Test
    public void testCreateTaskOccurrence() {

        TaskOccurrence taskOccurrence = recurrentTask.createTaskOccurrence();

        verify(taskOccurrenceFactory).persist(taskOccurrence);
        assertThat(taskOccurrence.getPayLoad()).isEqualTo(PAYLOAD);
        assertThat(taskOccurrence.getRecurrentTask()).isEqualTo(recurrentTask);
        assertThat(taskOccurrence.getTriggerTime()).isEqualTo(NOW);
    }

    @Test
    public void testSave() {
        recurrentTask.save();

        verify(recurrentTaskFactory).persist(recurrentTask);
    }

    @Test
    public void testDelete() {
        recurrentTask.delete();

        verify(recurrentTaskFactory).remove(recurrentTask);
    }

    @Test
    public void testSuspend() {
        recurrentTask.updateNextExecution();
        recurrentTask.suspend();

        assertThat(recurrentTask.getNextExecution()).isNull();
        verify(recurrentTaskFactory).persist(recurrentTask);
    }

    @Test
    public void testResume() {
        recurrentTask.resume();

        assertThat(recurrentTask.getNextExecution()).isEqualTo(NEXT);
        verify(recurrentTaskFactory).persist(recurrentTask);

    }

}
