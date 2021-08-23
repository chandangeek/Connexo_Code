package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class LinkLayerFrameTest {
    @Test
    public void testCrc() {
        // packet header [01 04 00 00] should result in crc [fe 5b] (little-endian)
        IData payload = new LittleEndianData(new byte[0]);

        LinkFrameHeaderData headerData = new LinkFrameHeaderData.Builder()
                .setSequence((byte)0)
                .setVersion((byte)1)
                .setBusy((byte)0)
                .setReserved((byte)0)
                .setFrameType(LinkFrameType.RESYNC)
                .setFramePayloadLength(payload.getLength())
                .setDestination((byte)0)
                .setSource((byte)0)
                .build();

        LinkLayerFrame linkLayerFrame = new LinkLayerFrame.Builder()
                .setLinkFrameHeaderData(headerData)
                .setPayload(payload)
                .build();

        int c1 = 0xfe;
        int c2 = 0x5b;
        int c = 23550;

        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)c);

        ByteBuffer buffer2 = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        buffer2.put((byte)c1).put((byte)c2);
        assertArrayEquals(buffer.array(), buffer2.array());

        assertArrayEquals(buffer.array(), linkLayerFrame.getCrc().getRaw());

    }
}
