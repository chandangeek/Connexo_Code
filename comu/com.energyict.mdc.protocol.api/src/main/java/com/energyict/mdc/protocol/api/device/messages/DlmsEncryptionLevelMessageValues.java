/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.messages;

import java.util.ArrayList;
import java.util.List;

public enum DlmsEncryptionLevelMessageValues {

    NO_ENCRYPTION("No encryption", 0),
    DATA_AUTHENTICATION("Data authentication", 1),
    DATA_ENCRYPTION("Data encryption", 2),
    DATA_AUTHENTICATION_ENCRYPTION("Data authentication and encryption", 3);

    private final String name;
    private final int value;

    private DlmsEncryptionLevelMessageValues(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static String[] getNames() {
        List<String> names = new ArrayList<>();
        for (DlmsEncryptionLevelMessageValues dlmsEncryptionLevelMessageValues : values()) {
            names.add(dlmsEncryptionLevelMessageValues.name);
        }
        return names.toArray(new String[names.size()]);
    }

    public static int getValueFor(final String name) {
        for (DlmsEncryptionLevelMessageValues dlmsEncryptionLevelMessageValues : values()) {
            if (dlmsEncryptionLevelMessageValues.name.equals(name)) {
                return dlmsEncryptionLevelMessageValues.getValue();
            }
        }
        return -1;
    }
}
