package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.packet.HeaderPayloadData;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.*;

public class LinkFrameHeaderDataTest {
    static public LinkFrameHeaderData createTestFrameHeader(IData payload) {
        LinkFrameHeaderData headerData = new LinkFrameHeaderData.Builder()
                .setSequence((byte)0)
                .setVersion((byte)1)
                .setBusy((byte)0)
                .setReserved((byte)0)
                .setFrameType(LinkFrameType.SIMPLE)
                .setFramePayloadLength(payload.getLength())
                .setDestination((byte)1)
                .setSource((byte)2)
                .build();

        return headerData;
    }

    @Test
    public void createHeaderPayloadAndCheckFields() {
        byte sequenceVersion = 0x01;
        byte busyReservedFrametype = 0x00;
        byte payloadLength = 123;
        byte destinationSource = 0x12;

        LinkFrameHeaderData data = createTestFrameHeader(new LittleEndianData(new byte[payloadLength]));
        assertEquals(sequenceVersion, data.getSequenceVersion());
        assertEquals(busyReservedFrametype, data.getBusyReservedFrametype());
        assertEquals(payloadLength, data.getFramePayloadLength());
        assertEquals(destinationSource, data.getDestinationSource());
    }

    @Test
    public void recreateFrameHeaderFromRawData() {
        byte payloadLength = 123;
        LinkFrameHeaderData data = createTestFrameHeader(new LittleEndianData(new byte[payloadLength]));
        LinkFrameHeaderData dataFromRaw = new LinkFrameHeaderData(data.getRaw());
        assertEquals(data, dataFromRaw);
        assertArrayEquals(data.getRaw(), dataFromRaw.getRaw());

    }

    @Test
    public void testGetters() {
        byte sequence = 0x01;
        byte version = 0x01;
        byte busy = 0x01;
        byte reserved = 0x00;
        LinkFrameType frameType = LinkFrameType.RESYNC;
        short payloadLength = 123;
        byte destination = 0x01;
        byte source = 0x02;

        LinkFrameHeaderData headerData = new LinkFrameHeaderData.Builder()
                .setSequence(sequence)
                .setVersion(version)
                .setBusy(busy)
                .setReserved(reserved)
                .setFrameType(frameType)
                .setFramePayloadLength(new LittleEndianData(new byte[payloadLength]).getLength())
                .setDestination(destination)
                .setSource(source)
                .build();

        assertEquals(sequence, headerData.getSequence());
        assertEquals(version, headerData.getVersion());
        assertTrue(headerData.isBusy());
        assertEquals(LinkFrameType.RESYNC, headerData.getLinkFrameType());
        assertEquals(payloadLength, headerData.getFramePayloadLength());
        assertEquals(destination, headerData.getDestination());
        assertEquals(source, headerData.getSource());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new LinkFrameHeaderData(new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromBigRawData() {
        new LinkFrameHeaderData(new byte[HeaderPayloadData.SIZE + 1]);
    }
}
