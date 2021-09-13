package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class EvtSubscribeCmdPayloadTest {
    @Test
    public void createSubscribeCmdPayload() {
        int events = 128;
        SecurityScheme securityScheme = SecurityScheme.ASYMMETRIC;
        EvtSubscribeCmdPayload payload = new EvtSubscribeCmdPayload(events, securityScheme);
        assertEquals(events, payload.getEvents());
        assertEquals(securityScheme, payload.getPublishSecScheme());
        assertEquals(EvtSubscribeCmdPayload.SIZE, payload.getLength());
    }

    @Test
    public void recreateSubscribeCmdPayloadFromRaw() {
        int events = 128;
        SecurityScheme securityScheme = SecurityScheme.ASYMMETRIC;
        EvtSubscribeCmdPayload payload = new EvtSubscribeCmdPayload(events, securityScheme);
        EvtSubscribeCmdPayload payload1 = new EvtSubscribeCmdPayload(payload.getRaw());
        assertEquals(payload.getEvents(), payload1.getEvents());
        assertEquals(payload.getPublishSecScheme(), payload1.getPublishSecScheme());
        assertEquals(EvtSubscribeCmdPayload.SIZE, payload1.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        EvtSubscribeCmdPayload payload = new EvtSubscribeCmdPayload(new byte[0]);
    }
} 
