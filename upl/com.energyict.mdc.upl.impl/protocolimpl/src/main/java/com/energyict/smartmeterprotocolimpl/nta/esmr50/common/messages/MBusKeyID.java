package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages;

/**
 * Created by iulian on 9/28/2016.
 */
@Deprecated
public enum MBusKeyID {
    P2(0, "P2 Key"),
    FUAK(1, "FUAK");


    private final String name;
    private final byte id;

    MBusKeyID(int i, String name) {
        this.id = (byte) i;
        this.name = name;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
