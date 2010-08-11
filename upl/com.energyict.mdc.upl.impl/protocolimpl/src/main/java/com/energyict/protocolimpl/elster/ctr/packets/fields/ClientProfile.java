package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.elster.ctr.packets.PacketField;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:40:38
 */
public class ClientProfile implements PacketField {

    private final boolean longFrame;
    private final int profile;
    private final int operatorId;

    public ClientProfile(int profile, int operatorId, boolean longFrame) {
        this.operatorId = operatorId;
        this.longFrame = longFrame;
        this.profile = profile;
    }

    public ClientProfile(int profile, int operatorId) {
        this(profile, operatorId, false);
    }

    public ClientProfile(int rawProfileValue) {
        this(rawProfileValue & 0x07, (rawProfileValue >> 3) & 0x07, (rawProfileValue & 0x080) != 0);
    }

    public ClientProfile() {
        this(0, 0, false);
    }

    public byte[] getBytes() {
        byte[] value = new byte[1];
        value[0] |= longFrame ? 0x80 : 0x00;
        value[0] |= (profile & 0x07);
        value[0] |= ((operatorId << 3) & 0x38);
        return value;
    }

}
