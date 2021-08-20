package com.energyict.protocolimplv2.umi.signature.scheme2;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.Role;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CmdSignatureS2Test {
    @Test
    public void createSignatureAndCheckFields() {
        Role role = Role.PERIPHERAL;
        Date from = new Date(123);
        Date until = new Date(1234);
        byte[] digitalSignature = new byte[CmdSignatureS2.MIN_DIGITAL_SIGNATURE_SIZE];
        new Random().nextBytes(digitalSignature);

        CmdSignatureS2 cmdSignature = new CmdSignatureS2(role, from, until, digitalSignature);
        assertEquals(role, cmdSignature.getRole());
        assertEquals(from, cmdSignature.getValidFrom());
        assertEquals(until, cmdSignature.getValidUntil());
        assertArrayEquals(digitalSignature, cmdSignature.getDigitalSignature());
        assertEquals(CmdSignatureS2.LENGTH, cmdSignature.getLength());
        assertEquals(SecurityScheme.ASYMMETRIC, cmdSignature.getScheme());
    }

    @Test
    public void recreateSignatureFromRawData() {
        Role role = Role.GUEST;
        Date from = new Date(123);
        Date until = new Date(1234);
        byte[] digitalSignature = new byte[CmdSignatureS2.MAX_DIGITAL_SIGNATURE_SIZE];
        new Random().nextBytes(digitalSignature);

        CmdSignatureS2 cmdSignature = new CmdSignatureS2(role, from, until, digitalSignature);
        CmdSignatureS2 cmdSignature2 = new CmdSignatureS2(cmdSignature.getRaw());
        assertEquals(role, cmdSignature2.getRole());
        assertEquals(from.toString(), cmdSignature2.getValidFrom().toString());
        assertEquals(until.toString(), cmdSignature2.getValidUntil().toString());
        assertArrayEquals(digitalSignature, cmdSignature2.getDigitalSignature());
        assertEquals(CmdSignatureS2.LENGTH, cmdSignature2.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidMsgAuthCode() {
        new CmdSignatureS2(Role.GUEST, new Date(), new Date(), new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnBigMsgAuthCode() {
        new CmdSignatureS2(Role.GUEST, new Date(), new Date(), new byte[CmdSignatureS2.MAX_DIGITAL_SIGNATURE_SIZE + 1]);
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
