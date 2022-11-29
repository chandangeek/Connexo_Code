/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.coap.crest;

public enum Medium {

    UNDEFINED((byte) 0x07),
    COLD_WATER((byte) 0x16),
    HOT_WATER((byte) 0x06);

    final byte id;

    Medium(byte id) {
        this.id = id;
    }

    public static Medium forId(byte id) {
        for (Medium medium : Medium.values()) {
            if (medium.id == id) {
                return medium;
            }
        }
        return null;
    }
}
