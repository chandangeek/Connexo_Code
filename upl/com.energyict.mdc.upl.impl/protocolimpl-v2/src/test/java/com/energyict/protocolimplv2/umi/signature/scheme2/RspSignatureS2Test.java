package com.energyict.protocolimplv2.umi.signature.scheme2;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RspSignatureS2Test {
    @Test
    public void createSignatureAndCheckFields() {
        byte[] msgAuthCode = new byte[RspSignatureS2.MIN_DIGITAL_SIGNATURE_SIZE];
        new Random().nextBytes(msgAuthCode);

        RspSignatureS2 rspSignature = new RspSignatureS2(msgAuthCode, false);
        assertArrayEquals(msgAuthCode, rspSignature.getDigitalSignature());
        assertEquals(rspSignature.LENGTH, rspSignature.getLength());
        assertEquals(SecurityScheme.ASYMMETRIC, rspSignature.getScheme());
    }

    @Test
    public void recreateSignatureFromRawData() {
        byte[] msgAuthCode = new byte[RspSignatureS2.MAX_DIGITAL_SIGNATURE_SIZE];
        new Random().nextBytes(msgAuthCode);

        RspSignatureS2 rspSignature = new RspSignatureS2(new RspSignatureS2(msgAuthCode, false).getRaw());
        assertArrayEquals(msgAuthCode, rspSignature.getDigitalSignature());
        assertEquals(RspSignatureS2.LENGTH, rspSignature.getLength());
        assertEquals(SecurityScheme.ASYMMETRIC, rspSignature.getScheme());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidMsgAuthCode() {
        new RspSignatureS2(new byte[1], false);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnBigMsgAuthCode() {
        new RspSignatureS2(new byte[RspSignatureS2.MAX_DIGITAL_SIGNATURE_SIZE + 1], false);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new RspSignatureS2(new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromBigRawData() {
        new RspSignatureS2(new byte[100]);
    }
}
