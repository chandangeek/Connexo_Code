package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskOccurrenceLauncherTest {

    private static final String DS_NAME1 = "DSName1";
    private static final String DS_NAME2 = "DSName2";
    private static final String PAYLOAD_1 = "Payload1";
    private static final String PAYLOAD_2 = "Payload2";

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

    @Before
    public void setUp() {
        Bus.setServiceLocator(serviceLocator);

        when(serviceLocator.getClock()).thenReturn(clock);
        when(serviceLocator.getMessageService().getDestinationSpec(DS_NAME1)).thenReturn(destinationSpec1);
        when(serviceLocator.getMessageService().getDestinationSpec(DS_NAME2)).thenReturn(destinationSpec2);
        when(dueTaskFetcher.dueTasks()).thenReturn(Arrays.asList(recurrentTask1, recurrentTask2));
        when(recurrentTask1.getDestination()).thenReturn(DS_NAME1);
        when(recurrentTask1.createTaskOccurrence(clock)).thenReturn(taskOccurrence1);
        when(taskOccurrence1.getPayLoad()).thenReturn(PAYLOAD_1);
        when(recurrentTask2.getDestination()).thenReturn(DS_NAME2);
        when(recurrentTask2.createTaskOccurrence(clock)).thenReturn(taskOccurrence2);
        when(taskOccurrence2.getPayLoad()).thenReturn(PAYLOAD_2);

        launcher = new TaskOccurrenceLauncher(dueTaskFetcher);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testRun() throws Exception {
        launcher.run();

        verify(destinationSpec1).send(PAYLOAD_1);
        verify(destinationSpec2).send(PAYLOAD_2);
        verify(taskOccurrence1).save();
        verify(taskOccurrence2).save();
    }
}
