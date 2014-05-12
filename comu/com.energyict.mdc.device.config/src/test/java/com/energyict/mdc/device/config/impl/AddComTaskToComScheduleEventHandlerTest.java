package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.scheduling.events.VetoComTaskAdditionException;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComTaskComScheduleLink;
import com.energyict.mdc.tasks.ComTask;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddComTaskToComScheduleEventHandlerTest {

    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    ComTask comTask;
    @Mock
    ComSchedule comSchedule;

    AddComTaskToComScheduleEventHandler eventHandler;
    private LocalEvent localEvent;

    @Before
    public void setUp() throws Exception {
        eventHandler = new AddComTaskToComScheduleEventHandler();
        eventHandler.setDeviceConfigurationService(deviceConfigurationService);
        ComTaskComScheduleLink comTaskComScheduleLink = mock(ComTaskComScheduleLink.class);
        when(comTaskComScheduleLink.getComSchedule()).thenReturn(comSchedule);
        when(comTaskComScheduleLink.getComTask()).thenReturn(comTask);
        localEvent = mock(LocalEvent.class);
        when(localEvent.getSource()).thenReturn(comTaskComScheduleLink);
    }

    @Test
    public void testAllowedAddition() throws Exception {
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(11L);
        when(comTask.getId()).thenReturn(11L);
        when(deviceConfigurationService.findAvailableComTasks(comSchedule)).thenReturn(Arrays.asList(comTask1));
        when(comSchedule.getComTasks()).thenReturn(Collections.<ComTask>emptyList());

        eventHandler.handle(localEvent);
    }

    @Test(expected = VetoComTaskAdditionException.class)
    public void testAddAlreadyLinkedComTask() throws Exception {
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(11L);
        when(comTask.getId()).thenReturn(11L);
        when(deviceConfigurationService.findAvailableComTasks(comSchedule)).thenReturn(Arrays.asList(comTask1));
        when(comSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1));

        eventHandler.handle(localEvent);
    }

    @Test(expected = VetoComTaskAdditionException.class)
    public void testAddIllegalComTask() throws Exception {
        ComTask comTask2 = mock(ComTask.class);
        when(comTask2.getId()).thenReturn(12L);
        when(comTask.getId()).thenReturn(11L);
        when(deviceConfigurationService.findAvailableComTasks(comSchedule)).thenReturn(Arrays.asList(comTask2));
        when(comSchedule.getComTasks()).thenReturn(Collections.<ComTask>emptyList());

        eventHandler.handle(localEvent);
    }
}
