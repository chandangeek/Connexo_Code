/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.ItemizeComTaskEnablementQueueMessage;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComTaskEnablementChangeEventHandlerTest {

    @Mock
    private List comTaskExecutions;
    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;
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
        QueryExecutor queryExecutor = mock(QueryExecutor.class);
        when(queryExecutor.select(any(Condition.class), any(), anyBoolean(), any(), anyInt(), anyInt())).thenReturn(comTaskExecutions);
        DataModel dataModel = mock(DataModel.class);
        when(dataModel.query(any(Class.class), anyVararg())).thenReturn(queryExecutor);
        when(this.deviceDataModelService.dataModel()).thenReturn(dataModel);
        when(this.deviceDataModelService.thesaurus()).thenReturn(this.thesaurus);
        this.eventHandler = new ComTaskEnablementChangeEventHandler(this.deviceDataModelService, this.schedulingService);
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.comTaskEnablement.getComTask()).thenReturn(this.comTask);
        Finder scheduleFinder = mock(Finder.class);
        List<ComSchedule> comSchedules = Arrays.asList(this.comSchedule1, this.comSchedule2);
        when(scheduleFinder.find()).thenReturn(comSchedules);
        when(scheduleFinder.stream()).thenReturn(comSchedules.stream());
        when(this.schedulingService.findAllSchedules()).thenReturn(scheduleFinder);

        String helloFromMyTest = "HelloFromMyTest";
        when(this.jsonService.serialize(any(ItemizeComTaskEnablementQueueMessage.class))).thenReturn(helloFromMyTest);
        when(this.deviceDataModelService.jsonService()).thenReturn(this.jsonService);
        when(this.deviceDataModelService.messageService()).thenReturn(this.messageService);

        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(any(String.class))).thenReturn(messageBuilder);
        MessageService messageService = mock(MessageService.class);
        when(deviceDataModelService.messageService()).thenReturn(messageService);
        when(messageService.getDestinationSpec(ComTaskEnablementChangeMessageHandler.COMTASK_ENABLEMENT_QUEUE_DESTINATION)).thenReturn(Optional.of(destinationSpec));
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
        when(this.comSchedule2.getComTasks()).thenReturn(Collections.singletonList(this.comTask));

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
        when(this.comTaskExecutions.isEmpty()).thenReturn(false);

        this.eventHandler.handle(this.event);

        verify(this.event).getSource();
        verify(this.comSchedule1).containsComTask(this.comTask);
        verify(this.comSchedule2).containsComTask(this.comTask);
        verify(this.comSchedule2).getComTasks();
    }
}