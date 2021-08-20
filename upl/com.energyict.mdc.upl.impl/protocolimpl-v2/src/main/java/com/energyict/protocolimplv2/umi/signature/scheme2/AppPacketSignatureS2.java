package com.energyict.protocolimplv2.umi.signature.scheme2;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.signature.AppPacketSignatureGeneric;
import com.energyict.protocolimplv2.umi.types.Role;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public abstract class AppPacketSignatureS2 extends AppPacketSignatureGeneric {
    public static final int MIN_DIGITAL_SIGNATURE_SIZE = 0x46;
    public static final int MAX_DIGITAL_SIGNATURE_SIZE = 0x48;
    private byte[] digitalSignature;
    private byte[] padding;

    protected AppPacketSignatureS2(int length) {
        this(length, Optional.empty(), Optional.empty(), Optional.empty());
    }

    protected AppPacketSignatureS2(int length, byte[] digitalSignature) {
        this(length, Optional.empty(), Optional.empty(), Optional.empty(), digitalSignature);
    }

    protected AppPacketSignatureS2(int length, Role role, byte[] digitalSignature) {
        this(length, Optional.of(role), Optional.empty(), Optional.empty(), digitalSignature);
    }

    protected AppPacketSignatureS2(int length, Role role, Date from, Date  until, byte[] digitalSignature) {
        this(length, Optional.of(role), Optional.of(from), Optional.of(until), digitalSignature);
    }

    protected AppPacketSignatureS2(int length, Role role, Date from, Date  until) {
        this(length, Optional.of(role), Optional.of(from), Optional.of(until));
    }

    protected AppPacketSignatureS2(int length, Optional<Role> role, Optional<Date> from, Optional<Date>  until,
                                   byte[] digitalSignature) {
        super(SecurityScheme.ASYMMETRIC, length, role, from, until);
        setDigitalSignature(digitalSignature);
    }

    protected AppPacketSignatureS2(int length, Optional<Role> role, Optional<Date> from, Optional<Date>  until) {
        super(SecurityScheme.ASYMMETRIC, length, role, from, until);
    }

    protected AppPacketSignatureS2(byte[] rawSignature, int length, boolean roleSet, boolean validPeriodSet) {
        super(rawSignature, length, roleSet, validPeriodSet);
        byte[] digSignatureWithPadding = new byte[getRawBuffer().remaining()];
        getRawBuffer().get(digSignatureWithPadding);

        int digSignatureLength = digSignatureWithPadding.length - 1;
        while (digSignatureLength >= 0) {
            if (digSignatureWithPadding[digSignatureLength] != 0x0) break;
            digSignatureLength--;
        }
        digSignatureLength++;
        digitalSignature = Arrays.copyOf(digSignatureWithPadding, digSignatureLength);
        padding = new byte[digSignatureWithPadding.length - digSignatureLength];
    }
    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(byte[] digitalSignature) {
        if (digitalSignature.length < MIN_DIGITAL_SIGNATURE_SIZE || digitalSignature.length > MAX_DIGITAL_SIGNATURE_SIZE) {
            throw new java.security.InvalidParameterException(
                    "Invalid digitalSignature size=" + digitalSignature.length + ", required size=[" +
                            MIN_DIGITAL_SIGNATURE_SIZE + "," + MAX_DIGITAL_SIGNATURE_SIZE + "]"
            );
        }
        this.digitalSignature = digitalSignature;
        getRawBuffer().position(getLength() - MAX_DIGITAL_SIGNATURE_SIZE);
        getRawBuffer().put(digitalSignature);
        padding = new byte[getRawBuffer().remaining()];
        getRawBuffer().put(padding);
    }

    @Override
    public byte[] getToBeSigned() {
        return Arrays.copyOfRange(getRaw(), 0, getLength() - MAX_DIGITAL_SIGNATURE_SIZE);
    }
}
