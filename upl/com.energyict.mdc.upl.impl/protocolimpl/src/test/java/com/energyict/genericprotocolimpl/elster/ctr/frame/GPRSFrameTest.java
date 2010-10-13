package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 11:50:24
 */
public class GPRSFrameTest {

    private static final byte[] correctFrame;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("0A000000004A323000B300123456789000000000090044264420456C65747472");
        sb.append("6F20494D504C52494D504C52302E303234414141523130010003E855E0749C41");
        sb.append("78787820202020202020202020202020202020043A0000000000000000000000");
        sb.append("0000000000000000000000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000001451C0D");
        correctFrame = ProtocolTools.getBytesFromHexString(sb.toString(), "");
    }

    private static final byte[] nackFrame;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("0A0000002D00004C680000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000418B0D");
        nackFrame = ProtocolTools.getBytesFromHexString(sb.toString(), "");
    }

    @Test
    public void testGetBytes() throws Exception {
        assertArrayEquals(correctFrame, new GPRSFrame().parse(correctFrame, 0).getBytes());
        assertArrayEquals(nackFrame, new GPRSFrame().parse(nackFrame, 0).getBytes());
    }

    @Test
    public void testParse() throws Exception {
        assertArrayEquals(correctFrame, new GPRSFrame().parse(correctFrame, 0).getBytes());
        assertArrayEquals(nackFrame, new GPRSFrame().parse(nackFrame, 0).getBytes());
    }


    @Test
    public void testNackCpa() throws Exception {
        GPRSFrame frame = new GPRSFrame().parse(nackFrame, 0);
        assertTrue(frame.validCpa(new MTU155Properties().getKeyCBytes()));
    }

    @Test
    public void testCHeckCRC() throws Exception {
        assertTrue(new GPRSFrame().parse(nackFrame, 0).isValidCrc());
    }
}
