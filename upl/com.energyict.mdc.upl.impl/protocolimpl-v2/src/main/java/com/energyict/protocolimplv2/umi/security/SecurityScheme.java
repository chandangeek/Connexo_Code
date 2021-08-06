package com.energyict.protocolimplv2.umi.security;

import com.energyict.protocolimplv2.umi.util.Limits;
import java.security.InvalidParameterException;
import java.util.Arrays;

public enum SecurityScheme {
    NO_SECURITY(0x0),
    SYMMETRIC(0x01),
    ASYMMETRIC(0x02);

    public static final int SIZE = 1;
    private final int id;
    public static SecurityScheme[] schemes = SecurityScheme.values();

    SecurityScheme(final int id) {
        if (id < Limits.MIN_UNSIGNED || id > Limits.MAX_UNSIGNED_BYTE)
            throw new InvalidParameterException("expected value in range [" +
                    Limits.MIN_UNSIGNED + "," + Limits.MAX_UNSIGNED_BYTE + "]");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SecurityScheme fromId(int id) {
        return Arrays.stream(schemes)
                .filter(x -> x.getId() == id)
                .findAny().orElseThrow(() -> new InvalidParameterException("SecurityScheme " + id + " is not supported"));
    }
}
