package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.elster.ctr.packets.PacketField;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:48:15
 */
public class WakeUp implements PacketField {

    private final boolean wakeUpEnabled;

    public WakeUp(boolean wakeUpEnabled) {
        this.wakeUpEnabled = wakeUpEnabled;
    }

    public WakeUp() {
        this(false);
    }

    public boolean isWakeUpEnabled() {
        return wakeUpEnabled;
    }

    public byte[] getBytes() {
        return wakeUpEnabled ? new byte[20] : new byte[0];
    }

    @Override
    public String toString() {
        return "WakeUp = " + wakeUpEnabled;
    }

}
