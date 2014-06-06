package com.elster.jupiter.issue.tests;

import com.elster.jupiter.issue.datacollection.impl.event.DataCollectionEventHandler;
import com.elster.jupiter.messaging.Message;
import org.junit.Test;
import org.osgi.service.event.EventConstants;

import javax.print.attribute.standard.JobKOctetsProcessed;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DataCollectionEventHandlerTest extends BaseTest {
    @Test
    public void testSuccessfullProcess() {
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE");
        String serializedMap = getJsonService().serialize(messageMap);
        Message message = getMockMessage(serializedMap);
        Boolean isProcessed = false;
        DataCollectionEventHandler handler = new DataCollectionEventHandler(getJsonService(), getIssueService(), getMockIssueCreationService(), getMeteringService());
        try {
            handler.process(message);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("processed!");
            isProcessed = true;
        }
        assertThat(isProcessed).isTrue();
    }

    @Test
    public void testUnsuccessfullProcess() {
        Map messageMap = new HashMap<>();
        messageMap.put(EventConstants.EVENT_TOPIC, "some/fake/topic");
        String serializedMap = getJsonService().serialize(messageMap);
        Message message = getMockMessage(serializedMap);
        Boolean isProcessed = false;
        DataCollectionEventHandler handler = new DataCollectionEventHandler(getJsonService(), getIssueService(), getMockIssueCreationService(), getMeteringService());
        try {
            handler.process(message);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("processed!");
            isProcessed = true;
        }
        assertThat(isProcessed).isFalse();
    }
}
