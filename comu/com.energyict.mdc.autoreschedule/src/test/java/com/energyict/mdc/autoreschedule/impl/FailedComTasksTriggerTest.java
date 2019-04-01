package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.sql.Fetcher;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ComTask;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class FailedComTasksTriggerTest {

    private static final TimeDuration EVERY_HOUR = new TimeDuration(1, TimeDuration.TimeUnit.HOURS);
    private final TemporalExpression temporalExpression = new TemporalExpression(EVERY_HOUR);
    private final List<ComTaskExecution> comTaskExecs = new ArrayList<>();
    private FailedComTasksTrigger failedComTasksTrigger;

    @Mock
    TaskParametersProvider taskParametersProvider;
    @Mock
    NextExecutionSpecs executionSpecs;
    @Mock
    ComTask comTask;

    @Before
    public void setUp() {
        when(executionSpecs.getTemporalExpression()).thenReturn(temporalExpression);
        Fetcher<ComTaskExecution> fetcher = new Fetcher<ComTaskExecution>() {
            @Override
            public void close() {
            }

            @Override
            public Iterator<ComTaskExecution> iterator() {
                return comTaskExecs.iterator();
            }
        };

        when(taskParametersProvider.getComTaskExecutionsForDevicesByComTask()).thenReturn(fetcher);
        when(taskParametersProvider.getTaskInterval()).thenReturn(1800);
        when(comTask.getName()).thenReturn("Test ComTask");
    }

    @Test
    public void whenAutoRescheduleTaskIsMoreFrequentThanComTasksThenAllComTasksAreTriggered() {
        createComTaskExecs(3, 0);
        failedComTasksTrigger = new FailedComTasksTrigger(taskParametersProvider);

        long result = failedComTasksTrigger.runNow();
        assertEquals(3, result);
        comTaskExecs.forEach(cte -> verify(cte, times(1)).runNow());
    }

    @Test
    public void whenAutoRescheduleTaskIsLessFrequentThanComTasksThenNoComTaskIsTriggered() {
        when(taskParametersProvider.getTaskInterval()).thenReturn(7200);
        createComTaskExecs(3, 0);
        failedComTasksTrigger = new FailedComTasksTrigger(taskParametersProvider);

        long result = failedComTasksTrigger.runNow();
        assertEquals(0, result);
        comTaskExecs.forEach(cte -> verify(cte, never()).runNow());
    }

    @Test
    public void whenAutoRescheduleTaskIsAdHocThenAllComTasksAreTriggered() {
        createComTaskExecs(3, 2);
        when(taskParametersProvider.getTaskInterval()).thenReturn(-1);
        failedComTasksTrigger = new FailedComTasksTrigger(taskParametersProvider);

        long result = failedComTasksTrigger.runNow();
        assertEquals(5, result);
        comTaskExecs.forEach(cte -> verify(cte, times(1)).runNow());
    }

    @Test
    public void whenMixedComTasksThenOnlyLessFrequentScheduledOnesAreTriggered() {
        createComTaskExecs(3, 2);
        failedComTasksTrigger = new FailedComTasksTrigger(taskParametersProvider);

        long result = failedComTasksTrigger.runNow();
        assertEquals(3, result);
        comTaskExecs.forEach(cte -> {
            if (cte.getNextExecutionSpecs().isPresent()) {
                verify(cte, times(1)).runNow();
            } else {
                verify(cte, never()).runNow();
            }
        });
    }

    private void createComTaskExecs(int nbScheduled, int nbAdHoc) {
        for (int i = 0; i < nbScheduled; ++i) {
            comTaskExecs.add(createScheduledComTaskExec());
        }
        for (int i = 0; i < nbAdHoc; ++i) {
            comTaskExecs.add(createAdHocComTaskExec());
        }
    }

    private ComTaskExecution createAdHocComTaskExec() {
        ComTaskExecution cte = mock(ComTaskExecution.class);
        when(cte.getNextExecutionSpecs()).thenReturn(Optional.empty());
        when(cte.getComTask()).thenReturn(comTask);

        return cte;
    }

    private ComTaskExecution createScheduledComTaskExec() {
        ComTaskExecution cte = mock(ComTaskExecution.class);
        when(cte.getNextExecutionSpecs()).thenReturn(Optional.of(executionSpecs));
        when(cte.getComTask()).thenReturn(comTask);

        return cte;
    }
}
