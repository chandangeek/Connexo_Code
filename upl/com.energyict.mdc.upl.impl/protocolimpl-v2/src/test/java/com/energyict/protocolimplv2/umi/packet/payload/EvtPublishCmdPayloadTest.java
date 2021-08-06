package com.energyict.protocolimplv2.umi.packet.payload;

import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class EvtPublishCmdPayloadTest {
    @Test
    public void createPublishCmdPayload() {
        int events = 10;
        EvtPublishCmdPayload payload = new EvtPublishCmdPayload(events);
        assertEquals(events, payload.getEvents());
        assertEquals(EvtPublishCmdPayload.SIZE, payload.getLength());
    }

    @Test
    public void recreatePublishCmdPayloadFromRaw() {
        int events = 10;
        EvtPublishCmdPayload payload = new EvtPublishCmdPayload(events);
        EvtPublishCmdPayload payload1 = new EvtPublishCmdPayload(payload.getRaw());
        assertEquals(events, payload1.getEvents());
        assertEquals(EvtPublishCmdPayload.SIZE, payload1.getLength());
        assertEquals(payload.getDatetime(), payload1.getDatetime());
        assertEquals(payload.getMilliseconds(), payload1.getMilliseconds());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        EvtPublishCmdPayload payload = new EvtPublishCmdPayload(new byte[0]);
    }
} 
