/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.impl.ScheduledComTaskExecutionIdRange;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import org.osgi.service.event.EventConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComScheduleBackgroundObsoleteHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-09 (10:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComScheduleBackgroundObsoleteHandlerTest {

    private static final String START_MESSAGE_PAYLOAD = "MOCKED-PAYLOAD_START";
    private static final String RANGE_MESSAGE_PAYLOAD = "MOCKED-PAYLOAD_RANGE";
    private static final long COM_SCHEDULE_ID = 97;

    @Mock
    private JsonService jsonService;
    @Mock
    private EventService eventService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ComSchedule comSchedule;
    @Mock
    private Message message;

    private ComScheduleBackgroundObsoleteHandler eventHandler;

    @Before
    public void setupComScheduleMock() {
        when(this.comSchedule.getId()).thenReturn(COM_SCHEDULE_ID);
    }

    @Before
    public void createEventHandler() {
        this.eventHandler = new ComScheduleBackgroundObsoleteHandler(this.jsonService, this.eventService, this.communicationTaskService);
    }

    private void createStartMessage() {
        Map<String, Object> startMessageParameters = new HashMap<>();
        startMessageParameters.put("id", COM_SCHEDULE_ID);
        startMessageParameters.put(EventConstants.EVENT_TOPIC, ComScheduleBackgroundObsoleteHandler.START_TOPIC);
        when(this.message.getPayload()).thenReturn(START_MESSAGE_PAYLOAD.getBytes());
        when(this.jsonService.deserialize(START_MESSAGE_PAYLOAD.getBytes(), Map.class)).thenReturn(startMessageParameters);
    }

    private void createRangeMessage(int firstComTaskExecutionId, int lastComTaskExecutionId) {
        Map<String, Object> rangeMessageParameters = new HashMap<>();
        rangeMessageParameters.put("comScheduleId", COM_SCHEDULE_ID);
        rangeMessageParameters.put("minId", firstComTaskExecutionId);
        rangeMessageParameters.put("maxId", lastComTaskExecutionId);
        rangeMessageParameters.put(EventConstants.EVENT_TOPIC, ComScheduleBackgroundObsoleteHandler.RANGE_OBSOLETE_TOPIC);
        when(this.message.getPayload()).thenReturn(RANGE_MESSAGE_PAYLOAD.getBytes());
        when(this.jsonService.deserialize(RANGE_MESSAGE_PAYLOAD.getBytes(), Map.class)).thenReturn(rangeMessageParameters);
    }

    @Test
    public void testWithoutComTaskExecutions() {
        this.createStartMessage();
        when(this.communicationTaskService.getScheduledComTaskExecutionIdRange(anyLong())).thenReturn(Optional.<ScheduledComTaskExecutionIdRange>empty());

        // Business method
        this.eventHandler.process(this.message);

        // Asserts
        verify(this.message).getPayload();
        verify(this.communicationTaskService).getScheduledComTaskExecutionIdRange(COM_SCHEDULE_ID);
        verifyZeroInteractions(this.eventService);
    }

    @Test
    public void testSmallRange() {
        this.createStartMessage();
        int firstComTaskExecutionId = 1;
        int lastComTaskExecutionId = 10;
        ScheduledComTaskExecutionIdRange idRange = new ScheduledComTaskExecutionIdRange(COM_SCHEDULE_ID, firstComTaskExecutionId, lastComTaskExecutionId);
        when(this.communicationTaskService.getScheduledComTaskExecutionIdRange(anyLong())).thenReturn(Optional.of(idRange));

        // Business method
        this.eventHandler.process(this.message);

        // Asserts
        verify(this.message).getPayload();
        verify(this.communicationTaskService).getScheduledComTaskExecutionIdRange(COM_SCHEDULE_ID);
        verify(this.eventService).postEvent(ComScheduleBackgroundObsoleteHandler.RANGE_OBSOLETE_TOPIC, new ScheduledComTaskExecutionIdRange(idRange.comScheduleId, firstComTaskExecutionId, lastComTaskExecutionId));
    }

    @Test
    public void testBigRange() {
        this.createStartMessage();
        int firstComTaskExecutionId = 1;
        int lastComTaskExecutionId = ComScheduleBackgroundObsoleteHandler.RECALCULATION_BATCH_SIZE * 2 + 10;
        ScheduledComTaskExecutionIdRange idRange = new ScheduledComTaskExecutionIdRange(COM_SCHEDULE_ID, firstComTaskExecutionId, lastComTaskExecutionId);
        when(this.communicationTaskService.getScheduledComTaskExecutionIdRange(anyLong())).thenReturn(Optional.of(idRange));

        // Business method
        this.eventHandler.process(this.message);

        // Asserts
        verify(this.message).getPayload();
        verify(this.communicationTaskService).getScheduledComTaskExecutionIdRange(COM_SCHEDULE_ID);
        InOrder inOrder = inOrder(this.eventService);
        inOrder.verify(this.eventService).postEvent(ComScheduleBackgroundObsoleteHandler.RANGE_OBSOLETE_TOPIC, new ScheduledComTaskExecutionIdRange(idRange.comScheduleId, 1, 1000));
        inOrder.verify(this.eventService).postEvent(ComScheduleBackgroundObsoleteHandler.RANGE_OBSOLETE_TOPIC, new ScheduledComTaskExecutionIdRange(idRange.comScheduleId, 1001, 2000));
        inOrder.verify(this.eventService).postEvent(ComScheduleBackgroundObsoleteHandler.RANGE_OBSOLETE_TOPIC, new ScheduledComTaskExecutionIdRange(idRange.comScheduleId, 2001, 2010));
    }

    @Test
    public void testMakeRangeObsolete() {
        int firstComTaskExecutionId = 97;
        int lastComTaskExecutionId = 953;
        this.createRangeMessage(firstComTaskExecutionId, lastComTaskExecutionId);
        ScheduledComTaskExecutionIdRange idRange = new ScheduledComTaskExecutionIdRange(COM_SCHEDULE_ID, firstComTaskExecutionId, lastComTaskExecutionId);

        // Business method
        this.eventHandler.process(this.message);

        // Asserts
        verify(this.message).getPayload();
        verify(this.communicationTaskService).obsoleteComTaskExecutionsInRange(idRange);
        verifyNoMoreInteractions(this.eventService);
    }

}