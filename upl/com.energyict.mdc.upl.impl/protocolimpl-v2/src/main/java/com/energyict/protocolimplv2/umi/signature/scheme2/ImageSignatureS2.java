package com.energyict.protocolimplv2.umi.signature.scheme2;

import com.energyict.protocolimplv2.umi.types.Role;

public class ImageSignatureS2 extends AppPacketSignatureS2 {
    public static final short LENGTH = 75; // including padding

    public ImageSignatureS2(Role role, byte[] digitalSignature) {
        super(LENGTH, role, digitalSignature);
    }

    public ImageSignatureS2(byte[] rawSignature) {
        super(rawSignature, LENGTH, true, false);
    }

    public Role getRole() {
        return super.getOptionalRole().get();
    }
}
