package com.elster.jupiter.issue.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.elster.jupiter.issue.datacollection.DataCollectionEvent;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.transaction.TransactionContext;

public class DataCollectionEventTest extends BaseTest {

    @Test
    public void testInit() {
        String topic = "com/energyict/mdc/inboundcommunication/UNKNOWNDEVICE";

        AmrSystem amrSystem = getMeteringService().findAmrSystem(ModuleConstants.MDC_AMR_SYSTEM_ID).get();
        
        try (TransactionContext context = getContext()) {
            amrSystem.newMeter("test device").save();
            context.commit();
        }

        Map<String, String> rawEvent = new HashMap<>();
        rawEvent.put("event.topics", topic);
        rawEvent.put("deviceIdentifier", "test device");

        DataCollectionEvent event = new DataCollectionEvent(getIssueService(), getMeteringService(), rawEvent);

        assertThat(event.getEventType()).isEqualTo(topic);
        assertThat(event.getDevice().getAmrId()).isEqualTo("test device");
    }
}
