package com.energyict.protocolimplv2.umi.signature;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;

public class AppPacketSignatureS0 extends LittleEndianData {
    private static final SecurityScheme[] securitySchemes = SecurityScheme.values();
    public static final short LENGTH = 2;

    private final SecurityScheme scheme;
    private final short          length; // 1 byte

    public AppPacketSignatureS0() {
        this(SecurityScheme.NO_SECURITY, LENGTH);
    }

    public AppPacketSignatureS0(byte[] rawSignature) {
        this(rawSignature, LENGTH);
    }

    protected AppPacketSignatureS0(byte[] rawSignature, int length) {
        super(rawSignature, length, false);
        this.scheme = SecurityScheme.fromId(getRawBuffer().get());
        this.length = (short)Byte.toUnsignedInt(getRawBuffer().get());
    }

    protected AppPacketSignatureS0(SecurityScheme scheme, int length) {
        super(length);
        this.scheme = scheme;
        this.length = (short) length;
        getRawBuffer().put((byte) scheme.getId()).put((byte) length);
    }

    public SecurityScheme getScheme() {
        return scheme;
    }

    public int getLength() {
        return length;
    }
}
