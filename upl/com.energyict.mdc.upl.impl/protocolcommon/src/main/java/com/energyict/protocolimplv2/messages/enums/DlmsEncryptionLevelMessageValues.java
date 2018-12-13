package com.energyict.protocolimplv2.messages.enums;

import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 19/03/13
 * Time: 8:37
 */
public enum DlmsEncryptionLevelMessageValues {

    NO_ENCRYPTION("No encryption", 0),
    DATA_AUTHENTICATION("Data authentication", 1),
    DATA_ENCRYPTION("Data encryption", 2),
    DATA_AUTHENTICATION_ENCRYPTION("Data authentication and encryption", 3);

    private final String name;
    private final int value;

    DlmsEncryptionLevelMessageValues(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static String[] getNames() {
        return Stream
                    .of(values())
                    .map(each -> each.name)
                    .toArray(String[]::new);
    }

    public static int getValueFor(final String name) {
        return Stream
                    .of(values())
                    .filter(each -> each.name.equals(name))
                    .map(DlmsEncryptionLevelMessageValues::getValue)
                    .findFirst()
                    .orElse(-1);
    }

}