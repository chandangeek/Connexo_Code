package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

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


    private static final byte[] identificationResponse;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("0A000000293000123456789000007819090044264420456C657474726F20494D");
        sb.append("504C52494D504C52302E303530424242523130010003E82D2D2D2D2D2D2D2D2D");
        sb.append("2D2D2D2D2D2D2D2D2D2D2D001500000A013E963DB7026DF84800000000000000");
        sb.append("0000009F7F000000000000000000000000000000000000000000000000000000");
        sb.append("000000000000006A71026F8E450D");
        identificationResponse = ProtocolTools.getBytesFromHexString(sb.toString(), "");
    }

    @Test
    public void testGetBytes() throws Exception {
        assertArrayEquals(correctFrame, new GPRSFrame().parse(correctFrame, 0).getBytes());
    }

    @Test
    public void testParse() throws Exception {
        assertArrayEquals(correctFrame, new GPRSFrame().parse(correctFrame, 0).getBytes());
        assertArrayEquals(identificationResponse, new GPRSFrame().parse(identificationResponse, 0).getBytes());
    }
}
