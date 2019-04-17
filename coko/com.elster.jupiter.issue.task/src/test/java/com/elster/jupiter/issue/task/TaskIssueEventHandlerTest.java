/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.impl.service.IssueCreationServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.issue.task.impl.event.TaskIssueEventHandlerFactory;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;

import org.osgi.service.event.EventConstants;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TaskIssueEventHandlerTest extends BaseTest {

    @Test
    public void testUnmappedEvent() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/elster/jupiter/tasks/unknown/EVENT");
        messageMap.put(EventConstants.TIMESTAMP, Instant.now().toEpochMilli());
        messageMap.put(ModuleConstants.TASKOCCURRENCE_ID, "1");
        Message message = getMockMessage(getJsonService().serialize(messageMap));
        MessageHandler handler = getTaskIssueEventHandler(getMockIssueCreationService());

        try {
            handler.process(message);
        } catch (DispatchCreationEventException ex) {
            fail("This event shouldn't be processed");
        }
    }

    private MessageHandler getTaskIssueEventHandler(IssueCreationService issueCreationService) {
        IssueService issueService = mockIssueService(issueCreationService);
        MeteringService meteringService = mockMeteringService();
        TaskIssueEventHandlerFactory handlerFactory = getInjector().getInstance(TaskIssueEventHandlerFactory.class);
        handlerFactory.setIssueService(issueService);
        handlerFactory.setMeteringService(meteringService);
        return handlerFactory.newMessageHandler();
    }

    private IssueService mockIssueService(IssueCreationService issueCreationService) {
        IssueService issueService = mock(IssueService.class);
        when(issueService.getIssueCreationService()).thenReturn(issueCreationService);
        IssueStatus status = mock(IssueStatus.class);
        when(issueService.findStatus(IssueStatus.OPEN)).thenReturn(Optional.of(status));
        return issueService;
    }

    private MeteringService mockMeteringService() {
        MeteringService meteringService = mock(MeteringService.class);
        AmrSystem amrSystem = mock(AmrSystem.class);
        Meter meter = mock(Meter.class);
        State state = mock(State.class);
        Stage stage = mock(Stage.class);
        when(stage.getName()).thenReturn(EndDeviceStage.OPERATIONAL.getKey());
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(meter.getState()).thenReturn(Optional.of(state));

        when(meteringService.findAmrSystem(1)).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(Matchers.anyString())).thenReturn(Optional.of(meter));
        return meteringService;

    }


    protected class CheckEventTypeServiceMock extends IssueCreationServiceImpl {
        private List<Class<? extends IssueEvent>> expectedClasses;
        private int size = 0;

        @SafeVarargs
        public CheckEventTypeServiceMock(Class<? extends IssueEvent>... expectedClasses) {
            this.expectedClasses = new ArrayList<>(Arrays.asList(expectedClasses));
        }

        @Override
        public void dispatchCreationEvent(List<IssueEvent> events) {
            for (IssueEvent event : events) {
                if (!expectedClasses.contains(event.getClass())) {
                    size++;
                }
                ;
            }
        }

        public boolean isSuccessfull() {
            return size == 0;
        }
    }

    protected class CheckEventCountServiceMock extends IssueCreationServiceImpl {
        private int counter = 0;

        @Override
        public void dispatchCreationEvent(List<IssueEvent> events) {
            counter = events.size();
        }

        public int getCounter() {
            return counter;
        }
    }
}
