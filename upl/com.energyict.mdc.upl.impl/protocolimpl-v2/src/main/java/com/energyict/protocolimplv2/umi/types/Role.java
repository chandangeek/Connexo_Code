package com.energyict.protocolimplv2.umi.types;


import com.energyict.protocolimplv2.umi.util.Limits;

import java.security.InvalidParameterException;

public enum Role {
    GUEST(0),
    HOST(1),
    PERIPHERAL(2),
    METERING_DEVICE(3),
    USER(4),
    SERVICE(5),
    MANUFACTURER(6),
    VERIFICATION(7),
    SOFTWARE_PROVIDER(8),
    UTILITY_SUPPLIER(9),
    GRID_OPERATOR(0xA),
    REGIONAL_AUTHORITY(0xB),
    UNUSED_1(0xC),
    UNUSED_2(0xD),
    UNUSED_3(0xE),
    SECURITY_AUTHORITY(0xF);

    private final int id;

    Role(final int id) {
        if (id < Limits.MIN_UNSIGNED || id > Limits.MAX_UNSIGNED_BYTE)
            throw new InvalidParameterException("expected value in range [" +
                    Limits.MIN_UNSIGNED + "," + Limits.MAX_UNSIGNED_BYTE + "]");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Role[] roles = Role.values();

    public static Role fromId(int id) {
        return roles[id];
    }
}
