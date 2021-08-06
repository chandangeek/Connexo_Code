package com.energyict.protocolimplv2.umi.signature.scheme2;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.Role;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ImageSignatureS2Test {
    @Test
    public void createSignatureAndCheckFields() {
        Role role = Role.PERIPHERAL;
        byte[] msgAuthCode = new byte[ImageSignatureS2.MIN_DIGITAL_SIGNATURE_SIZE];
        new Random().nextBytes(msgAuthCode);

        ImageSignatureS2 imageSignature = new ImageSignatureS2(role, msgAuthCode);
        assertEquals(role, imageSignature.getRole());
        assertArrayEquals(msgAuthCode, imageSignature.getDigitalSignature());
        assertEquals(ImageSignatureS2.LENGTH, imageSignature.getLength());
        assertEquals(SecurityScheme.ASYMMETRIC, imageSignature.getScheme());
    }

    @Test
    public void recreateSignatureFromRawData() {
        Role role = Role.GUEST;
        byte[] msgAuthCode = new byte[ImageSignatureS2.MAX_DIGITAL_SIGNATURE_SIZE];
        new Random().nextBytes(msgAuthCode);

        ImageSignatureS2 imageSignature = new ImageSignatureS2(new ImageSignatureS2(role, msgAuthCode).getRaw());
        assertEquals(role, imageSignature.getRole());
        assertArrayEquals(msgAuthCode, imageSignature.getDigitalSignature());
        assertEquals(ImageSignatureS2.LENGTH, imageSignature.getLength());
        assertEquals(SecurityScheme.ASYMMETRIC, imageSignature.getScheme());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidMsgAuthCode() {
        new ImageSignatureS2(Role.GUEST, new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnBigMsgAuthCode() {
        new ImageSignatureS2(Role.GUEST, new byte[ImageSignatureS2.MAX_DIGITAL_SIGNATURE_SIZE + 1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new ImageSignatureS2(new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromBigRawData() {
        new ImageSignatureS2(new byte[100]);
    }
}
