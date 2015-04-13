package com.energyict.dlms.cosem.generalblocktransfer;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author sva
 * @since 27/03/2015 - 10:44
 */
public class GeneralBlockTransferFrameTest {

    @Test
    public void testParseFrame() throws Exception {
        byte[] rawData = ProtocolTools.getBytesFromHexString("e0050001000082000Ac4024100000000010082", "");

        // Business methods
        GeneralBlockTransferFrame frame = new GeneralBlockTransferFrame();
        int offset = frame.parseFrame(rawData, 0);

        // Asserts
        assertFalse(frame.getBlockControl().isLastBlock());
        assertFalse(frame.getBlockControl().isStreamingMode());
        assertEquals(5, frame.getBlockControl().getWindowSize());
        assertEquals(1, frame.getBlockNumber());
        assertEquals(0, frame.getAcknowledgedBlockNumber());
        assertEquals(10, frame.getLengthOfBlockData());
        assertEquals(10, frame.getBlockData().length);
        assertEquals(19, offset);
    }

    @Test(expected = ProtocolException.class)
    public void testParseInvalidFrame_InvalidTag() throws Exception {
        byte[] rawData = ProtocolTools.concatByteArrays(
                new byte[]{DLMSCOSEMGlobals.COSEM_GETRESPONSE},
                ProtocolTools.getBytesFromHexString("00050001000082000Ac4024100000000010082", "")
        );

        // Business methods
        GeneralBlockTransferFrame frame = new GeneralBlockTransferFrame();
        frame.parseFrame(rawData, 0);
    }

    @Test(expected = ProtocolException.class)
    public void testParseInvalidFrame_WrongContentLength() throws Exception {
        byte[] rawData = ProtocolTools.getBytesFromHexString("e00500010000820005c4", "");

        // Business methods
        GeneralBlockTransferFrame frame = new GeneralBlockTransferFrame();
        frame.parseFrame(rawData, 0);
    }
}