package com.energyict.protocolimpl.elster.ctr.packets.fields;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:48:15
 */
public class WakeUp extends AbstractPacketField {

    private final boolean wakeUpEnabled;

    public WakeUp(boolean wakeUpEnabled) {
        this.wakeUpEnabled = wakeUpEnabled;
    }

    public boolean isWakeUpEnabled() {
        return wakeUpEnabled;
    }

    public byte[] getBytes() {
        return wakeUpEnabled ? new byte[20] : new byte[0];
    }

}
