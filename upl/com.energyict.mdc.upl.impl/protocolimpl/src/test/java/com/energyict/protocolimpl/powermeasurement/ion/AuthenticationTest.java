package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class AuthenticationTest {

    private static final byte[] ALPHA_NUMERIC_AUTH = ProtocolTools.getBytesFromHexString("80C8003033706163", "");
    private static final byte[] HEX_AUTH = ProtocolTools.getBytesFromHexString("8003000000706163", "");
    private static final byte[] NUMERICAL_AUTH = ProtocolTools.getBytesFromHexString("00c800000001e240", "");

    @Test
    public void testAlphaNumericAuthentication() throws Exception {
        Authentication auth = new Authentication("03pac", "200");
        assertArrayEquals(ALPHA_NUMERIC_AUTH, auth.toByteArray().getBytes());;
    }

    @Test
    public void testHexAuthentication() throws Exception {
        Authentication auth = new Authentication("HEX0000706163", "3");
        assertArrayEquals(HEX_AUTH, auth.toByteArray().getBytes());;
    }

    @Test
    public void testNumericalAuthentication() throws Exception {
        Authentication auth = new Authentication("123456", "200");
        assertArrayEquals(NUMERICAL_AUTH, auth.toByteArray().getBytes());;
    }
}
