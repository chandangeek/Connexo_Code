package com.energyict.protocolimplv2.umi.packet.payload;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmptyPacketPayloadTest {
    @Test
    public void createEmptyPacketPayload() {
        EmptyPacketPayload packetPayload = new EmptyPacketPayload();
        assertEquals(0, packetPayload.getLength());
        assertEquals(0, packetPayload.getRaw().length);
    }

} 
