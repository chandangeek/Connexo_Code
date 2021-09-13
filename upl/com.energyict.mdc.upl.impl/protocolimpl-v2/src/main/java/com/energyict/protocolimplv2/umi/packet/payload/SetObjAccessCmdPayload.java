package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.Role;
import com.energyict.protocolimplv2.umi.types.UmiCode;

import java.util.ArrayList;
import java.util.List;

public class SetObjAccessCmdPayload extends ReadObjCmdPayload {
    public static final int SIZE = 6;

    private final List<Role> roles;

    /**
     * A 16-bit bitfield giving the roles allowed to read the object value.
     * Bit 0 represents Role 0, etc
     */
    private int rolesBitfield;

    public SetObjAccessCmdPayload(UmiCode umiCode, List<Role> roles) {
        super(umiCode, SIZE);
        this.roles = roles;

        for (Role role : roles) {
            rolesBitfield |= (1 << role.getId());
        }
        getRawBuffer().putShort((short) rolesBitfield);
    }

    public SetObjAccessCmdPayload(byte[] rawPayload) {
        super(rawPayload, SIZE, false);
        getRawBuffer().position(UmiCode.SIZE);
        rolesBitfield = Short.toUnsignedInt(getRawBuffer().getShort());

        roles = new ArrayList<>();
        for (Role role : Role.roles) {
            int value = 1 << role.getId();
            if ((rolesBitfield & value) == value) {
                roles.add(role);
            }
        }
    }

    public List<Role> getRoles() {
        return roles;
    }

    public int getRolesBitfield() {
        return rolesBitfield;
    }
}
