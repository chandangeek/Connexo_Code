package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComTaskEnablementChangeEventHandlerTest {

    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private SchedulingService schedulingService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComSchedule comSchedule1;
    @Mock
    private ComSchedule comSchedule2;
    @Mock
    private LocalEvent event;

    private ComTaskEnablementChangeEventHandler eventHandler;

    @Before
    public void createEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn("com/energyict/mdc/device/config/comtaskenablement/UPDATED");
        when(this.event.getSource()).thenReturn(this.comTaskEnablement);
        when(this.event.getType()).thenReturn(eventType);
    }

    @Before
    public void createEventHandler() {
        when(this.deviceDataModelService.thesaurus()).thenReturn(this.thesaurus);
        when(this.deviceDataModelService.communicationTaskService()).thenReturn(this.communicationTaskService);
        this.eventHandler = new ComTaskEnablementChangeEventHandler(this.deviceDataModelService, this.schedulingService);
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.comTaskEnablement.getComTask()).thenReturn(this.comTask);
        Finder scheduleFinder = mock(Finder.class);
        List<ComSchedule> comSchedules = Arrays.asList(this.comSchedule1, this.comSchedule2);
        when(scheduleFinder.find()).thenReturn(comSchedules);
        when(scheduleFinder.stream()).thenReturn(comSchedules.stream());
        when(this.schedulingService.findAllSchedules()).thenReturn(scheduleFinder);
    }

    @Test
    public void testNoExceptionForInactiveConfig() {
        when(this.deviceConfiguration.isActive()).thenReturn(false);
        this.eventHandler.handle(this.event);
    }

    @Test
    public void testNoExceptionForNoComSchedules() {
        when(this.deviceConfiguration.isActive()).thenReturn(true);
        when(this.comSchedule1.containsComTask(this.comTask)).thenReturn(false);
        when(this.comSchedule2.containsComTask(this.comTask)).thenReturn(false);

        this.eventHandler.handle(this.event);

        verify(this.event).getSource();
        verify(this.comSchedule1).containsComTask(this.comTask);
        verify(this.comSchedule2).containsComTask(this.comTask);
    }


    @Test
    public void testNoExceptionForComScheduleWithSingleComTask() {
        when(this.deviceConfiguration.isActive()).thenReturn(true);
        when(this.comSchedule1.containsComTask(this.comTask)).thenReturn(false);
        when(this.comSchedule2.containsComTask(this.comTask)).thenReturn(true);
        when(this.comSchedule2.getComTasks()).thenReturn(Arrays.asList(this.comTask));

        this.eventHandler.handle(this.event);

        verify(this.event).getSource();
        verify(this.comSchedule1).containsComTask(this.comTask);
        verify(this.comSchedule2).containsComTask(this.comTask);
        verify(this.comSchedule2).getComTasks();
    }

    @Test(expected = VetoComTaskEnablementChangeException.class)
    public void testInUse() {
        when(this.deviceConfiguration.isActive()).thenReturn(true);
        when(this.comSchedule1.containsComTask(this.comTask)).thenReturn(false);
        when(this.comSchedule2.containsComTask(this.comTask)).thenReturn(true);
        ComTask anotherComTask = mock(ComTask.class);
        when(this.comSchedule2.getComTasks()).thenReturn(Arrays.asList(this.comTask, anotherComTask));
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(this.communicationTaskService.findComTaskExecutionsByFilter(any(ComTaskExecutionFilterSpecification.class), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(comTaskExecution));

        this.eventHandler.handle(this.event);

        verify(this.event).getSource();
        verify(this.comSchedule1).containsComTask(this.comTask);
        verify(this.comSchedule2).containsComTask(this.comTask);
        verify(this.comSchedule2).getComTasks();
    }

}