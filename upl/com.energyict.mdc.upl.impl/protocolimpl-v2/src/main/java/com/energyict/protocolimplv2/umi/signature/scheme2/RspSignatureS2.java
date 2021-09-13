package com.energyict.protocolimplv2.umi.signature.scheme2;

public class RspSignatureS2 extends AppPacketSignatureS2 {
    public static final short LENGTH = 74; // including padding

    public RspSignatureS2() {
        super(LENGTH);
    }

    public RspSignatureS2(byte[] digitalSignature, boolean none) {
        super(LENGTH, digitalSignature);
    }

    public RspSignatureS2(byte[] rawSignature) {
        super(rawSignature, LENGTH, false, false);
    }
}
