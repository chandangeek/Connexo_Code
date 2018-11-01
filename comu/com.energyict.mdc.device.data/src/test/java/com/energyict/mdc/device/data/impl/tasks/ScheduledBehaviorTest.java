package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Jozsef Szekrenyes on 10/31/2018.
 */
public class ScheduledBehaviorTest {
    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private SchedulingService schedulingService;
    @Mock
    private Device device;
    @Mock
    private ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
    private List<ComTask> comTasks;

    @Test
    public void testMaxNumberOfRetriesWhenScheduleHasComTasks() {
        comTasks = createComTasks();
        ComTaskExecutionImpl comTaskExecution = setup();
        assertThat(comTaskExecution.getMaxNumberOfTries()).isEqualTo(7);
    }

    @Test
    public void testMaxNumberOfRetriesWhenScheduleHasNoComTasks() {
        comTasks = new ArrayList<>();
        ComTaskExecutionImpl comTaskExecution = setup();
        assertThat(comTaskExecution.getMaxNumberOfTries()).isEqualTo(3);
    }

    private ComTaskExecutionImpl setup() {
        ComTaskExecutionImpl comTaskExecution = new ComTaskExecutionImpl(dataModel, eventService, thesaurus, clock, communicationTaskService, schedulingService);
        when(comTaskEnablement.getConnectionFunction()).thenReturn(Optional.empty());
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.empty());
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getComTasks()).thenReturn(comTasks);

        comTaskExecution.initializeForScheduledComTask(device, comTaskEnablement, comSchedule);
        return comTaskExecution;
    }

    private List<ComTask> createComTasks() {
        List<ComTask> comTasks = new ArrayList<>();
        ComTask comTask1 = createComTask(7);
        ComTask comTask2 = createComTask(9);
        comTasks.add(comTask1);
        comTasks.add(comTask2);

        return comTasks;
    }

    private ComTask createComTask(int nbRetries) {
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getMaxNrOfTries()).thenReturn(nbRetries);
        return comTask1;
    }
}
