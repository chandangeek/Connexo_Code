package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 11-okt-2010
 * Time: 16:24:06
 */
public class GprsConnectionTest {

    private static final byte[] NACK_FRAME;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("0A0000002D00004C680000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000000000000000000000000000000000000000000000");
        sb.append("0000000000000000000000418B0D");
        NACK_FRAME = ProtocolTools.getBytesFromHexString(sb.toString(), "");
    }


    @Test(timeout = 500)
    public void testSendFrameGetResponse() throws Exception {
        byte[] meterData = ProtocolTools.concatByteArrays(new byte[15], NACK_FRAME, new byte[5]);
        ByteArrayInputStream in = new ByteArrayInputStream(meterData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        GPRSFrame request = new GPRSFrame();
        GprsConnection connection = new GprsConnection(in, out, new MTU155Properties());
        GPRSFrame response = connection.sendFrameGetResponse(request);
        assertArrayEquals(NACK_FRAME, response.getBytes());

    }

    @Test(expected = CTRConnectionException.class, timeout = 1000)
    public void testTimeOut() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        properties.addProperty(MTU155Properties.TIMEOUT, "100");
        properties.addProperty(MTU155Properties.DELAY_AFTER_ERROR, "0");
        properties.addProperty(MTU155Properties.FORCED_DELAY, "0");
        properties.addProperty(MTU155Properties.RETRIES, "0");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        GprsConnection connection = new GprsConnection(in, out, properties);
        connection.sendFrameGetResponse(new GPRSFrame());
    }

    @Test(timeout = 3000)
    public void testRetries() throws Exception {
        MTU155Properties properties = new MTU155Properties();
        properties.addProperty(MTU155Properties.TIMEOUT, "1");
        properties.addProperty(MTU155Properties.DELAY_AFTER_ERROR, "0");
        properties.addProperty(MTU155Properties.FORCED_DELAY, "0");
        properties.addProperty(MTU155Properties.RETRIES, "3");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        GprsConnection connection = new GprsConnection(in, out, properties);
        GPRSFrame request = new GPRSFrame();
        try {
            connection.sendFrameGetResponse(request);
        } catch (CTRConnectionException e) {
            assertEquals(CTRConnectionException.class, e.getClass());
        }

        byte[] expected = new byte[0];
        for (int i = 0; i < (properties.getRetries() + 1); i++) {
            expected = ProtocolTools.concatByteArrays(expected, request.getBytes());
        }

        assertArrayEquals(expected, out.toByteArray());
    }

}
