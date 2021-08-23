package com.energyict.protocolimplv2.umi.signature.scheme2;

import com.energyict.protocolimplv2.umi.types.Role;

import java.util.Date;

public class CmdSignatureS2 extends AppPacketSignatureS2 {
    public static final short LENGTH = 83; // including padding

    public CmdSignatureS2(Role role, Date from, Date until, byte[] digitalSignature) {
        super(LENGTH, role, from, until, digitalSignature);
    }

    public CmdSignatureS2(Role role, Date from, Date until) {
        super(LENGTH, role, from, until);
    }

    public CmdSignatureS2(byte[] rawSignature) {
        super(rawSignature, LENGTH, true, true);
    }

    public Role getRole() {
        return super.getOptionalRole().get();
    }

    public Date getValidFrom() {
        return super.getOptionalValidFrom().get();
    }

    public Date getValidUntil() {
        return super.getOptionalValidUntil().get();
    }
}
