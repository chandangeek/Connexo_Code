package com.energyict.protocolimplv2.umi.signature;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.signature.scheme2.CmdSignatureS2;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AppPacketSignatureS0Test {
    @Test
    public void createSignatureAndCheckFields() {
        AppPacketSignatureS0 signature = new AppPacketSignatureS0();
        assertEquals(signature.getLength(), AppPacketSignatureS0.LENGTH);
        assertEquals(signature.getScheme(), SecurityScheme.NO_SECURITY);
    }

    @Test
    public void recreateSignatureFromRawData() {
        AppPacketSignatureS0 signature = new AppPacketSignatureS0();
        AppPacketSignatureS0 signature2 = new AppPacketSignatureS0(signature.getRaw());
        assertEquals(signature2.getLength(), AppPacketSignatureS0.LENGTH);
        assertEquals(signature2.getScheme(), SecurityScheme.NO_SECURITY);
        assertArrayEquals(signature.getRaw(), signature2.getRaw());

    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new CmdSignatureS2(new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromBigRawData() {
        new CmdSignatureS2(new byte[100]);
    }
}
